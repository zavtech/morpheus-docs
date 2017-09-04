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
package com.zavtech.morpheus.docs.basic;

import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.frame.DataFrameRow;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.util.Collect;
import com.zavtech.morpheus.util.PerfStat;
import com.zavtech.morpheus.util.text.parser.Parser;
import com.zavtech.morpheus.viz.chart.Chart;

public class SortingDocs {

    /**
     * Returns the ATP match results for the year specified
     * @param year      the year for ATP results
     * @return          the ATP match results
     */
    static DataFrame<Integer,String> loadTennisMatchData(int year) {
        final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yy");
        return DataFrame.read().csv(options -> {
            options.setHeader(true);
            options.setResource("http://www.zavtech.com/data/tennis/atp/atp-" + year + ".csv");
            options.setExcludeColumns("ATP");
            options.setParser("Date", Parser.ofLocalDate(dateFormat));
        });
    }


    @Test()
    public void sortRowAxis() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);

        frame.out().print();

        //Sort rows by row keys in descending order
        frame.rows().sort(false);
        //Print first ten rows
        frame.out().print(10);
    }


    @Test()
    public void sortColAxis() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        //Sort columns by column keys in ascending order
        frame.cols().sort(true);
        //Print first ten rows
        frame.out().print(10);
    }

    @Test()
    public void sortRowsByData1() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        //Sort rows by the WRank (winner rank) column values
        frame.rows().sort(true, "WRank");
        //Print first ten rows
        frame.out().print(10);
    }

    @Test()
    public void sortColsByKeys() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        //Sort columns by column keys in ascending order
        frame.cols().sort(true);
        //Print first ten rows
        frame.out().print(10);
    }

    @Test()
    public void sortColsByData1() {
        //Create a 10x10 frame initialized with random doubles
        DataFrame<String,String> frame = DataFrame.ofDoubles(
            Range.of(0, 10).map(i -> "R" + i),
            Range.of(0, 10).map(i -> "C" + i),
            value -> Math.random() * 100d
        );
        //Sort columns by the data in first row
        frame.cols().sort(true, "R0");
        //Print first ten rows
        frame.out().print(10);
    }

    @Test()
    public void sortRowsAndColsByData1() {
        //Create a 10x10 frame initialized with random doubles
        DataFrame<String,String> frame = DataFrame.ofDoubles(
            Range.of(0, 10).map(i -> "R" + i),
            Range.of(0, 10).map(i -> "C" + i),
            value -> Math.random() * 100d
        );
        //Sort columns by the data in first row
        frame.cols().sort(true, "R0");
        //Sort rows by the data that is now in the first column
        frame.rows().sort(true, frame.cols().key(0));
        //Print first ten rows
        frame.out().print(10);
    }


    @Test()
    public void sorRowsMultiDimensional() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        //Multidimensional row sort (ascending) first by Date, then WRank
        frame.rows().sort(true, Collect.asList("Date", "WRank"));
        //Print first ten rows
        frame.out().print(10);
    }


    @Test()
    public void sortRowsCustom() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        //Sort rows so that matches smallest difference in betting odds between winner and looser.
        frame.rows().sort((row1, row2) -> {
            double diff1 = Math.abs(row1.getDouble("AvgW") - row1.getDouble("AvgL"));
            double diff2 = Math.abs(row2.getDouble("AvgW") - row2.getDouble("AvgL"));
            return Double.compare(diff1, diff2);
        });
        //Print first ten rows
        frame.out().print(10);
    }

    @Test()
    public void sortOnFilter() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        //First filter frame to include only rows where Novak Djokovic was the victor
        DataFrame<Integer,String> filter = frame.rows().select(row -> row.getValue("Winner").equals("Djokovic N."));
        //Sort rows so that the highest rank players he beat come first
        filter.rows().sort(true, "LRank");
        //Print first ten rows
        filter.out().print(10);
    }


    @Test()
    public void performanceSequential() throws Exception {

        //Define range of row counts we want to test, from 1M to 5M inclusive
        Range<Integer> rowCounts = Range.of(1, 6).map(i -> i * 1000000);

        //Time DataFrame sort operations on frame of random doubles with row counts ranging from 1M to 6M
        DataFrame<String,String> results = DataFrame.combineFirst(rowCounts.map(rowCount -> {
            Range<Integer> rowKeys = Range.of(0, rowCount.intValue());
            Range<String> colKeys = Range.of(0, 5).map(i -> "C" + i);
            //Create frame initialized with random double values
            DataFrame<Integer,String> frame = DataFrame.ofDoubles(rowKeys, colKeys, v -> Math.random() * 100d);
            String label = "Rows(" + (rowCount / 1000000) + "M)";
            //Run each test 10 times, clear the sort before running the test with sort(null)
            return PerfStat.run(10, TimeUnit.MILLISECONDS, false, tasks -> {
                tasks.beforeEach(() -> frame.rows().sort(null));
                tasks.put(label, () -> frame.rows().sort(true, "C1"));
            });
        }));

        //Plot the results of the combined DataFrame with timings
        Chart.create().withBarPlot(results, false, chart -> {
            chart.plot().axes().domain().label().withText("Timing Statistic");
            chart.plot().axes().range(0).label().withText("Time In Milliseconds");
            chart.title().withText("DataFrame Sorting Performance (Sequential)");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.subtitle().withText("Row Sort with counts from 1M to 5M rows");
            chart.legend().on().bottom();
            chart.writerPng(new File("./docs/images/frame/data-frame-row-sort-sequential.png"), 845, 400, true);
            chart.show();
        });
    }



    @Test()
    public void performanceParallel() throws Exception {

        //Define range of row counts we want to test, from 1M to 5M inclusive
        Range<Integer> rowCounts = Range.of(1, 6).map(i -> i * 1000000);

        //Time DataFrame sort operations on frame of random doubles with row counts ranging from 1M to 6M
        DataFrame<String,String> results = DataFrame.combineFirst(rowCounts.map(rowCount -> {
            Range<Integer> rowKeys = Range.of(0, rowCount.intValue());
            Range<String> colKeys = Range.of(0, 5).map(i -> "C" + i);
            //Create frame initialized with random double values
            DataFrame<Integer,String> frame = DataFrame.ofDoubles(rowKeys, colKeys, v -> Math.random() * 100d);
            String label = "Rows(" + (rowCount / 1000000) + "M)";
            //Run each test 10 times, clear the sort before running the test with sort(null)
            return PerfStat.run(10, TimeUnit.MILLISECONDS, false, tasks -> {
                tasks.beforeEach(() -> frame.rows().sort(null));
                tasks.put(label, () -> frame.rows().parallel().sort(true, "C1"));
            });
        }));

        //Plot the results of the combined DataFrame with timings
        Chart.create().withBarPlot(results, false, chart -> {
            chart.plot().axes().domain().label().withText("Timing Statistic");
            chart.plot().axes().range(0).label().withText("Time In Milliseconds");
            chart.title().withText("DataFrame Sorting Performance (Parallel)");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.subtitle().withText("Row Sort with counts from 1M to 5M rows");
            chart.legend().on().bottom();
            chart.writerPng(new File("./docs/images/frame/data-frame-row-sort-parallel.png"), 845, 400, true);
            chart.show();
        });
    }

    @Test()
    public void performanceComparator() {

        //Create frame initialized with random double values
        Range<Integer> rowKeys = Range.of(0, 1000000);
        Range<String> colKeys = Range.of(0, 5).map(i -> "C" + i);
        DataFrame<Integer,String> frame = DataFrame.ofDoubles(rowKeys, colKeys, v -> Math.random() * 100d);
        //Define comparator to sort rows by column C1, which is ordinal 1
        Comparator<DataFrameRow<Integer,String>> comparator = (row1, row2) -> {
            double v1 = row1.getDouble(1);
            double v2 = row2.getDouble(1);
            return Double.compare(v1, v2);
        };

        //Time sorting in various modes (with & without comparator in both sequential & parallel mode)
        DataFrame<String,String> results = PerfStat.run(10, TimeUnit.MILLISECONDS, false, tasks -> {
            tasks.beforeEach(() -> frame.rows().sort(null));
            tasks.put("W/O Comparator (seq)", () -> frame.rows().sort(true, "C1"));
            tasks.put("W/O Comparator (par)", () -> frame.rows().parallel().sort(true, "C1"));
            tasks.put("W/ Comparator (seq)", () -> frame.rows().sort(comparator));
            tasks.put("W/ Comparator (par)", () -> frame.rows().parallel().sort(comparator));
        });

        //Plot the results of the combined DataFrame with timings
        Chart.create().withBarPlot(results, false, chart -> {
            chart.plot().axes().domain().label().withText("Timing Statistic");
            chart.plot().axes().range(0).label().withText("Time In Milliseconds");
            chart.title().withText("DataFrame Sorting Performance With & Without Comparator");
            chart.subtitle().withText("1 Million rows of random double precision values");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.legend().on().bottom();
            chart.writerPng(new File("./docs/images/frame/data-frame-row-sort-comparator.png"), 845, 400, true);
            chart.show();
        });

    }

}
