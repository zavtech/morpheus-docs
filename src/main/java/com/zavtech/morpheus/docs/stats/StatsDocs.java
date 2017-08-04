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
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.frame.DataFrameRow;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.stats.StatType;
import com.zavtech.morpheus.stats.Stats;
import com.zavtech.morpheus.util.PerfStat;
import com.zavtech.morpheus.viz.chart.Chart;

public class StatsDocs {


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


    /**
     * Returns a frame of random double precision values
     * @param rowCount  the row count for frame
     * @param columns   the column keys
     * @return          the newly created frame
     */
    static DataFrame<LocalDate,String> random(int rowCount, String... columns) {
        final LocalDate start = LocalDate.now().minusDays(rowCount);
        return DataFrame.ofDoubles(
            Range.of(0, rowCount).map(start::plusDays),
            Array.of(columns),
            value -> Math.random() * 100d
        );
    }


    @Test()
    public void frameStats() {

        //Create 100x5 DataFrame of random doubles
        DataFrame<LocalDate,String> frame = random(100, "A", "B", "C", "D", "E");

        frame.out().print();

        System.out.println("\n");
        System.out.printf("Count = %.4f\n", frame.stats().count());
        System.out.printf("Minimum = %.4f\n", frame.stats().min());
        System.out.printf("Maximum = %.4f\n", frame.stats().max());
        System.out.printf("Mean = %.4f\n", frame.stats().mean());
        System.out.printf("Median = %.4f\n", frame.stats().median());
        System.out.printf("Variance = %.4f\n", frame.stats().variance());
        System.out.printf("StdDev = %.4f\n", frame.stats().stdDev());
        System.out.printf("Skew = %.4f\n", frame.stats().skew());
        System.out.printf("Kurtosis = %.4f\n", frame.stats().kurtosis());
        System.out.printf("Mean Abs Deviation = %.4f\n", frame.stats().mad());
        System.out.printf("Sum = %.4f\n", frame.stats().sum());
        System.out.printf("Sum of Squares = %.4f\n", frame.stats().sumSquares());
        System.out.printf("Std Error of Mean = %.4f\n", frame.stats().sem());
        System.out.printf("Geometric Mean = %.4f\n", frame.stats().geoMean());
        System.out.printf("Percentile(75th) = %.4f\n", frame.stats().percentile(0.75d));
        System.out.printf("Autocorrelation(2) = %.4f\n", frame.stats().autocorr(2));

        DataFrame<String,StatType> count = frame.cols().stats().count();
        DataFrame<String,StatType> min = frame.cols().stats().min();
        DataFrame<String,StatType> max = frame.cols().stats().max();
        DataFrame<String,StatType> mean = frame.cols().stats().mean();
        DataFrame<String,StatType> median = frame.cols().stats().median();
        DataFrame<String,StatType> variance = frame.cols().stats().variance();
        DataFrame<String,StatType> stdDev = frame.cols().stats().stdDev();
        DataFrame<String,StatType> kurtosis = frame.cols().stats().kurtosis();
        DataFrame<String,StatType> mad = frame.cols().stats().mad();
        DataFrame<String,StatType> sum = frame.cols().stats().sum();
        DataFrame<String,StatType> sumSquares = frame.cols().stats().sumSquares();
        DataFrame<String,StatType> sem = frame.cols().stats().sem();
        DataFrame<String,StatType> geoMean = frame.cols().stats().geoMean();

        frame.cols().describe(
            StatType.COUNT,
            StatType.MIN,
            StatType.MAX,
            StatType.MEAN,
            StatType.VARIANCE,
            StatType.STD_DEV,
            StatType.KURTOSIS
        ).out().print();
    }

    @Test()
    public void rowStats1() {

        //Create 100x5 DataFrame of random doubles
        DataFrame<LocalDate,String> frame = random(100, "A", "B", "C", "D", "E");
        //Capture stats interface for two rows independently sourced by ordinal and key
        LocalDate date = frame.rows().key(3);
        Stats<Double> stats1 = frame.rowAt(3).stats();
        Stats<Double> stats2 = frame.rowAt(date).stats();

        StatType.univariate().forEach(statType -> {
            switch (statType) {
                case COUNT:         assert(stats1.count().doubleValue() == stats2.count());                     break;
                case MIN:           assert(stats1.min().doubleValue() == stats2.min());                         break;
                case MAX:           assert(stats1.max().doubleValue() == stats2.max());                         break;
                case MEAN:          assert(stats1.mean().doubleValue() == stats2.mean());                       break;
                case MEDIAN:        assert(stats1.median().doubleValue() == stats2.median());                   break;
                case VARIANCE:      assert(stats1.variance().doubleValue() == stats2.variance());               break;
                case STD_DEV:       assert(stats1.stdDev().doubleValue() == stats2.stdDev());                   break;
                case KURTOSIS:      assert(stats1.kurtosis().doubleValue() == stats2.kurtosis());               break;
                case MAD:           assert(stats1.mad().doubleValue() == stats2.mad());                         break;
                case SEM:           assert(stats1.sem().doubleValue() == stats2.sem());                         break;
                case GEO_MEAN:      assert(stats1.geoMean().doubleValue() == stats2.geoMean());                 break;
                case SUM:           assert(stats1.sum().doubleValue() == stats2.sum());                         break;
                case SUM_SQUARES:   assert(stats1.sumSquares().doubleValue() == stats2.sumSquares());           break;
                case AUTO_CORREL:   assert(stats1.autocorr(2).doubleValue() == stats2.autocorr(2));             break;
                case PERCENTILE:    assert(stats1.percentile(0.75).doubleValue() == stats2.percentile(0.75));   break;
            }
        });
    }


    @Test()
    public void rowStats() {

        DataFrame<LocalDate,String> frame = random(10, "A", "B", "C", "D", "E");

        frame.out().print();

        DataFrame<LocalDate,StatType> count = frame.rows().stats().count();
        DataFrame<LocalDate,StatType> min = frame.rows().stats().min();
        DataFrame<LocalDate,StatType> max = frame.rows().stats().max();
        DataFrame<LocalDate,StatType> mean = frame.rows().stats().mean();
        DataFrame<LocalDate,StatType> median = frame.rows().stats().median();
        DataFrame<LocalDate,StatType> variance = frame.rows().stats().variance();
        DataFrame<LocalDate,StatType> stdDev = frame.rows().stats().stdDev();
        DataFrame<LocalDate,StatType> kurtosis = frame.rows().stats().kurtosis();
        DataFrame<LocalDate,StatType> mad = frame.rows().stats().mad();
        DataFrame<LocalDate,StatType> sum = frame.rows().stats().sum();
        DataFrame<LocalDate,StatType> sumLogs = frame.rows().stats().sumLogs();
        DataFrame<LocalDate,StatType> sumSquares = frame.rows().stats().sumSquares();
        DataFrame<LocalDate,StatType> sem = frame.rows().stats().sem();
        DataFrame<LocalDate,StatType> geoMean = frame.rows().stats().geoMean();
        DataFrame<LocalDate,StatType> autocorr = frame.rows().stats().autocorr(1);
        DataFrame<LocalDate,StatType> percentile = frame.rows().stats().percentile(0.75d);

        DataFrame<LocalDate,StatType> rowStats = frame.rows().describe(
            StatType.COUNT, StatType.MEAN, StatType.VARIANCE, StatType.SKEWNESS, StatType.SUM
        );
        rowStats.out().print();
    }

    @Test()
    public void rowDemean() throws Exception {

        //Create a 1,000,000x10 DataFrame of random double precision values
        DataFrame<LocalDate,String> frame = random(1000000, "A", "B", "C", "D", "E", "F", "G", "H", "I", "J");

        //Run 10 performance samples, randomizing the frame before each test
        DataFrame<String,String> timing = PerfStat.run(10, TimeUnit.MILLISECONDS, false, tasks -> {

            tasks.beforeEach(() -> frame.applyDoubles(v -> Math.random() * 100d));

            tasks.put("Bad", () -> {
                for (int i=0; i<frame.rowCount(); ++i) {
                    final DataFrameRow<LocalDate,String> row = frame.rowAt(i);
                    final double mean = row.stats().mean();
                    row.applyDoubles(v -> v.getDouble() - mean);
                }
                return frame;
            });

            tasks.put("Good(sequential)", () -> {
                frame.rows().forEach(row -> {
                    final double mean = row.stats().mean();
                    row.applyDoubles(v -> v.getDouble() - mean);
                });
                return frame;
            });

            tasks.put("Good(parallel)", () -> {
                frame.rows().parallel().forEach(row -> {
                    final double mean = row.stats().mean();
                    row.applyDoubles(v -> v.getDouble() - mean);
                });
                return frame;
            });
        });

        //Plot a chart of the results
        Chart.of(timing, chart -> {
            chart.plot(0).withBars(0d);
            chart.title().withText("DataFrame Row Demeaning Performance (10 Samples)");
            chart.subtitle().withText("DataFrame Dimension: 1 Million x 10");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.axes().domain().label().withText("Timing Statistic");
            chart.axes().range(0).label().withText("Time (Milliseconds)");
            chart.legend().on().bottom();
            chart.writerPng(new File("./docs/images/frame/data-frame-row-demean.png"), 845, 400);
            chart.show();
        });

        Thread.currentThread().join();

    }

    @Test()
    public void colStats() {
        DataFrame<LocalDate,String> frame = random(100, "A", "B", "C", "D", "E");

        frame.out().print();

        DataFrame<String,StatType> count = frame.cols().stats().count();
        DataFrame<String,StatType> min = frame.cols().stats().min();
        DataFrame<String,StatType> max = frame.cols().stats().max();
        DataFrame<String,StatType> mean = frame.cols().stats().mean();
        DataFrame<String,StatType> median = frame.cols().stats().median();
        DataFrame<String,StatType> variance = frame.cols().stats().variance();
        DataFrame<String,StatType> stdDev = frame.cols().stats().stdDev();
        DataFrame<String,StatType> kurtosis = frame.cols().stats().kurtosis();
        DataFrame<String,StatType> mad = frame.cols().stats().mad();
        DataFrame<String,StatType> sum = frame.cols().stats().sum();
        DataFrame<String,StatType> sumLogs = frame.cols().stats().sumLogs();
        DataFrame<String,StatType> sumSquares = frame.cols().stats().sumSquares();
        DataFrame<String,StatType> sem = frame.cols().stats().sem();
        DataFrame<String,StatType> geoMean = frame.cols().stats().geoMean();
        DataFrame<String,StatType> autocorr = frame.cols().stats().autocorr(1);
        DataFrame<String,StatType> percentile = frame.cols().stats().percentile(0.75d);

        percentile.out().print();

        DataFrame<String,StatType> colStats = frame.cols().describe(
            StatType.COUNT, StatType.MEAN, StatType.VARIANCE, StatType.SKEWNESS, StatType.SUM
        );
        colStats.out().print();

    }


    @Test()
    public void colStats1() {

        //Create 100x5 DataFrame of random doubles
        DataFrame<LocalDate,String> frame = random(100, "A", "B", "C", "D", "E");
        //Capture stats interface for two columns independently sourced by ordinal and key
        Stats<Double> stats1 = frame.colAt(3).stats();
        Stats<Double> stats2 = frame.colAt("D").stats();

        StatType.univariate().forEach(statType -> {
            switch (statType) {
                case COUNT:         assert(stats1.count().doubleValue() == stats2.count());                     break;
                case MIN:           assert(stats1.min().doubleValue() == stats2.min());                         break;
                case MAX:           assert(stats1.max().doubleValue() == stats2.max());                         break;
                case MEAN:          assert(stats1.mean().doubleValue() == stats2.mean());                       break;
                case MEDIAN:        assert(stats1.median().doubleValue() == stats2.median());                   break;
                case VARIANCE:      assert(stats1.variance().doubleValue() == stats2.variance());               break;
                case STD_DEV:       assert(stats1.stdDev().doubleValue() == stats2.stdDev());                   break;
                case KURTOSIS:      assert(stats1.kurtosis().doubleValue() == stats2.kurtosis());               break;
                case MAD:           assert(stats1.mad().doubleValue() == stats2.mad());                         break;
                case SEM:           assert(stats1.sem().doubleValue() == stats2.sem());                         break;
                case GEO_MEAN:      assert(stats1.geoMean().doubleValue() == stats2.geoMean());                 break;
                case SUM:           assert(stats1.sum().doubleValue() == stats2.sum());                         break;
                case SUM_SQUARES:   assert(stats1.sumSquares().doubleValue() == stats2.sumSquares());           break;
                case AUTO_CORREL:   assert(stats1.autocorr(2).doubleValue() == stats2.autocorr(2));             break;
                case PERCENTILE:    assert(stats1.percentile(0.75).doubleValue() == stats2.percentile(0.75));   break;
            }
        });
    }


    @Test()
    public void expanding() {
        DataFrame<LocalDate,String> frame = random(100, "A", "B", "C", "D", "E");
        DataFrame<LocalDate,String> expandingMean = frame.cols().stats().expanding(5).mean();
        expandingMean.out().print(10);
    }


    @Test()
    public void rolling() {
        DataFrame<LocalDate,String> frame = random(100, "A", "B", "C", "D", "E");
        DataFrame<LocalDate,String> rollingMean = frame.cols().stats().rolling(5).mean();
        rollingMean.out().print(10);

    }




    @Test()
    public void testStats1() {
        DataFrame<Integer,String> frame = loadCarDataset();
        frame.out().print();
        frame.cols().describe(
            StatType.MEAN,
            StatType.MIN,
            StatType.MAX
        ).out(). print();
    }

    @Test()
    public void testStats2() {
        DataFrame<Integer,String> frame = loadCarDataset();
        frame.out().print();
        frame.transpose().rows().describe(
            StatType.MEAN,
            StatType.MIN,
            StatType.MAX
        ).out(). print();
    }

    @Test()
    public void testNonNumeric1() {
        DataFrame<Integer,String> frame = loadCarDataset();
        DataFrame<String,StatType> colStats = frame.cols().describe(
            StatType.COUNT, StatType.MEAN, StatType.VARIANCE, StatType.SKEWNESS, StatType.SUM
        );

        frame.out().print();
        colStats.out().print();

    }

    @Test()
    public void testCovariance1() {
        DataFrame<Integer,String> frame = loadCarDataset();
        double covar1 = frame.cols().stats().covariance("Price", "Horsepower");
        double covar2 = frame.cols().stats().covariance("EngineSize", "MPG.city");
        System.out.printf("\nCovariance between Price & Horsepower = %.2f", covar1);
        System.out.printf("\nCovariance between EngineSize and MPG.city = %.2f", covar2);
    }

    @Test()
    public void testCovariance2() {
        DataFrame<Integer,String> frame = loadCarDataset();
        DataFrame<String,String> covm = frame.cols().stats().covariance();
        covm.out().print(100, formats -> {
            formats.setDecimalFormat("0.000;-0.000", 1);
        });
    }


    @Test()
    public void testCorrelation1() {
        DataFrame<Integer,String> frame = loadCarDataset();
        double correl1 = frame.cols().stats().correlation("Price", "Horsepower");
        double correl2 = frame.cols().stats().correlation("EngineSize", "MPG.city");
        System.out.printf("\nCorrelation between Price & Horsepower = %.2f", correl1);
        System.out.printf("\nCorrelation between EngineSize and MPG.city = %.2f", correl2);
    }

    @Test()
    public void testCorrelation2() {
        DataFrame<Integer,String> frame = loadCarDataset();
        DataFrame<String,String> correlm = frame.cols().stats().correlation();
        correlm.out().print(100, formats -> {
            formats.setDecimalFormat("0.000;-0.000", 1);
        });
    }


    @Test()
    public void correlationPerformance() throws Exception {

        //Create a 1,000,000x10 DataFrame of random double precision values
        DataFrame<LocalDate,String> frame = random(1000000, "A", "B", "C", "D", "E", "F", "G", "H", "I", "J");

        //Run 10 performance samples, randomizing the frame before each test
        DataFrame<String,String> timing = PerfStat.run(10, TimeUnit.MILLISECONDS, false, tasks -> {
            tasks.beforeEach(() -> frame.applyDoubles(v -> Math.random() * 100d));
            tasks.put("Sequential", () -> frame.cols().stats().correlation());
            tasks.put("Parallel", () -> frame.cols().parallel().stats().correlation());
        });

        //Plot a chart of the results
        Chart.of(timing, chart -> {
            chart.plot(0).withBars(0d);
            chart.title().withText("DataFrame Correlation Matrix Performance (10 Samples)");
            chart.subtitle().withText("DataFrame Dimension: 1 Million x 10");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.axes().domain().label().withText("Timing Statistic");
            chart.axes().range(0).label().withText("Time (Milliseconds)");
            chart.legend().on().bottom();
            chart.writerPng(new File("./docs/images/frame/data-frame-column-correl.png"), 845, 400);
            chart.show();
        });

        Thread.currentThread().join();

    }

}
