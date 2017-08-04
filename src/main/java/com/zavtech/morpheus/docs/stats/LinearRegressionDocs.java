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
import java.time.Year;
import java.util.Optional;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.testng.annotations.Test;

import static com.zavtech.morpheus.util.Asserts.assertEquals;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.frame.DataFrameLeastSquares.Field;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.util.Bounds;
import com.zavtech.morpheus.viz.chart.Chart;

public class LinearRegressionDocs {

    /**
     * Returns a DataFrame of motor vehicles features
     * @return  the frame of motor vehicle features
     */
    static DataFrame<Integer,String> loadCarDataset() {
        return DataFrame.read().csv(options -> {
            options.setResource("http://zavtech.com/data/samples/cars93.csv");
            options.setExcludeColumnIndexes(0);
        });
    }


    @Test()
    public void ols1() throws Exception {
        DataFrame<Integer,String> frame = loadCarDataset();
        frame.regress().ols("Price", "Horsepower", true, model -> {
            model.withAlpha(0.05d);
            assert (model.getRegressand().equals("Price"));
            assert (model.getRegressors().size() == 1);
            assertEquals(model.getRSquared(), 0.6212869500338227, 0.00001);
            assertEquals(model.getRSquaredAdj(), 0.6171252681660625, 0.000001);
            assertEquals(model.getStdError(), 5.976952882230454, 0.00001);
            assertEquals(model.getBetaValue("Horsepower", Field.PARAMETER), 0.14537123, 0.0000001);
            assertEquals(model.getBetaValue("Horsepower", Field.STD_ERROR), 0.0118978, 0.0000001);
            assertEquals(model.getBetaValue("Horsepower", Field.T_STAT), 12.2183251, 0.0000001);
            assertEquals(model.getBetaValue("Horsepower", Field.P_VALUE), 0.0000, 0.0000001);
            assertEquals(model.getBetaValue("Horsepower", Field.CI_LOWER), 0.1217377, 0.0000001);
            assertEquals(model.getBetaValue("Horsepower", Field.CI_UPPER), 0.16900475, 0.0000001);
            assertEquals(model.getInterceptValue(Field.PARAMETER), -1.39876912, 0.0000001);
            assertEquals(model.getInterceptValue(Field.STD_ERROR), 1.82001642, 0.0000001);
            assertEquals(model.getInterceptValue(Field.T_STAT), -0.76854753, 0.0000001);
            assertEquals(model.getInterceptValue(Field.P_VALUE), 0.44415195, 0.0000001);
            assertEquals(model.getInterceptValue(Field.CI_LOWER), -5.01400816, 0.0000001);
            assertEquals(model.getInterceptValue(Field.CI_UPPER), 2.21646991, 0.0000001);
            System.out.println(model);
            return Optional.of(model);
        });
    }

    @Test()
    public void residualAnalysis() throws Exception {
        final DataFrame<Integer,String> frame = loadCarDataset();
        final DataFrame<Integer,Integer> omega = estimateCarDatasetOmega(frame);
        frame.regress().gls("Price", "Horsepower", omega, true, model -> {
            System.out.println(model);
            return Optional.empty();
        });
    }



    private DataFrame<Integer,Integer> estimateCarDatasetOmega(DataFrame<Integer,String> dataset) {
        return dataset.regress().ols("Price", "Horsepower", true, model -> {
            final DataFrame<Integer,String> residuals = model.getResiduals();
            final DataFrame<Integer,String> horsepower = dataset.cols().select("Horsepower");
            final DataFrame<Integer,String> combined = DataFrame.concatColumns(horsepower, residuals);
            final double minBhp = horsepower.stats().min();
            final double maxBhp = horsepower.stats().max();
            final double stepSize = (maxBhp - minBhp) / 3d;
            final Range<Bounds<Double>> bounds = Range.of(minBhp, maxBhp, stepSize).map(i -> Bounds.of(i, i + stepSize));
            final DataFrame<Bounds<Double>,String> variances = DataFrame.ofDoubles(bounds, Array.of("Variance"), v -> {
                final Bounds<Double> bhpBounds = v.rowKey();
                final double lowerBhp = bhpBounds.lower();
                final double upperBhp = bhpBounds.upper();
                return combined.rows().select(row -> {
                    final double bhp = row.getDouble("Horsepower");
                    return bhp >= lowerBhp && bhp < upperBhp;
                }).colAt("Residuals").stats().variance();
            });
            variances.cols().add("MidPoint", Double.class, v -> v.rowKey().lower() + (v.rowKey().upper() - v.rowKey().lower()) / 2d);
            return variances.regress().ols("Variance", "MidPoint", true, model2 -> {
                final Array<Integer> keys = dataset.rows().keyArray();
                final double slope = model2.getBetaValue("MidPoint", Field.PARAMETER);
                return Optional.of(DataFrame.ofDoubles(keys, keys, v -> {
                    if (v.rowOrdinal() == v.colOrdinal()) {
                        final Integer rowKey = v.rowKey();
                        final double bhp = dataset.data().getDouble(rowKey, "Horsepower");
                        final double impliedVariance = slope * bhp;
                        return 1d / impliedVariance;
                    } else {
                        return 0d;
                    }
                }));
            });
        }).orElse(null);
    }



    @Test()
    public void carPrice1() throws Exception {
        DataFrame<Integer,String> frame = loadCarDataset();
        final String regressand = "Price";
        final String regressor = "Horsepower";
        DataFrame<Integer,String> xy = frame.cols().select(regressand, regressor);
        Chart.of(xy, regressor, Double.class,  chart -> {
            chart.plot(0).withPoints();
            chart.style(regressand).withColor(Color.RED).withPointsVisible(true).withLineWidth(1f);
            chart.trendLine().add(regressand, regressand + " (trend)").withColor(Color.BLACK);
            chart.title().withText(regressand + " vs " + regressor);
            chart.subtitle().withText("Single Variable Linear Regression");
            chart.title().withFont(new Font("Verdana", Font.BOLD, 16));
            chart.axes().domain().label().withText(regressor);
            chart.axes().domain().format().withPattern("0.00;-0.00");
            chart.axes().range(0).label().withText("Price / 100");
            chart.axes().range(0).format().withPattern("0.00;-0.00");
            chart.legend().on().bottom();
            chart.writerPng(new File("../morpheus-docs/docs/images/frame/data-frame-ols1.png"), 845, 450);
            chart.show();
        });

        Thread.currentThread().join();
    }



    @Test()
    public void ols2() {
        String regressand = "Price";
        Iterable<String> regressors = Array.of("Horsepower", "Weight");
        DataFrame<Integer,String> frame = loadCarDataset();
        frame.regress().ols(regressand, regressors, true, model -> {
            assert (model.getRegressand().equals("Price"));
            assert (model.getRegressors().size() == 2);
            assertEquals(model.getRSquared(), 0.6305453858523555, 0.00001);
            assertEquals(model.getRSquaredAdj(), 0.6223352833157412, 0.000001);
            assertEquals(model.getStdError(), 5.936147528912534, 0.00001);
            assertEquals(model.getBetaValue("Horsepower", Field.PARAMETER), 0.12591702, 0.0000001);
            assertEquals(model.getBetaValue("Horsepower", Field.STD_ERROR), 0.0175339, 0.0000001);
            assertEquals(model.getBetaValue("Horsepower", Field.T_STAT), 7.1813481, 0.0000001);
            assertEquals(model.getBetaValue("Horsepower", Field.P_VALUE), 0.0000, 0.0000001);
            assertEquals(model.getBetaValue("Horsepower", Field.CI_LOWER), 0.09108288, 0.0000001);
            assertEquals(model.getBetaValue("Horsepower", Field.CI_UPPER), 0.16075117, 0.0000001);
            assertEquals(model.getBetaValue("Weight", Field.PARAMETER), 0.00233793, 0.0000001);
            assertEquals(model.getBetaValue("Weight", Field.STD_ERROR), 0.00155676, 0.0000001);
            assertEquals(model.getBetaValue("Weight", Field.T_STAT), 1.50179107, 0.0000001);
            assertEquals(model.getBetaValue("Weight", Field.P_VALUE), 0.1366516, 0.0000001);
            assertEquals(model.getBetaValue("Weight", Field.CI_LOWER), -0.00075485, 0.0000001);
            assertEquals(model.getBetaValue("Weight", Field.CI_UPPER), 0.00543071, 0.0000001);
            assertEquals(model.getInterceptValue(Field.PARAMETER), -5.78494037, 0.0000001);
            assertEquals(model.getInterceptValue(Field.STD_ERROR), 3.43474103, 0.0000001);
            assertEquals(model.getInterceptValue(Field.T_STAT), -1.68424353, 0.0000001);
            assertEquals(model.getInterceptValue(Field.P_VALUE), 0.09559984, 0.0000001);
            assertEquals(model.getInterceptValue(Field.CI_LOWER), -12.60865293, 0.0000001);
            assertEquals(model.getInterceptValue(Field.CI_UPPER), 1.0387722, 0.0000001);
            System.out.println(model);
            return Optional.empty();
        });
    }


    /**
     Beta=0.145371226711863
     Beta SE=0.011897803134444768
     Beta CI Lower=0.12173770237918513
     Beta CI Upper=0.16900475104454088
     Intercept=-1.3987691236331157
     Intercept SE=1.8200164195205977
     */
    @Test()
    public void olsApache() {
        final DataFrame<Integer,String> frame = loadCarDataset();
        final SimpleRegression regression = new SimpleRegression(true);
        frame.rows().forEach(row -> regression.addData(row.getDouble("Horsepower"), row.getDouble("Price")));
        System.out.printf("\nBeta=%s", regression.getSlope());
        System.out.printf("\nBeta SE=%s", regression.getSlopeStdErr());
        System.out.printf("\nBeta CI Lower=%s", regression.getSlope() - regression.getSlopeConfidenceInterval());
        System.out.printf("\nBeta CI Upper=%s", regression.getSlope() + regression.getSlopeConfidenceInterval());
        System.out.printf("\nIntercept=%s", regression.getIntercept());
        System.out.printf("\nIntercept SE=%s", regression.getInterceptStdErr());
    }

    @Test()
    public void olsViz() {
        DataFrame<Integer,String> frame = loadCarDataset();
        frame.out().print();
    }


    @Test()
    public void whiteNoise() {
        Range<Integer> rowKeys = Range.of(0, 1000000);
        Array<String> colKeys = Array.of("X", "Y");
        DataFrame<Integer,String> frame = DataFrame.ofDoubles(rowKeys, colKeys, v -> Math.random());
    }


    @Test()
    public void testLongley() throws Exception {
        DataFrame<Year,String> frame =  longley();
        frame.out().print(100);
        frame.cols().stats().correlation().out().print(100);
        final String regressand = "Employed";
        final Iterable<String> regressors = Array.of("GNP", "Population");
        frame.regress().ols(regressand, regressors, true, model -> {
            System.out.println(model);
            return Optional.empty();
        });
    }


    @Test()
    public void longleyCollinearity() throws Exception {
        DataFrame<Year,String> frame =  longley();
        DataFrame<Year,String> xy = frame.cols().select("GNP", "Population");
        Chart.of(xy, "Population", Double.class,  chart -> {
            chart.plot(0).withPoints();
            chart.style("GNP").withColor(Color.RED).withPointsVisible(true).withLineWidth(1f);
            chart.trendLine().add("GNP", "GNP" + " (trend)").withColor(Color.BLACK);
            chart.subtitle().withText("Single Variable Linear Regression");
            chart.title().withFont(new Font("Verdana", Font.BOLD, 16));
            chart.axes().domain().format().withPattern("0.00;-0.00");
            chart.axes().range(0).label().withText("Price / 100");
            chart.axes().range(0).format().withPattern("0.00;-0.00");
            chart.legend().on().bottom();
            //chart.writerPng(new File("../morpheus-docs/docs/images/frame/data-frame-ols1.png"), 845, 450);
            chart.show();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void longleyCorrelation() throws Exception {
        final DataFrame<Year,String> frame = longley();
        final DataFrame<Year,String> variables = frame.cols().select("GNP", "Employed", "Population");
        final DataFrame<String,String> correlMatrix = variables.cols().stats().correlation();
        correlMatrix.out().print();
    }


    @Test()
    public void longleyOlsResiduals() throws Exception {
        final DataFrame<Year,String> frame = longley();
        final String regressand = "Employed";
        final Iterable<String> regressors = Array.of("GNP", "Population");
        frame.regress().ols(regressand, regressors, true, model -> {
            final DataFrame<Year,String> residuals = model.getResiduals();
            final Range<Integer> lags = Range.of(0, 8);
            final DataFrame<Integer,String> autoCorr = DataFrame.ofDoubles(lags, Array.of("Residual(ACF)"), v -> {
                final int lag = v.rowKey();
                return residuals.colAt(0).stats().autocorr(lag);
            });
            autoCorr.out().print();

            final DataFrame<Year,String> fittedValues = model.getFittedValues();
            final DataFrame<Year,String> combined = DataFrame.concatColumns(residuals, fittedValues);
            Chart.of(combined, "Fitted", Double.class,  chart -> {
                chart.plot(0).withPoints();
                chart.style("Residuals").withColor(Color.RED).withPointsVisible(true).withLineWidth(1f);
                chart.subtitle().withText("Single Variable Linear Regression");
                chart.title().withFont(new Font("Verdana", Font.BOLD, 16));
                chart.axes().domain().format().withPattern("0.00;-0.00");
                chart.axes().range(0).label().withText("Price / 100");
                chart.axes().range(0).format().withPattern("0.00;-0.00");
                chart.legend().on().bottom();
                //chart.writerPng(new File("../morpheus-docs/docs/images/frame/data-frame-ols1.png"), 845, 450);
                chart.show();
            });
            return Optional.empty();
        });
        Thread.currentThread().join();
    }



    @Test()
    public void longleyCorrelogramOfResiduals() throws Exception {
        final DataFrame<Year,String> frame = longley();
        final String regressand = "Employed";
        final Iterable<String> regressors = Array.of("GNP", "Population");
        frame.regress().ols(regressand, regressors, true, model -> {
            final DataFrame<Integer, String> autoCorr = model.getResidualsAcf(8);
            autoCorr.out().print();

            Array<Integer> domain = autoCorr.rows().keyArray();
            DataFrame<Integer,String> bounds = DataFrame.ofDoubles(domain, Array.of("Upper", "Lower"), v -> {
                return v.colOrdinal() == 0 ? 0.2d : -0.2d;
            });

            bounds.out().print(100);

            Chart.of(autoCorr, chart -> {
                chart.data().add(bounds);
                chart.plot(0).withBars(0d);
                chart.plot(1).withLines();
                chart.title().withText("Autocorrelation Function of Residuals");
                chart.title().withFont(new Font("Arial", Font.PLAIN, 14));
                chart.subtitle().withText("Upper/Lower Bounds at alpha = +/- 0.2");
                chart.axes().domain().label().withText("Lag");
                chart.axes().range(0).label().withText("Autocorrelation");
                chart.axes().domain().withRange(Bounds.of(-0.25, 8));
                chart.style("Residual(ACF)").withColor(Color.RED);
                chart.style("Upper").withColor(Color.BLUE).withDashes(true).withLineWidth(1f);
                chart.style("Lower").withColor(Color.BLUE).withDashes(true).withLineWidth(1f);
                chart.show();
            });

            return Optional.empty();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void longleyCorrelogramOfResiduals2() throws Exception {
        final DataFrame<Year,String> frame = longley();
        final String regressand = "Employed";
        final Iterable<String> regressors = Array.of("GNP", "Population");
        frame.regress().ols(regressand, regressors, true, model -> {
            Chart.acf(model, 8, 0.2d, chart -> {
                chart.title().withText("Autocorrelation Function of Residuals");
                chart.title().withFont(new Font("Arial", Font.PLAIN, 14));
                chart.subtitle().withText("Upper/Lower Bounds at alpha = +/- 0.2");
                chart.writerPng(new File("../morpheus-docs/docs/images/frame/data-frame-gls-acf1.png"), 600, 320);
                chart.show();
            });
            return Optional.empty();
        });
        Thread.currentThread().join();
    }



    @Test()
    public void longleyAutocorrelation() throws Exception {
        final Range<Integer> lags = Range.of(0, 8);
        final DataFrame<Year,String> frame = longley().cols().select("GNP", "Employed", "Population");
        final DataFrame<Integer,String> autoCorr = DataFrame.ofDoubles(lags, frame.cols().keyArray(), v -> {
            final int lag = v.rowKey();
            final String colKey = v.colKey();
            return frame.colAt(colKey).stats().autocorr(lag);
        });
        autoCorr.out().print();
    }


    @Test()
    public void arData() throws Exception {

        Range<Integer> rowKeys = Range.of(0, 100);
        DataFrame<Integer,String> data = DataFrame.ofDoubles(rowKeys, Array.of("X", "Y"), v -> {
           switch (v.colOrdinal()) {
               case 0:  return (double)v.rowKey();
               case 1:  return Math.sin(v.rowKey()) + 0.35 * v.rowKey() + 2d;
               default: throw new IllegalArgumentException("Unexpected column ordinal");
           }
        });

        data.regress().ols("Y", "X", true, model -> {
            System.out.println(model);
            Chart.acf(model, 20, 0.2, chart -> {
                chart.show();
            });
            return Optional.empty();
        });



        /*
        Chart.of(data, "X", Double.class, chart -> {
            chart.plot(0).withLines();
            chart.title().withText("Model");
            chart.style("Y").withColor(Color.RED);
            chart.trendLine().add("Y", "Y(ols)");
            chart.show();
        });
         */



        Thread.currentThread().join();
    }



    public static DataFrame<Year,String> longley() {
        return DataFrame.read().csv(options -> {
            options.setHeader(true);
            options.setExcludeColumnIndexes(0);
            options.setResource("https://vincentarelbundock.github.io/Rdatasets/csv/datasets/longley.csv");
            options.setRowKeyParser(Year.class, values -> Year.of(Integer.parseInt(values[0])));
        });
    }
}
