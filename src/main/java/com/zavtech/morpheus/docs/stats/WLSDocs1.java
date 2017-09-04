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
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.frame.DataFrameLeastSquares;
import com.zavtech.morpheus.frame.DataFrameLeastSquares.Field;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.stats.StatType;
import com.zavtech.morpheus.util.Collect;
import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.xy.XyPlot;


public class WLSDocs1 {


    /**
     * Returns a sample dataset based on a known population process using the regression coefficients provided
     * The sample dataset exhibits increasing variance in the dependent variable as the independent variable increases.
     * @param alpha     the intercept term for population process
     * @param beta      the slope term for population process
     * @param startX    the start value for independent variable
     * @param stepX     the step size for independent variable
     * @return          the frame of XY values
     */
    private DataFrame<Integer,String> sample(double alpha, double beta, double startX, double stepX, int n) {
        final Array<Double> xValues = Array.of(Double.class, n).applyDoubles(v -> startX + v.index() * stepX);
        final Array<Double> yValues = Array.of(Double.class, n).applyDoubles(v -> {
            final double xValue = xValues.getDouble(v.index());
            final double yFitted = alpha + beta * xValue;
            final double stdDev = xValue * 2d;
            return new NormalDistribution(yFitted, stdDev).sample();
        });
        final Range<Integer> rowKeys = Range.of(0, n);
        return DataFrame.of(rowKeys, String.class, columns -> {
            columns.add("X", xValues);
            columns.add("Y", yValues);
        });
    }


    /**
     * Returns a sample dataset based on a known population process using the regression coefficients provided
     * The sample dataset exhibits increasing variance in the dependent variable as the independent variable increases.
     * @param alpha     the intercept term for population process
     * @param beta1     the slope of first term for population process
     * @param beta2     the slope of second term for population process
     * @param startX    the start value for independent variable
     * @param stepX     the step size for independent variable
     * @return          the frame of XY values
     */
    private DataFrame<Integer,String> sample(double alpha, double beta1, double beta2, double startX, double stepX, int n) {
        final Array<Double> x1Values = Array.of(Double.class, n).applyDoubles(v -> startX + v.index() * stepX);
        final Array<Double> x2Values = Array.of(Double.class, n).applyDoubles(v -> startX * 15 + 2 * Math.random() + v.index() * stepX);
        final Array<Double> yValues = Array.of(Double.class, n).applyDoubles(v -> {
            final double x1Value = x1Values.getDouble(v.index());
            final double x2Value = x2Values.getDouble(v.index());
            final double yFitted = alpha + beta1 * x1Value + beta2 * x2Value;
            final double stdDev = x1Value * 2d;
            return new NormalDistribution(yFitted, stdDev).sample();
        });
        final Range<Integer> rowKeys = Range.of(0, n);
        return DataFrame.of(rowKeys, String.class, columns -> {
            columns.add("Y", yValues);
            columns.add("X1", x1Values);
            columns.add("X2", x2Values);
        });
    }

    @Test()
    public void saveData() {
        sample(20d, 4d, 6d, 1, 1, 100).write().csv(options -> {
            options.setFile("/Users/witdxav/Dropbox/projects/morpheus/morpheus-core/src/test/resources/csv/wls-2.csv");
        });
    }



    @Test()
    public void plotScatter() throws Exception {
        final double beta = 4d;
        final double alpha = 20d;
        Chart.show(2, IntStream.range(0, 4).mapToObj(i -> {
            DataFrame<Integer,String> frame = sample(alpha, beta, 1, 1, 100);
            String title = "Sample %s Dataset, Beta: %.2f Alpha: %.2f";
            String subtitle = "Parameter estimates, Beta^: %.3f, Alpha^: %.3f";
            DataFrameLeastSquares<Integer,String> ols = frame.regress().ols("Y", "X", true, Optional::of).get();
            double betaHat = ols.getBetaValue("X", DataFrameLeastSquares.Field.PARAMETER);
            double alphaHat = ols.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
            return Chart.create().withScatterPlot(frame, false, "X", chart -> {
                chart.title().withText(String.format(title, i, beta, alpha));
                chart.title().withFont(new Font("Arial", Font.BOLD, 14));
                chart.subtitle().withText(String.format(subtitle, betaHat, alphaHat));
                chart.plot().style("Y").withColor(Color.RED).withPointsVisible(true);
                chart.plot().trend("Y").withLineWidth(2f);
                chart.writerPng(new File(String.format("./docs/images/wls/wls-sample-%s.png", i)), 350, 250, true);

            });
        }));

        Thread.currentThread().join();
    }






    @Test()
    public void compare() {
        final double beta = 4d;
        final double alpha = 20d;
        final DataFrame<Integer,String> frame = sample(alpha, beta, 1, 1, 100);
        frame.regress().ols("Y", "X", true, model -> {
            System.out.println(model);
            return Optional.empty();
        });
        final Array<Double> weights = computeWeights(frame);
        frame.regress().wls("Y", "X", weights, true, model -> {
            System.out.println(model);
            return Optional.empty();
        });
    }


    @Test()
    public void unbiasedness() throws Exception {
        int n = 100;
        double beta = 4d;
        double alpha = 20d;
        int regressionCount = 100000;
        Range<Integer> rows = Range.of(0, regressionCount);
        Array<String> columns = Array.of("Beta", "Alpha");
        DataFrame<Integer,String> results = DataFrame.ofDoubles(rows, columns);

        results.rows().parallel().forEach(row -> {
            final DataFrame<Integer,String> frame = sample(alpha, beta, 1, 1, n);
            final Array<Double> weights = computeWeights(frame);
            frame.regress().wls("Y", "X", weights, true, model -> {
                final double alphaHat = model.getInterceptValue(Field.PARAMETER);
                final double betaHat = model.getBetaValue("X", Field.PARAMETER);
                row.setDouble("Alpha", alphaHat);
                row.setDouble("Beta", betaHat);
                return Optional.empty();
            });
        });

        Array.of("Beta", "Alpha").forEach(coeff -> {
            final DataFrame<Integer,String> coeffResults = results.cols().select(col -> col.key().startsWith(coeff));
            Chart.create().withHistPlot(coeffResults, 250, true, chart -> {
                String title = "%s Histogram of %s WLS regressions";
                String subtitle = "%s estimate unbiasedness, Actual: %.2f, Mean: %.2f, Variance: %.2f";
                double actual = coeff.equals("Beta") ? beta : alpha;
                double estimate = coeffResults.colAt(coeff).stats().mean();
                double variance = coeffResults.colAt(coeff).stats().variance();
                Color color = coeff.equals("Beta") ? new Color(255, 100, 100) : new Color(102, 204, 255);
                chart.plot().style(coeff).withColor(color);
                chart.plot().axes().domain().label().withText(coeff + " Estimate");
                chart.title().withText(String.format(title, coeff, regressionCount));
                chart.subtitle().withText(String.format(subtitle, coeff, actual, estimate, variance));
                chart.writerPng(new File(String.format("./docs/images/wls/wls-%s-unbiasedness.png", coeff)), 700, 400, true);
                chart.show(700, 400);
            });
        });

        Thread.currentThread().join();
    }



    @Test()
    public void efficiency() throws Exception {
        final int n = 100;
        final double beta = 4d;
        final double alpha = 20d;
        final int regressionCount = 100000;
        Range<Integer> rows = Range.of(0, regressionCount);
        Array<String> columns = Array.of("Beta(OLS)", "Alpha(OLS)", "Beta(WLS)", "Alpha(WLS)");
        DataFrame<Integer,String> results = DataFrame.ofDoubles(rows, columns);

        results.rows().parallel().forEach(row -> {
            final DataFrame<Integer,String> frame = sample(alpha, beta, 1, 1, n);
            frame.regress().ols("Y", "X", true, model -> {
                double alphaHat = model.getInterceptValue(Field.PARAMETER);
                double betaHat = model.getBetaValue("X", Field.PARAMETER);
                row.setDouble("Alpha(OLS)", alphaHat);
                row.setDouble("Beta(OLS)", betaHat);
                return Optional.empty();
            });

            final Array<Double> weights = computeWeights(frame);
            frame.regress().wls("Y", "X", weights, true, model -> {
                double alphaHat = model.getInterceptValue(Field.PARAMETER);
                double betaHat = model.getBetaValue("X", Field.PARAMETER);
                row.setDouble("Alpha(WLS)", alphaHat);
                row.setDouble("Beta(WLS)", betaHat);
                return Optional.empty();
            });
        });

        Array.of("Alpha", "Beta").forEach(coeff -> {
            final String olsKey = coeff + "(OLS)";
            final String wlsKey = coeff + "(WLS)";
            final DataFrame<Integer,String> data = results.cols().select(olsKey, wlsKey);
            Chart.create().withHistPlot(data, 200, true, chart -> {
                double meanOls = results.colAt(olsKey).stats().mean();
                double stdOls = results.colAt(olsKey).stats().stdDev();
                double meanWls = results.colAt(wlsKey).stats().mean();
                double stdWls = results.colAt(wlsKey).stats().stdDev();
                double coeffAct = coeff.equals("Alpha") ? alpha : beta;
                String title = "%s Histogram from %s OLS & WLS Regressions (n=%s)";
                String subtitle = "Actual: %.4f, Mean(OLS): %.4f, Std(OLS): %.4f, Mean(WLS): %.4f, Std(WLS): %.4f";
                chart.title().withText(String.format(title, coeff, regressionCount, n));
                chart.title().withFont(new Font("Arial", Font.BOLD, 15));
                chart.subtitle().withText(String.format(subtitle, coeffAct, meanOls, stdOls, meanWls, stdWls));
                chart.plot().axes().domain().label().withText(coeff + " Estimates");
                chart.legend().on().bottom();
                chart.writerPng(new File(String.format("./docs/images/wls/wls-%s-efficiency.png", coeff)), 700, 400, true);
                chart.show(700, 400);
            });
        });

        Thread.currentThread().join();
    }



    @Test()
    public void consistency() throws Exception {
        final double beta = 4d;
        final double alpha = 20d;
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
                final DataFrame<Integer,String> frame = sample(alpha, beta, 1, 1, n);
                final Array<Double> weights = computeWeights(frame);
                frame.regress().wls("Y", "X", weights, true, model -> {
                    final double alphaHat = model.getInterceptValue(Field.PARAMETER);
                    final double betaHat = model.getBetaValue("X", Field.PARAMETER);
                    row.setDouble(alphaKey, alphaHat);
                    row.setDouble(betaKey, betaHat);
                    return Optional.empty();
                });
            });
        });

        Array.of("Beta", "Alpha").forEach(coeff -> {
            final DataFrame<Integer,String> coeffResults = results.cols().select(col -> col.key().startsWith(coeff));
            Chart.create().withHistPlot(coeffResults, 200, true, chart -> {
                chart.plot().axes().domain().label().withText("Coefficient Estimate");
                chart.title().withText(coeff + " Histograms of " + regressionCount + " Regressions");
                chart.subtitle().withText(coeff + " Estimate distribution as sample size increases");
                chart.legend().on().bottom();
                chart.writerPng(new File(String.format("./docs/images/wls/wls-%s-consistency.png", coeff)), 700, 400, true);
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

        Chart.show(2, Collect.asList(
            Chart.create().withBarPlot(variances.getValue(0), false, chart -> {
                chart.title().withText("Beta variance with sample size");
                chart.plot().style(StatType.VARIANCE).withColor(new Color(255, 100, 100));
                chart.plot().axes().range(0).label().withText("Beta Variance");
                chart.plot().axes().domain().label().withText("Sample Size");
                chart.writerPng(new File("./docs/images/wls/wls-beta-variance.png"), 350, 200, true);
            }),
            Chart.create().withBarPlot(variances.getValue(1), false, chart -> {
                chart.title().withText("Alpha variance with sample size");
                chart.plot().style(StatType.VARIANCE).withColor(new Color(102, 204, 255));
                chart.plot().axes().range(0).label().withText("Alpha Variance");
                chart.plot().axes().domain().label().withText("Sample Size");
                chart.writerPng(new File("./docs/images/wls/wls-alpha-variance.png"), 350, 200, true);
            }))
        );

        Thread.currentThread().join();
    }



    @Test()
    public void plotCompare() throws Exception {
        final int n = 100;
        final double actualBeta = 2d;
        final double actualAlpha = 20d;
        final DataFrame<Integer,String> frame = sample(actualAlpha, actualBeta, 1, 1, n);
        final double[] x = frame.colAt("X").toDoubleStream().toArray();

        //frame.out().writeCsv(new File("/Users/witdxav/wls_dataset.csv"));

        final DataFrameLeastSquares<Integer,String> ols = frame.regress().ols("Y", "X", true, Optional::of).orElse(null);
        final double olsAlpha = ols.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
        final double olsBeta = ols.getBetaValue("X", DataFrameLeastSquares.Field.PARAMETER);
        final DataFrame<Integer,String> olsFit = createFitted(olsAlpha, olsBeta, x, "OLS");

        final Array<Double> weights = computeWeights(frame);
        final DataFrameLeastSquares<Integer,String> wls = frame.regress().wls("Y", "X", weights, true, Optional::of).orElse(null);
        System.out.println(wls);
        final double wlsAlpha = wls.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
        final double wlsBeta = wls.getBetaValue("X", DataFrameLeastSquares.Field.PARAMETER);
        final DataFrame<Integer,String> wlsFit = createFitted(wlsAlpha, wlsBeta, x, "WLS");

        Chart.create().withScatterPlot(frame, false, "X", chart -> {
            chart.plot().style("Y").withColor(Color.BLUE);
            chart.plot().<String>data().add(olsFit, "X");
            chart.plot().<String>data().add(wlsFit, "X");
            chart.plot().render(1).withLines(false, false);
            chart.plot().render(2).withLines(false, false);
            chart.title().withText("Regression with Heteroskedastic Errors (OLS vs WLS)");
            chart.title().withFont(new Font("Arial", Font.BOLD, 16));
            chart.subtitle().withText(String.format("Beta Actual: %.3f, Beta OLS: %.3f, Beta WLS: %.3f", actualBeta, olsBeta, wlsBeta));
            chart.plot().axes().domain().label().withText("X-value");
            chart.plot().axes().range(0).label().withText("Y-value");
            chart.plot().style("OLS").withColor(Color.RED).withLineWidth(2f);
            chart.plot().style("WLS").withColor(Color.GREEN).withLineWidth(2f);
            chart.legend().on().right();
            chart.show();
        });

        final DataFrame<Integer,String> copy = frame.copy();
        copy.addAll(ols.getResiduals());
        copy.cols().add("Weights", weights);
        copy.write().csv(o -> o.setFile(new File("/Users/witdxav/wls_dataset_diagnostics.csv")));

        Thread.currentThread().join();
    }


    /**
     * Returns the vector of weights for the WLS regression
     * @param frame     the frame of original data
     * @return          the weight vector for diagonal matrix in WLS
     */
    private Array<Double> computeWeights1(DataFrame<Integer,String> frame) {
        return frame.regress().ols("Y", "X", true, model -> {
            final DataFrame<Integer,String> residuals = model.getResiduals();
            final DataFrame<Integer,String> residualsAbs = residuals.mapToDoubles(v -> Math.abs(v.getDouble()));
            final DataFrame<Integer,String> fittedValues = model.getFittedValues();
            final DataFrame<Integer,String> newData = DataFrame.concatColumns(residualsAbs, fittedValues);
            return newData.regress().ols("Residuals", "Fitted", true, ols -> {
                final DataFrame<Integer,String> stdDev = ols.getFittedValues();
                final double[] weights = stdDev.colAt(0).toDoubleStream().map(v -> 1d / Math.pow(v, 2d)).toArray();
                return Optional.of(Array.of(weights));
            });
        }).orElse(null);
    }


    /**
     * Returns the vector of weights for the WLS regression by regressing |residuals| on the predictor
     * @param frame     the frame of original data
     * @return          the weight vector for diagonal matrix in WLS
     */
    private Array<Double> computeWeights(DataFrame<Integer,String> frame) {
        return frame.regress().ols("Y", "X", true, model -> {
            final DataFrame<Integer,String> residuals = model.getResiduals();
            final DataFrame<Integer,String> residualsAbs = residuals.mapToDoubles(v -> Math.abs(v.getDouble()));
            final DataFrame<Integer,String> xValues = frame.cols().select("X");
            final DataFrame<Integer,String> newData = DataFrame.concatColumns(residualsAbs, xValues);
            return newData.regress().ols("Residuals", "X", false, ols -> {
                final DataFrame<Integer,String> stdDev = ols.getFittedValues();
                final double[] weights = stdDev.colAt(0).toDoubleStream().map(v -> 1d / Math.pow(v, 2d)).toArray();
                return Optional.of(Array.of(weights));
            });
        }).orElse(null);
    }


    @Test()
    public void residualVsPredicator() throws Exception {
        final double actualBeta = 7.34d;
        final double actualAlpha = 2.51d;
        final DataFrame<Integer,String> frame = sample(actualAlpha, actualBeta, 5, 0.01, 1000);
        frame.regress().ols("Y", "X", true, model -> {
            DataFrame<Integer,String> x = frame.cols().select("X");
            DataFrame<Integer,String> residuals = model.getResiduals();
            DataFrame<Integer,String> newData = DataFrame.concatColumns(residuals, x);
            Chart.create().withScatterPlot(newData, false, "X",chart -> {
                chart.plot().style("Residuals").withPointsVisible(true);
                chart.show();
            });

            return Optional.empty();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void residualVsFitted() throws Exception {
        final double actualBeta = 7.34d;
        final double actualAlpha = 2.51d;
        final DataFrame<Integer,String> frame = sample(actualAlpha, actualBeta, 5, 0.01, 1000);
        frame.regress().ols("Y", "X", true, model -> {
            DataFrame<Integer,String> fitted = model.getFittedValues();
            DataFrame<Integer,String> residuals = model.getResiduals();
            DataFrame<Integer,String> newData = DataFrame.concatColumns(residuals, fitted);
            Chart.create().withScatterPlot(newData, false, "Fitted", chart -> {
                chart.plot().style("Residuals").withPointsVisible(true);
                chart.show();
            });

            return Optional.empty();
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
        return DataFrame.ofDoubles(Range.of(0, x.length), Array.of("X", yName), v -> {
           switch (v.colOrdinal()) {
               case 0:  return x[v.rowOrdinal()];
               default: return alpha + beta * x[v.rowOrdinal()];
           }
        });
    }


    @Test()
    public void plotWeights() throws Exception {
        final double actualBeta = 0.34d;
        final double actualAlpha = 1d;
        final DataFrame<Integer,String> frame = sample(actualAlpha, actualBeta, 1, 1, 100);
        final double[] x = frame.colAt("X").toDoubleStream().toArray();
        final Array<Double> weights1 = computeWeights1(frame);
        final DataFrame<Integer,String> weights = DataFrame.ofDoubles(Range.of(0, weights1.length()), Array.of("Weight"), v -> weights1.getDouble(v.rowOrdinal()));

        Chart.create().withLinePlot(weights, chart -> {
            chart.plot().style("Weight").withColor(Color.RED);
            chart.show();
        });

        Thread.currentThread().join();

    }




    @Test()
    public void checkInverseOfDiagonalMatrix() {
        final double[] values = IntStream.range(0,5).mapToDouble(i -> Math.random() * 10d).toArray();
        final RealMatrix matrix = new DiagonalMatrix(values);
        final RealMatrix inverse = new LUDecomposition(matrix).getSolver().getInverse();
        for (int i=0; i<inverse.getRowDimension(); ++i) {
            for (int j=0; j<inverse.getColumnDimension(); ++j) {
                final double original = matrix.getEntry(i, j);
                final double value = inverse.getEntry(i, j);
                final double expected = original == 0d ? 0d : 1d / original;
                Assert.assertEquals(value, expected, 0.000000001);
            }
        }
    }



    @Test
    public void testSuper() throws Exception {
        final DataFrame<Integer,String> frame = DataFrame.read().csv("/supervisor.csv");
        frame.regress().ols("y", "x", true, model -> {
            System.out.println(model);
            return Optional.empty();
        });

        Chart.create().withScatterPlot(frame.cols().select("y", "x"), false, "x", chart -> {
            chart.plot().style("y").withColor(Color.BLUE);
            chart.title().withText("Workers (x) vs Supervisors(y)");
            chart.subtitle().withText("Source: Regression by Example, Chatterjee & Price (1977)");
            chart.plot().axes().domain().label().withText("Worker Count");
            chart.plot().axes().range(0).label().withText("Supervisor Count");
            chart.plot().trend("y");
            chart.legend().on().bottom();
            chart.show();
        });

        Thread.currentThread().join();

    }


}
