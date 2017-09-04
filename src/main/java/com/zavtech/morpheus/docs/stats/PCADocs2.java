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
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

import org.apache.commons.math3.linear.RealMatrix;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.frame.DataFramePCA;
import com.zavtech.morpheus.frame.DataFramePCA.Field;
import com.zavtech.morpheus.jama.Matrix;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.stats.StatType;
import com.zavtech.morpheus.util.Collect;
import com.zavtech.morpheus.viz.chart.Chart;

public class PCADocs2 {


    @Test()
    public void test() {
        DataFrame<Integer,Integer> frame = DataFrame.ofImage(getClass().getResource("/poppet.jpg"));
        Stream.of(DataFramePCA.Solver.EVD_COV, DataFramePCA.Solver.SVD).forEach(solver -> {
            frame.transpose().pca().apply(true, solver, model -> {
                model.getEigenValues().out().print();
                model.getEigenVectors().out().print();
                model.getProjection(10).out().print();
                return Optional.empty();
            });
        });
    }


    @Test()
    public void processImage() throws Exception {

        //Load image from classpath
        URL url = getClass().getResource("/poppet.jpg");

        //Re-create PCA reduced image while retaining different number of principal components
        Array.of(5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 360).forEach(nComp -> {

            //Initialize the **transpose** of image as we need nxp frame where n >= p
            DataFrame<Integer,Integer> rgbFrame = DataFrame.ofImage(url).transpose();

            //Create 3 frames from RGB data, one for red, green and blue
            DataFrame<Integer,Integer> red = rgbFrame.mapToDoubles(v -> (v.getInt() >> 16) & 0xFF);
            DataFrame<Integer,Integer> green = rgbFrame.mapToDoubles(v -> (v.getInt() >> 8) & 0xFF);
            DataFrame<Integer,Integer> blue = rgbFrame.mapToDoubles(v -> v.getInt() & 0xFF);

            //Perform PCA on each color frame, and project using only first N principal components
            Stream.of(red, green, blue).parallel().forEach(color -> {
                color.pca().apply(true, model -> {
                    DataFrame<Integer,Integer> projection = model.getProjection(nComp);
                    projection.cap(true).doubles(0, 255);  //cap values between 0 and 255
                    color.update(projection, false, false);
                    return null;
                });
            });

            //Apply reduced RBG values onto the original frame so we don't need to allocate memory
            rgbFrame.applyInts(v -> {
                int i = v.rowOrdinal();
                int j = v.colOrdinal();
                int r = (int)red.data().getDouble(i,j);
                int g = (int)green.data().getDouble(i,j);
                int b = (int)blue.data().getDouble(i,j);
                return ((0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF));
            });

            //Create reduced image from **transpose** of the DataFrame to get back original orientation
            int width = rgbFrame.rowCount();
            int height = rgbFrame.colCount();
            BufferedImage transformed = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            rgbFrame.forEachValue(v -> {
                int i = v.colOrdinal();
                int j = v.rowOrdinal();
                int rgb = v.getInt();
                transformed.setRGB(j, i, rgb);
            });

            try {
                File outputfile = new File("/Users/witdxav/temp/poppet-" + nComp + ".jpg");
                outputfile.getParentFile().mkdirs();
                ImageIO.write(transformed, "jpg", outputfile);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to record image result", ex);
            }
        });
    }



    @Test()
    public void variancePlot() throws Exception {

        URL url = getClass().getResource("/poppet.jpg");
        DataFrame<Integer,Integer> rgbFrame = DataFrame.ofImage(url);
        Range<Integer> rowKeys = Range.of(0, rgbFrame.rowCount());

        DataFrame<Integer,String> result = DataFrame.ofDoubles(rowKeys, Array.of("Red", "Green", "Blue"));
        Collect.<String,DataFrame<Integer,Integer>>asMap(mapping -> {
            mapping.put("Red", rgbFrame.mapToDoubles(v -> (v.getInt() >> 16) & 0xFF));
            mapping.put("Green", rgbFrame.mapToDoubles(v -> (v.getInt() >> 8) & 0xFF));
            mapping.put("Blue", rgbFrame.mapToDoubles(v -> v.getInt() & 0xFF));
        }).forEach((name, color) -> {
            color.transpose().pca().apply(true, model -> {
                DataFrame<Integer,Field> eigenFrame = model.getEigenValues();
                DataFrame<Integer,Field> varPercent = eigenFrame.cols().select(Field.VAR_PERCENT);
                result.update(varPercent.cols().mapKeys(k -> name), false, false);
                return Optional.empty();
            });
        });

        DataFrame<Integer,String> chartData = result.rows().select(c -> c.ordinal() < 10).copy();
        Chart.create().withBarPlot(chartData.rows().mapKeys(r -> String.valueOf(r.ordinal())), false, chart -> {
            chart.plot().style("Red").withColor(Color.RED);
            chart.plot().style("Green").withColor(Color.GREEN);
            chart.plot().style("Blue").withColor(Color.BLUE);
            chart.plot().axes().range(0).label().withText("Percent of Variance");
            chart.plot().axes().domain().label().withText("Principal Component");
            chart.title().withText("Eigen Spectrum (Percent of Explained Variance)");
            chart.legend().on().bottom();
            chart.writerPng(new File("./docs/images/pca/poppet-explained-variance.png"), 700, 400, true);
            chart.show();
        });

        Thread.currentThread().join();
    }



    Matrix toJama(DataFrame<?,?> frame) {
        Matrix matrix = new Matrix(frame.rowCount(), frame.colCount());
        frame.forEachValue(v -> matrix.set(v.rowOrdinal(), v.colOrdinal(), v.getDouble()));
        return matrix;
    }


    @Test()
    public void filesizes() throws Exception {
        final File home = new File("/Users/witdxav");
        final Range<Integer> rowKeys = Range.of(1, 61);
        final DataFrame<Integer,String> frame = DataFrame.ofDoubles(rowKeys, Array.of("Size"), v -> {
            final File file = new File(home, "cutedog-" + v.rowKey() + ".jpg");
            return (double)file.length();
        });

        Chart.create().withBarPlot(frame, false, chart -> {
            chart.plot().data().at(0).withLowerDomainInterval(v -> v + 1);
            chart.show();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void scores1() {
        URL url = getClass().getResource("/poppet.jpg");
        DataFrame<Integer,Integer> image = DataFrame.ofImage(url).transpose();
        DataFrame<Integer,Integer> red = image.mapToDoubles(v -> (v.getInt() >> 16) & 0xFF);
        red.pca().apply(true, model -> {
            DataFrame<Integer,Integer> scores = model.getScores();
            Assert.assertEquals(scores.rowCount(), 504);
            Assert.assertEquals(scores.colCount(), 360);
            scores.out().print();
            return Optional.empty();
        });
    }


    @Test()
    public void scores2() {
        URL url = getClass().getResource("/poppet.jpg");
        DataFrame<Integer,Integer> image = DataFrame.ofImage(url).transpose();
        DataFrame<Integer,Integer> red = image.mapToDoubles(v -> (v.getInt() >> 16) & 0xFF);
        red.pca().apply(true, model -> {
            DataFrame<Integer,Integer> scores = model.getScores(10);
            Assert.assertEquals(scores.rowCount(), 504);
            Assert.assertEquals(scores.colCount(), 10);
            scores.out().print();
            return Optional.empty();
        });
    }

    @Test()
    public void projection1() {
        URL url = getClass().getResource("/poppet.jpg");
        DataFrame<Integer,Integer> image = DataFrame.ofImage(url).transpose();
        DataFrame<Integer,Integer> red = image.mapToDoubles(v -> (v.getInt() >> 16) & 0xFF);
        red.pca().apply(true, model -> {
            DataFrame<Integer,Integer> projection = model.getProjection(10);
            Assert.assertEquals(projection.rowCount(), 504);
            Assert.assertEquals(projection.colCount(), 360);
            projection.out().print();
            return Optional.empty();
        });
    }


    /**
     * Returns a DataFrame representation of a matrix
     * @param matrix    the matrix reference
     * @return          the DataFrame representation
     */
    DataFrame<Integer,Integer> toDataFrame(RealMatrix matrix) {
        final Range<Integer> rowKeys = Range.of(0, matrix.getRowDimension());
        final Range<Integer> colKeys = Range.of(0, matrix.getColumnDimension());
        return DataFrame.ofDoubles(rowKeys, colKeys, v -> matrix.getEntry(v.rowOrdinal(), v.colOrdinal()));
    }


    @Test()
    public void shift() {
        System.out.println(Integer.MAX_VALUE);
        System.out.println(255 >> 4);
        System.out.println(0xFF);
    }


}
