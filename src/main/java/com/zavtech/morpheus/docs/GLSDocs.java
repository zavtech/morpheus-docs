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
import java.util.Optional;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.testng.annotations.Test;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.frame.DataFrameCursor;
import com.zavtech.morpheus.frame.DataFrameLeastSquares;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.viz.chart.Chart;


public class GLSDocs {


    /**
     * Returns a sample dataset based on a population process using the population regression coefficients provided
     * @param alpha     the intercept term for population process
     * @param beta      the slope term for population process
     * @param rho       the AR(1) coefficient used to generate serially correlated error terms for sample dataset
     * @param startX    the start value for independent variable
     * @param stepX     the step size for independent variable
     * @return          the frame of XY values with serially correlated residuals
     */
    private DataFrame<Integer,String> sample(double alpha, double beta, double rho, double startX, double stepX, int n) {
        final RealDistribution distribution1 = new NormalDistribution(0, 40d);
        final RealDistribution distribution2 = new NormalDistribution(0, 40d);
        final Array<Double> xValues = Array.of(Double.class, n).applyDoubles(v -> startX + v.index() * stepX);
        final Array<Integer> rowKeys = Range.of(0, n).toArray();
        return DataFrame.of(rowKeys, String.class, columns -> {
            columns.add("X", xValues);
            columns.add("Y", Array.of(Double.class, n).applyDoubles(v -> {
                final double xValue = xValues.getDouble(v.index());
                final double yActual = alpha + beta * xValue;
                if (v.index() == 0) return yActual + distribution1.sample();
                else {
                    final double priorX = xValues.getDouble(v.index()-1);
                    final double priorY = v.array().getDouble(v.index()-1);
                    final double priorError = priorY - (alpha + beta * priorX);
                    final double error = rho * priorError + distribution2.sample();
                    return yActual + error;
                }
            }));
        });
    }


    @Test()
    public void compare() {
        final DataFrame<Integer,String> frame = sample(20d, 4d, 0.4d, 1, 1, 1000);

        //Run OLS on the dataset first
        frame.regress().ols("Y", "X", true, model -> {
            System.out.println(model);
            return Optional.empty();
        });

        //Runs GLS and assume AR(1) errors in residuals
        frame.regress().ols("Y", "X", true, model -> {
            final double autocorr = model.getResiduals().colAt(0).stats().autocorr(1);
            final DataFrame<Integer,Integer> omega = createOmega(frame.rowCount(), autocorr);
            frame.regress().gls("Y", "X", omega, true, gls -> {
                System.out.println(gls);
                return Optional.empty();
            });
            return Optional.empty();
        });
    }


    @Test()
    public void olsRuns() throws Exception {
        final int regressionCount = 100000;
        final DataFrame<Integer,String> results = DataFrame.ofDoubles(
            Range.of(0, regressionCount),
            Array.of("Beta", "Alpha")
        );
        results.rows().parallel().forEach(row -> {
            final DataFrame<Integer,String> frame = sample(20d, 4d, 0.5d, 1, 1, 100);
            frame.regress().ols("Y", "X", true, model -> {
                final double alpha = model.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
                final double beta = model.getBetaValue("X", DataFrameLeastSquares.Field.PARAMETER);
                row.setDouble("Alpha", alpha);
                row.setDouble("Beta", beta);
                return Optional.empty();
            });
        });

        System.out.printf("\nBeta mean: %s", results.colAt(0).stats().mean());
        System.out.printf("\nBeta stdDev: %s", results.colAt(0).stats().stdDev());

        Chart.hist(results, "Beta", 100, chart -> {
            chart.title().withText("Beta Estimate Histogram");
            chart.subtitle().withText("Regression count: " + regressionCount);
            chart.show();
        });

        Chart.hist(results, "Alpha", 100, chart -> {
            chart.title().withText("Alpha Estimate Histogram");
            chart.subtitle().withText("Regression count: " + regressionCount);
            chart.show();
        });


        Thread.currentThread().join();
    }


    @Test()
    public void glsRuns() throws Exception {
        final int regressionCount = 100000;
        final DataFrame<Integer,String> results = DataFrame.ofDoubles(
            Range.of(0, regressionCount),
            Array.of("Beta", "Alpha")
        );
        results.rows().parallel().forEach(row -> {
            final DataFrame<Integer,String> frame = sample(20d, 4d, 0.5d, 1, 1, 100);
            frame.regress().ols("Y", "X", true, model -> {
                final double autocorr = model.getResiduals().colAt(0).stats().autocorr(1);
                final DataFrame<Integer,Integer> omega = createOmega(frame.rowCount(), autocorr);
                frame.regress().gls("Y", "X", omega, true, gls -> {
                    final double alpha = model.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
                    final double beta = model.getBetaValue("X", DataFrameLeastSquares.Field.PARAMETER);
                    row.setDouble("Alpha", alpha);
                    row.setDouble("Beta", beta);
                    return Optional.empty();
                });
                return Optional.empty();
            });
        });

        System.out.printf("\nBeta mean: %s", results.colAt(0).stats().mean());
        System.out.printf("\nBeta stdDev: %s", results.colAt(0).stats().stdDev());

        Chart.hist(results, "Beta", 100, chart -> {
            chart.title().withText("Beta Estimate Histogram");
            chart.subtitle().withText("Regression count: " + regressionCount);
            chart.show();
        });

        Thread.currentThread().join();
    }



    @Test()
    public void plotScatter() throws Exception {
        final DataFrame<Integer,String> frame = sample(20d, 4d, 0.5d, 1, 1, 200);
        Chart.of(frame, "X", Double.class, chart -> {
            chart.plot(0).withPoints();
            chart.title().withText("Regression with Serial Correlation");
            chart.subtitle().withText("Artificially generated dataset with AR(1) errors");
            chart.title().withFont(new Font("Arial", Font.BOLD, 16));
            chart.axes().domain().label().withText("X");
            chart.axes().range(0).label().withText("Y");
            chart.style("Y").withColor(Color.RED).withPointsVisible(true);
            chart.trendLine().add("Y", "Y(fitted)");
            chart.show();
        });
        Thread.currentThread().join();
    }


    @Test()
    public void plotResidualsVsOrder() throws Exception {
        final DataFrame<Integer,String> frame = sample(20d, 4d, 0.5d, 1, 1, 200);
        frame.regress().ols("Y", "X", true, model -> {
            System.out.println(model);
            Chart.residualsVsOrder(model).show();
            return Optional.empty();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void plotResidualsVsFitted() throws Exception {
        final DataFrame<Integer,String> frame = sample(20d, 4d, 0.5d, 1, 1, 100);
        frame.regress().ols("Y", "X", true, model -> {
            System.out.println(model);
            Chart.residualsVsFitted(model).show();
            return Optional.empty();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void plotACF() throws Exception {
        final DataFrame<Integer,String> frame = sample(20d, 4d, 0.5d, 1, 1, 100);
        frame.regress().ols("Y", "X", true, model -> {
            System.out.println(model);
            Chart.acf(model, 50, 0.2d).show();
            return Optional.empty();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void gls() {
        final DataFrame<Integer,String> frame = sample(20d, 4d, 0.5d, 1, 1, 100);
        frame.regress().ols("Y", "X", true, model -> {
            System.out.println(model);
            final double autocorr = model.getResiduals().colAt(0).stats().autocorr(1);
            final DataFrame<Integer,Integer> omega = createOmega(frame.rowCount(), autocorr);
            omega.out().print();
            frame.regress().gls("Y", "X", omega, true, gls -> {
                System.out.println(gls);
                return Optional.empty();
            });
            return Optional.empty();
        });
    }


    /**
     * Returns the correlation matrix omega
     * @param size      the size for the correlation matrix
     * @param autocorr  the auto correlation value to base matrix on
     * @return          the newly created correlation matrix
     */
    private DataFrame<Integer,Integer> createOmega(int size, double autocorr) {
        final Range<Integer> keys = Range.of(0, size);
        return DataFrame.ofDoubles(keys, keys, v -> {
            return Math.pow(autocorr,  Math.abs(v.rowOrdinal() - v.colOrdinal()));
        });
    }



    @Test()
    public void ar() throws Exception {
        final DataFrame<Integer,String> frame = sample(20d, 4d, 0.5d, 1, 1, 100);
        frame.regress().ols("Y", "X", true, model -> {
            final DataFrame<Integer,String> residuals = model.getResiduals();
            final Range<Integer> rowKeys = Range.of(0, residuals.rowCount()-1);
            final DataFrame<Integer,String> arData = DataFrame.of(rowKeys, String.class, columns -> {
                columns.add("X(t)", Double.class, v -> residuals.data().getDouble(v.rowOrdinal()+1, 0));
                columns.add("X(t-1)", Double.class, v -> residuals.data().getDouble(v.rowOrdinal(), 0));
            });

            Chart.of(arData, "X(t-1)", Double.class, chart -> {
                chart.plot(0).withPoints();
                chart.style("X(t)").withPointsVisible(true);
                chart.style("X(t)").withColor(Color.BLUE);
                chart.show();
            });

            arData.regress().ols("X(t)", "X(t-1)", true, arModel -> {
                System.out.println(arModel);
                return Optional.empty();
            });

            return Optional.empty();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void fgls() throws Exception {
        final DataFrame<Integer,String> frame = sample(20d, 4d, 0.5d, 1, 1, 100);
        frame.regress().ols("Y", "X", true, initialOls -> {
            Array<Double> residuals = null;
            Range<Integer> rowKeys = Range.of(0, frame.rowCount()-1);
            DataFrame<Integer,String> transformedModel = DataFrame.ofDoubles(rowKeys, Array.of("Y*", "X*"));
            final DataFrameCursor<Integer,String> xt = frame.cursor().moveToColumn("X");
            final DataFrameCursor<Integer,String> xt_1 = frame.cursor().moveToColumn("X");
            final DataFrameCursor<Integer,String> yt = frame.cursor().moveToColumn("Y");
            final DataFrameCursor<Integer,String> yt_1 = frame.cursor().moveToColumn("Y");
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
                    row.setDouble("Y*", yt.getDouble() - rho * yt_1.getDouble());
                    row.setDouble("X*", xt.getDouble() - rho * xt_1.getDouble());
                });
                residuals = transformedModel.regress().ols("Y*", "X*", true, ols -> {
                    final double beta = ols.getBetaValue("X*", DataFrameLeastSquares.Field.PARAMETER);
                    final double alphaStar = ols.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
                    final double alpha = alphaStar / (1 - rho);
                    final Array<Double> newResiduals = computeResiduals(alpha, beta, frame, "Y", "X");
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

}
