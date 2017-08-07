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

import static com.zavtech.morpheus.util.Asserts.assertEquals;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.frame.DataFrameLeastSquares.Field;
import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.ChartShape;

/**
 * Code for OLS related documentation
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public class OLSDocs1 {

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
    public void scatterPlot() throws Exception {
        DataFrame<Integer,String> frame = loadCarDataset();
        final String y = "Horsepower";
        final String x = "EngineSize";
        DataFrame<Integer,String> xy = frame.cols().select(y, x);
        Chart.of(xy, x, Double.class, chart -> {
            chart.plot(0).withPoints();
            chart.style(y).withColor(Color.RED);
            chart.style(y).withPointsVisible(true).withPointShape(ChartShape.DIAMOND);
            chart.title().withText(y + " vs " + x);
            chart.axes().domain().label().withText(x);
            chart.axes().domain().format().withPattern("0.00;-0.00");
            chart.axes().range(0).label().withText(y);
            chart.axes().range(0).format().withPattern("0;-0");
            chart.writerPng(new File("../morpheus-docs/docs/images/ols/data-frame-ols1.png"), 845, 450);
            chart.show(845, 450);
        });

        Thread.currentThread().join();
    }


    @Test()
    public void regressPlot() throws Exception {
        DataFrame<Integer,String> frame = loadCarDataset();
        final String regressand = "Horsepower";
        final String regressor = "EngineSize";
        DataFrame<Integer,String> xy = frame.cols().select(regressand, regressor);
        Chart.of(xy, regressor, Double.class, chart -> {
            chart.plot(0).withPoints();
            chart.style(regressand).withColor(Color.RED).withPointsVisible(true).withPointShape(ChartShape.DIAMOND);
            chart.trendLine().add(regressand, regressand + " (trend)").withColor(Color.BLACK);
            chart.title().withText(regressand + " regressed on " + regressor);
            chart.subtitle().withText("Single Variable Linear Regression");
            chart.title().withFont(new Font("Verdana", Font.BOLD, 16));
            chart.axes().domain().label().withText(regressor);
            chart.axes().domain().format().withPattern("0.00;-0.00");
            chart.axes().range(0).label().withText(regressand);
            chart.axes().range(0).format().withPattern("0;-0");
            chart.writerPng(new File("../morpheus-docs/docs/images/ols/data-frame-ols2.png"), 845, 450);
            chart.show();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void ols1() {
        DataFrame<Integer,String> frame = loadCarDataset();
        final String regressand = "Horsepower";
        final String regressor = "EngineSize";
        frame.regress().ols(regressand, regressor, true, model -> {
            System.out.println(model);
            return Optional.empty();
        });

        frame.regress().ols(regressand, regressor, false, model -> {
            System.out.println(model);
            return Optional.empty();
        });

    }


    @Test()
    public void ols2() throws Exception {
        DataFrame<Integer,String> frame = loadCarDataset();
        final String regressand = "Horsepower";
        final String regressor = "EngineSize";
        frame.regress().ols(regressand, regressor, true, model -> {
            assert (model.getRegressand().equals(regressand));
            assert (model.getRegressors().size() == 1);
            assertEquals(model.getRSquared(), 0.5359992996664269, 0.00001);
            assertEquals(model.getRSquaredAdj(), 0.5309003908715525, 0.000001);
            assertEquals(model.getStdError(), 35.87167658782274, 0.00001);
            assertEquals(model.getFValue(), 105.120393642, 0.00001);
            assertEquals(model.getFValueProbability(), 0, 0.00001);
            assertEquals(model.getBetaValue("EngineSize", Field.PARAMETER), 36.96327914, 0.0000001);
            assertEquals(model.getBetaValue("EngineSize", Field.STD_ERROR), 3.60518041, 0.0000001);
            assertEquals(model.getBetaValue("EngineSize", Field.T_STAT), 10.25282369, 0.0000001);
            assertEquals(model.getBetaValue("EngineSize", Field.P_VALUE), 0.0000, 0.0000001);
            assertEquals(model.getBetaValue("EngineSize", Field.CI_LOWER), 29.80203113, 0.0000001);
            assertEquals(model.getBetaValue("EngineSize", Field.CI_UPPER), 44.12452714, 0.0000001);
            assertEquals(model.getInterceptValue(Field.PARAMETER), 45.21946716, 0.0000001);
            assertEquals(model.getInterceptValue(Field.STD_ERROR), 10.31194906, 0.0000001);
            assertEquals(model.getInterceptValue(Field.T_STAT), 4.3851523, 0.0000001);
            assertEquals(model.getInterceptValue(Field.P_VALUE), 0.00003107, 0.0000001);
            assertEquals(model.getInterceptValue(Field.CI_LOWER), 24.73604714, 0.0000001);
            assertEquals(model.getInterceptValue(Field.CI_UPPER), 65.70288719, 0.0000001);
            System.out.println(model);
            return Optional.of(model);
        });
    }


    @Test()
    public void ols3() throws Exception {
        DataFrame<Integer,String> frame = loadCarDataset();
        final String regressand = "Horsepower";
        final String regressor = "EngineSize";
        frame.regress().ols(regressand, regressor, false, model -> {
            assert (model.getRegressand().equals(regressand));
            assert (model.getRegressors().size() == 1);
            assertEquals(model.getRSquared(), 0.934821940829, 0.00001);
            assertEquals(model.getRSquaredAdj(), 0.9341134836, 0.000001);
            assertEquals(model.getStdError(), 39.26510834, 0.00001);
            assertEquals(model.getFValue(), 1319.517942852, 0.00001);
            assertEquals(model.getFValueProbability(), 0, 0.00001);
            assertEquals(model.getBetaValue("EngineSize", Field.PARAMETER), 51.708176166, 0.0000001);
            assertEquals(model.getBetaValue("EngineSize", Field.STD_ERROR), 1.4234806556, 0.0000001);
            assertEquals(model.getBetaValue("EngineSize", Field.T_STAT), 36.32516955, 0.0000001);
            assertEquals(model.getBetaValue("EngineSize", Field.P_VALUE), 5.957E-56, 0.0000001);
            assertEquals(model.getBetaValue("EngineSize", Field.CI_LOWER), 48.880606712, 0.0000001);
            assertEquals(model.getBetaValue("EngineSize", Field.CI_UPPER), 54.53574562, 0.0000001);
            System.out.println(model);
            return Optional.of(model);
        });
    }


    @Test()
    public void frontExample() throws Exception {

        //Load the data
        DataFrame<Integer,String> data = DataFrame.read().csv(options -> {
            options.setResource("http://zavtech.com/data/samples/cars93.csv");
            options.setExcludeColumnIndexes(0);
        });

        //Run OLS regression
        String regressand = "Horsepower";
        String regressor = "EngineSize";
        data.regress().ols(regressand, regressor, true, model -> {
            System.out.println(model);
            DataFrame<Integer,String> xy = data.cols().select(regressand, regressor);
            Chart.of(xy, regressor, Double.class, chart -> {
                chart.plot(0).withPoints();
                chart.style(regressand).withColor(Color.RED).withPointsVisible(true);
                chart.trendLine().add(regressand, regressand + " (trend)").withColor(Color.BLACK);
                chart.title().withText(regressand + " regressed on " + regressor);
                chart.subtitle().withText("Single Variable Linear Regression");
                chart.axes().domain().label().withText(regressor);
                chart.axes().domain().format().withPattern("0.00;-0.00");
                chart.axes().range(0).label().withText(regressand);
                chart.axes().range(0).format().withPattern("0;-0");
                chart.writerPng(new File("../morpheus-docs/docs/images/ols/data-frame-ols.png"), 845, 450);
                chart.show();
            });
            return Optional.empty();
        });

        Thread.currentThread().join();
    }



}
