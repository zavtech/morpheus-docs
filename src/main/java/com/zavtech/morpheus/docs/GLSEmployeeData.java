/**
 * Copyright (C) 2014-2017 Xavier Witdouck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zavtech.morpheus.docs;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.testng.annotations.Test;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.frame.DataFrameCursor;
import com.zavtech.morpheus.frame.DataFrameLeastSquares;
import com.zavtech.morpheus.frame.DataFrameRow;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.viz.chart.Chart;

public class GLSEmployeeData {


    private DataFrame<Integer,String> load() {
        return DataFrame.read().csv(options -> {
            options.setResource("/employee.csv");
            options.setHeader(true);
            options.setExcludeColumnIndexes(0);
            options.setCharset(StandardCharsets.UTF_16);
            options.setRowKeyParser(Integer.class, values -> Integer.parseInt(values[0]));
        });
    }


    /**
     ---------------------------------Linear Regression Results-----------------------------------
     * Model: OLS
     * Sample Size: 60
     * Degrees Of Freedom: 58.0
     * R-Squared: 0.7443064331290693
     * R-Squared(adjusted): 0.7398979233554326
     * Standard Error: 1.5897896031543344
     * Significance Level: 0.05
     * Durbin-Watson Statistic:0.3685646982070464

     Index      |  PARAMETER   |  STD_ERROR   |    T_STAT     |   P_VALUE    |   CI_LOWER    |   CI_UPPER   |
     ---------------------------------------------------------------------------------------------------------
     Intercept  |  2.84791112  |  3.29996196  |   0.86301332  |  0.39168479  |  -3.75768043  |  9.45350266  |
     vendor     |  0.12244172  |  0.00942322  |  12.99361464  |      0.0000  |   0.10357909  |  0.14130435  |

     ===============================================
     */
    @Test()
    public void regress() {
        DataFrame<Integer,String> frame = load();
        frame.regress().ols("metal", "vendor", true, model -> {
            final DataFrame<Integer,String> residuals = model.getResiduals();
            final Array<Double> values = Array.of(residuals.colAt(0).toDoubleStream().toArray());
            final double ar1 = estimateAutoregressiveModel(values);
            System.out.println(model);
            System.out.printf("\nAR(1) coefficient: %s",  ar1);
            return Optional.of(model);
        });
    }


    @Test()
    public void scatter() throws Exception {
        DataFrame<Integer,String> frame = load();
        Chart.withPoints(frame, "vendor", Double.class, chart -> {
            chart.plot(0).withPoints();
            chart.style("metal").withPointsVisible(true).withColor(Color.BLUE);
            chart.trendLine().add("metal", "metal(trend)");
            chart.show();
        });
        Thread.currentThread().join();
    }

    @Test()
    public void acf() throws Exception {
        DataFrame<Integer,String> frame = load();
        frame.regress().ols("metal", "vendor", true, model -> {
            Chart.acf(model, frame.rowCount()/2, 0.2d).show();
            return Optional.empty();
        });
        Thread.currentThread().join();
    }

    @Test()
    public void residuals() throws Exception {
        DataFrame<Integer,String> frame = load();
        frame.regress().ols("metal", "vendor", true, model -> {
            Chart.residualsVsOrder(model).show();
            return Optional.empty();
        });
        Thread.currentThread().join();
    }


    @Test()
    public void fgls() throws Exception {
        final DataFrame<Integer,String> frame = load();
        frame.regress().ols("metal", "vendor", true, initialOls -> {
            Array<Double> residuals = null;
            Range<Integer> rowKeys = Range.of(0, frame.rowCount()-1);
            DataFrame<Integer,String> transformedModel = DataFrame.ofDoubles(rowKeys, Array.of("metal*", "vendor*"));
            final DataFrameCursor<Integer,String> xt = frame.cursor().moveToColumn("vendor");
            final DataFrameCursor<Integer,String> xt_1 = frame.cursor().moveToColumn("vendor");
            final DataFrameCursor<Integer,String> yt = frame.cursor().moveToColumn("metal");
            final DataFrameCursor<Integer,String> yt_1 = frame.cursor().moveToColumn("metal");
            for (int i=0; i<20; ++i) {
                if (residuals == null) {
                    residuals = Array.of(initialOls.getResiduals().colAt(0).toDoubleStream().toArray());
                }
                final double rho = estimateAutoregressiveModel(residuals);
                System.out.printf("\nRHO estimate = %s", rho);
                transformedModel.rows().forEach(row -> {
                    xt.moveToRow(row.ordinal()+1);
                    yt.moveToRow(row.ordinal()+1);
                    xt_1.moveToRow(row.ordinal());
                    yt_1.moveToRow(row.ordinal());
                    row.setDouble("metal*", yt.getDouble() - rho * yt_1.getDouble());
                    row.setDouble("vendor*", xt.getDouble() - rho * xt_1.getDouble());
                });
                residuals = transformedModel.regress().ols("metal*", "vendor*", true, ols -> {
                    final double beta = ols.getBetaValue("vendor*", DataFrameLeastSquares.Field.PARAMETER);
                    final double alphaStar = ols.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
                    final double alpha = alphaStar / (1 - rho);
                    final Array<Double> newResiduals = computeResiduals(alpha, beta, frame, "metal", "vendor");
                    return Optional.of(newResiduals);
                }).get();
            }
            //System.out.println(olsModel);
            return Optional.empty();
        });
    }


    /**
     * Returns the AR(n) coefficient estimate of the time series of values
     * @param values    the values to estimate a time series AR(N) coefficient
     * @return          the AR(n) coefficient
     */
    private Double estimateAutoregressiveModel(Array<Double> values) {
        final Range<Integer> rowKeys = Range.of(0, values.length()-1);
        return DataFrame.of(rowKeys, String.class, columns -> {
            columns.add("X(t)", Double.class, v -> values.getDouble(v.rowOrdinal()+1));
            columns.add("X(t-1)", Double.class, v -> values.getDouble(v.rowOrdinal()));
        }).regress().ols("X(t)", "X(t-1)", false, arModel -> {
            return Optional.of(arModel.getBetaValue("X(t-1)", DataFrameLeastSquares.Field.PARAMETER));
        }).orElseThrow(() -> new RuntimeException("AR fit did not return a rho estimate"));
    }


    private <R,C> Array<Double> computeResiduals(double alpha, double beta, DataFrame<R,C> frame, C regressand, C regressor) {
        return Array.of(Double.class, frame.rowCount()).applyDoubles(v -> {
            final int index = v.index();
            final double xValue = frame.data().getDouble(index, regressor);
            final double yValue = frame.data().getDouble(index, regressand);
            final double fitted = alpha + beta * xValue;
            return yValue - fitted;
        });
    }


    @Test()
    public void ar1() {
        DataFrame<Integer,String> frame = load();
        frame.regress().ols("metal", "vendor", true, model -> {
            final DataFrame<?,String> residuals = model.getResiduals();
            final Range<Integer> rowKeys = Range.of(0, residuals.rowCount()-1);
            final DataFrame<Integer,String> arData = DataFrame.of(rowKeys, String.class, columns -> {
                columns.add("X(t)", Double.class, v -> residuals.data().getDouble(v.rowOrdinal() + 1, 0));
                columns.add("X(t-1)", Double.class, v -> residuals.data().getDouble(v.rowOrdinal(), 0));
            });

            SimpleRegression regression = new SimpleRegression(false);
            arData.rows().forEach(row -> {
                final double x = row.getDouble("X(t-1)");
                final double y = row.getDouble("X(t)");
                regression.addData(x, y);
            });

            System.out.printf("\nBeta: %s", regression.getSlope());
            System.out.printf("\nSE(Beta): %s", regression.getSlopeStdErr());

            return Optional.empty();
        });
    }



}
