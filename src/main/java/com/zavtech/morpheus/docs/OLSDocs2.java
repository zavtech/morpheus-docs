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
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.testng.annotations.Test;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.frame.DataFrameLeastSquares;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.stats.StatType;
import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.ChartEngine;


/**
 * Code for OLS related documentation
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public class OLSDocs2 {


    /**
     * Returns a dataset of XY values with auto-correlated residuals
     * @param n     the sample size
     * @return      the frame of XY values with autocorrelated residuals
     */
    private DataFrame<Integer,String> sample(int n) {
        return sample(4.15d, 1.45d, 0d, 1d, 20d, n);
    }


    /**
     * Returns a 2D sample dataset based on a population process using the population regression coefficients provided
     * @param alpha     the intercept term for population process
     * @param beta      the slope term for population process
     * @param startX    the start value for independent variable
     * @param stepX     the step size for independent variable
     * @param sigma     the variance to add noise to dependent variable
     * @param n         the size of the sample to generate
     * @return          the frame of XY values
     */
    private DataFrame<Integer,String> sample(double alpha, double beta, double startX, double stepX, double sigma, int n) {
        final Array<Double> xValues = Array.of(Double.class, n).applyDoubles(v -> startX + v.index() * stepX);
        final Array<Double> yValues = Array.of(Double.class, n).applyDoubles(v -> {
            final double yfit = alpha + beta * xValues.getDouble(v.index());
            return new NormalDistribution(yfit, sigma).sample();
        });
        final Array<Integer> rowKeys = Range.of(0, n).toArray();
        return DataFrame.of(rowKeys, String.class, columns -> {
            columns.add("X", xValues);
            columns.add("Y", yValues);
        });
    }


    @Test()
    public void regress() {
        final DataFrame<Integer,String> frame = sample(200);

        frame.regress().ols("Y", "X", true, model -> {
            model.withSolver(DataFrameLeastSquares.Solver.INV);
            System.out.println(model);
            return Optional.empty();
        });

        frame.regress().ols("Y", "X", true, model -> {
            model.withSolver(DataFrameLeastSquares.Solver.QR);
            System.out.println(model);
            return Optional.empty();
        });
    }


    @Test()
    public void plotScatter() throws Exception {
        final DataFrame<Integer,String> frame = sample(100);
        Chart.of(frame, "X", Double.class, chart -> {
            chart.plot(0).withPoints();
            chart.title().withText("OLS Regression");
            chart.subtitle().withText("Artificially generated dataset");
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
    public void plotScatterMany() throws Exception {
        final double beta = 1.45d;
        final double alpha = 4.15d;
        final double sigma = 20d;
        Stream<Chart> charts = IntStream.range(0, 4).mapToObj(i -> {
            DataFrame<Integer,String> frame = sample(alpha, beta, 0, 1, sigma, 100);
            String title = "Sample %s Dataset, Beta: %.2f Alpha: %.2f";
            String subtitle = "Parameter estimates, Beta^: %.3f, Alpha^: %.3f";
            DataFrameLeastSquares<Integer,String> ols = frame.regress().ols("Y", "X", true, Optional::of).get();
            double betaHat = ols.getBetaValue("X", DataFrameLeastSquares.Field.PARAMETER);
            double alphaHat = ols.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
            return Chart.of(frame, "X", Double.class, chart -> {
                chart.plot(0).withPoints();
                chart.title().withText(String.format(title, i, beta, alpha));
                chart.title().withFont(new Font("Arial", Font.BOLD, 14));
                chart.subtitle().withText(String.format(subtitle, betaHat, alphaHat));
                chart.style("Y").withColor(Color.RED).withPointsVisible(true);
                chart.trendLine().add("Y", "OLS");
            });
        });
        List<Chart> chartArray = charts.collect(Collectors.toList());
        ChartEngine.getDefaultEngine().show(4, 4, chartArray);
        Thread.currentThread().join();
    }


    /**
     * Runs 100K regressions on samples from a known population process and plots histogram of estimates
     */
    @Test()
    public void unbiasedness() throws Exception {

        final int n = 100;
        final double actAlpha = 4.15d;
        final double actBeta = 1.45d;
        final double sigma = 20d;
        final int regressionCount = 100000;
        final Range<Integer> rows = Range.of(0, regressionCount);
        final Array<String> columns = Array.of("Beta", "Alpha");
        final DataFrame<Integer,String> results = DataFrame.ofDoubles(rows, columns);

        //Run 100K regressions in parallel
        results.rows().parallel().forEach(row -> {
            final DataFrame<Integer,String> frame = sample(actAlpha, actBeta, 0, 1, sigma, n);
            frame.regress().ols("Y", "X", true, model -> {
                final double alpha = model.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
                final double beta = model.getBetaValue("X", DataFrameLeastSquares.Field.PARAMETER);
                row.setDouble("Alpha", alpha);
                row.setDouble("Beta", beta);
                return Optional.empty();
            });
        });

        Array.of("Beta", "Alpha").forEach(coefficient -> {
            Chart.hist(results, coefficient, 250, chart -> {
                final double mean = results.colAt(coefficient).stats().mean();
                final double stdDev = results.colAt(coefficient).stats().stdDev();
                final double actual = coefficient.equals("Beta") ? actBeta : actAlpha;
                final String title = "%s Histogram from %s Regressions (n=%s)";
                final String subtitle = "Actual: %.4f, Mean: %.4f, StdDev: %.4f";
                chart.title().withText(String.format(title, coefficient, regressionCount, n));
                chart.subtitle().withText(String.format(subtitle, actual, mean, stdDev));
                chart.writerPng(new File(String.format("../morpheus-docs/docs/images/ols/ols-%s-unbiased.png", coefficient)), 700, 400);
                chart.show(700, 400);
            });
        });

        results.cols().stats().variance().out().print();

        Thread.currentThread().join();
    }


    /**
     * Runs 100K regressions for samples 100-500,100 and plots histograms of resulting estimates
     */
    @Test()
    public void consistency() throws Exception {

        final double actAlpha = 4.15d;
        final double actBeta = 1.45d;
        final double sigma = 20d;
        final int regressionCount = 100000;
        final Range<Integer> sampleSizes = Range.of(100, 600, 100);
        final Range<Integer> rows = Range.of(0, regressionCount);
        final DataFrame<Integer,String> results = DataFrame.of(rows, String.class, columns -> {
            sampleSizes.forEach(n -> {
                columns.add(String.format("Beta(n=%s)", n), Double.class);
                columns.add(String.format("Alpha(n=%s)", n), Double.class);
            });
        });

        sampleSizes.forEach(n -> {
            System.out.println("Running " + regressionCount + " regressions for n=" + n);
            final String betaKey = String.format("Beta(n=%s)", n);
            final String alphaKey = String.format("Alpha(n=%s)", n);
            results.rows().parallel().forEach(row -> {
                final DataFrame<Integer,String> frame = sample(actAlpha, actBeta, 0, 1, sigma, n);
                frame.regress().ols("Y", "X", true, model -> {
                    final double alpha = model.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
                    final double beta = model.getBetaValue("X", DataFrameLeastSquares.Field.PARAMETER);
                    row.setDouble(alphaKey, alpha);
                    row.setDouble(betaKey, beta);
                    return Optional.empty();
                });
            });
        });

        Array.of("Beta", "Alpha").forEach(coeff -> {
            final DataFrame<Integer,String> coeffResults = results.cols().select(col -> col.key().startsWith(coeff));
            Chart.hist(coeffResults, 250, chart -> {
                chart.axes().domain().label().withText("Coefficient Estimate");
                chart.title().withText(coeff + " Histograms of " + regressionCount + " Regressions");
                chart.subtitle().withText(coeff + " Variance decreases as sample size increases");
                chart.legend().on().bottom();
                chart.writerPng(new File(String.format("../morpheus-docs/docs/images/ols/ols-%s-consistency.png", coeff.toLowerCase())), 700, 400);
                chart.show(700, 400);
            });
        });

        Array<DataFrame<String,StatType>> variances = Array.of("Beta", "Alpha").map(value -> {
            final String coefficient = value.getValue();
            final Matcher matcher = Pattern.compile(coefficient + "\\(n=(\\d+)\\)").matcher("");
            return results.cols().select(column -> {
                final String name = column.key();
                return matcher.reset(name).matches();
            }).cols().mapKeys(column -> {
                final String name = column.key();
                if (matcher.reset(name).matches()) return matcher.group(1);
                throw new IllegalArgumentException("Unexpected column name: " + column.key());
            }).cols().stats().variance();
        });

        ChartEngine.getDefaultEngine().show(1, 2, Array.of(
            Chart.of(variances.getValue(0), chart -> {
                chart.plot(0).withBars(0d);
                chart.style(StatType.VARIANCE).withColor(new Color(255, 100, 100));
                chart.title().withText("Beta variance with sample size");
                chart.axes().range(0).label().withText("Beta Variance");
                chart.axes().domain().label().withText("Sample Size");
            }),
            Chart.of(variances.getValue(1), chart -> {
                chart.plot(0).withBars(0d);
                chart.style(StatType.VARIANCE).withColor(new Color(102, 204, 255));
                chart.title().withText("Alpha variance with sample size");
                chart.axes().range(0).label().withText("Alpha Variance");
                chart.axes().domain().label().withText("Sample Size");
            }))
        );

        Thread.currentThread().join();
    }


    @Test()
    public void checkPerformance() {
        final DataFrame<Integer,String> data = sample(1000000);
        data.regress().ols("Y", "X", true, model -> {
            System.out.println(model);
            return Optional.empty();
        });
    }


}
