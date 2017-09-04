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
package com.zavtech.morpheus.docs.stats;

import java.awt.*;
import java.io.File;
import java.util.Optional;

import org.testng.annotations.Test;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.frame.DataFrameLeastSquares;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.ChartShape;


/**
 * Example from Regression By Example
 */
public class WLSDocs2 {


    @Test
    public void scatterPlot() throws Exception {
        DataFrame<Integer,String> frame = DataFrame.read().csv("/supervisor.csv");
        frame.regress().ols("y", "x", true, model -> {
            System.out.println(model);
            return Optional.empty();
        });

        Chart.create().withScatterPlot(frame, false, "x", chart -> {
            chart.plot().style("y").withColor(Color.BLUE);
            chart.title().withText("Supervisors (y) vs Workers (x)");
            chart.subtitle().withText("Source: Regression by Example, Chatterjee & Price (1977)");
            chart.plot().axes().domain().label().withText("Worker Count");
            chart.plot().axes().range(0).label().withText("Supervisor Count");
            chart.plot().trend("y");
            chart.writerPng(new File("./docs/images/wls/wls-chatterjee1.png"), 700, 400, true);
            chart.show();
        });

        Thread.currentThread().join();
    }

    @Test()
    public void plotResiduals() throws Exception {
        DataFrame<Integer,String> frame = DataFrame.read().csv("/supervisor.csv");
        frame.regress().ols("y", "x", true, model -> {
            Chart.create().withResidualsVsFitted(model, chart -> {
                chart.title().withText("OLS Residuals vs Fitted Y Values");
                chart.plot().axes().domain().label().withText("Y(fitted)");
                chart.plot().axes().range(0).label().withText("OLS Residual");
                chart.legend().off();
                chart.writerPng(new File("./docs/images/wls/wls-chatterjee2.png"), 700, 400, true);
                chart.show();
            });
            return Optional.empty();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void wls() {
        DataFrame<Integer,String> frame = DataFrame.read().csv("/supervisor.csv");
        Array<Double> weights1 = Array.of(frame.colAt("x").toDoubleStream().map(v -> 1d / Math.pow(v, 2d)).toArray());
        frame.regress().wls("y", "x", weights1, true, model -> {
            System.out.println(model);
            return Optional.empty();
        });
    }


    @Test()
    public void weightEstimate() throws Exception {
        DataFrame<Integer,String> frame = DataFrame.read().csv("/supervisor.csv");
        frame.regress().ols("y", "x", true, model -> {
            final DataFrame<Integer,String> residuals = model.getResiduals();
            final DataFrame<Integer,String> residualsAbs = residuals.mapToDoubles(v -> Math.abs(v.getDouble()));
            final DataFrame<Integer,String> xValues = frame.cols().select("x");
            final DataFrame<Integer,String> newData = DataFrame.concatColumns(residualsAbs, xValues);
            Chart.create().withScatterPlot(newData, false, "x", chart -> {
                chart.plot().style("Residuals").withPointsVisible(true).withColor(Color.BLUE).withPointShape(ChartShape.DIAMOND);
                chart.title().withText("ABS(Residuals) vs Predictor of Original Regression");
                chart.subtitle().withText("Regression line is a proxy for change in variance of dependent variable");
                chart.plot().axes().domain().label().withText("Worker Count");
                chart.plot().axes().range(0).label().withText("|Residual|");
                chart.plot().trend("Residuals");
                chart.writerPng(new File("./docs/images/wls/wls-chatterjee4.png"), 700, 400, true);
                chart.show();
            });
            return Optional.empty();
        });

        Thread.currentThread().join();

    }


    /**
     * Returns the vector of weights for the WLS regression by regressing |residuals| on the predictor
     * @param frame     the frame of original data
     * @return          the weight vector for diagonal matrix in WLS
     */
    private Array<Double> computeWeights(DataFrame<Integer,String> frame) {
        return frame.regress().ols("y", "x", true, model -> {
            final DataFrame<Integer,String> residuals = model.getResiduals();
            final DataFrame<Integer,String> residualsAbs = residuals.mapToDoubles(v -> Math.abs(v.getDouble()));
            final DataFrame<Integer,String> xValues = frame.cols().select("x");
            final DataFrame<Integer,String> newData = DataFrame.concatColumns(residualsAbs, xValues);
            return newData.regress().ols("Residuals", "x", false, ols -> {
                final DataFrame<Integer,String> yHat = ols.getFittedValues();
                final double[] weights = yHat.colAt(0).toDoubleStream().map(v -> 1d / Math.pow(v, 2d)).toArray();
                return Optional.of(Array.of(weights));
            });
        }).orElse(null);
    }


    /**
     * Generate a scatter plot with both OLS and WLS regression lines
     */
    @Test()
    public void plotCompare() throws Exception {

        DataFrame<Integer,String> frame = DataFrame.read().csv("/supervisor.csv");
        double[] x = frame.colAt("x").toDoubleStream().toArray();

        DataFrameLeastSquares<Integer,String> ols = frame.regress().ols("y", "x", true, Optional::of).orElse(null);
        double olsAlpha = ols.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
        double olsBeta = ols.getBetaValue("x", DataFrameLeastSquares.Field.PARAMETER);
        DataFrame<Integer,String> olsFit = createFitted(olsAlpha, olsBeta, x, "OLS");

        Array<Double> weights = computeWeights(frame);
        DataFrameLeastSquares<Integer,String> wls = frame.regress().wls("y", "x", weights, true, Optional::of).orElse(null);
        double wlsAlpha = wls.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
        double wlsBeta = wls.getBetaValue("x", DataFrameLeastSquares.Field.PARAMETER);
        DataFrame<Integer,String> wlsFit = createFitted(wlsAlpha, wlsBeta, x, "WLS");

        Chart.create().withScatterPlot(frame, false, "x", chart -> {
            chart.plot().style("y").withColor(Color.BLUE);
            chart.plot().<String>data().add(olsFit, "x");
            chart.plot().<String>data().add(wlsFit, "x");
            chart.plot().render(1).withLines(false, false);
            chart.plot().render(2).withLines(false, false);
            chart.plot().axes().domain().label().withText("X-value");
            chart.plot().axes().range(0).label().withText("Y-value");
            chart.plot().style("OLS").withColor(Color.RED).withLineWidth(2f);
            chart.plot().style("WLS").withColor(Color.GREEN).withLineWidth(2f);
            chart.title().withText("OLS vs WLS Regression Comparison");
            chart.subtitle().withText(String.format("Beta OLS: %.3f, Beta WLS: %.3f", olsBeta, wlsBeta));
            chart.legend().on().right();
            chart.writerPng(new File("./docs/images/wls/wls-chatterjee3.png"), 700, 450, true);
            chart.show();
        });

        Thread.currentThread().join();
    }


    /**
     * Returns a DataFrame of fitted values given alpha, beta and the x-values
     * @param alpha     the alpha or intercept parameter
     * @param beta      the beta or slope parameter
     * @param x         the x-values or independent variable values
     * @param yName     the name for the fitted values
     * @return          the newly created DataFrame
     */
    private DataFrame<Integer,String> createFitted(double alpha, double beta, double[] x, String yName) {
        return DataFrame.ofDoubles(Range.of(0, x.length), Array.of("x", yName), v -> {
            switch (v.colOrdinal()) {
                case 0:  return x[v.rowOrdinal()];
                default: return alpha + beta * x[v.rowOrdinal()];
            }
        });
    }


}
