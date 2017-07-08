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
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.frame.DataFrameGrouping;
import com.zavtech.morpheus.util.PerfStat;
import com.zavtech.morpheus.util.Tuple;
import com.zavtech.morpheus.util.text.parser.Parser;
import com.zavtech.morpheus.viz.chart.Chart;

public class GroupingDocs {


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


    /**
     * Returns a DataFrame of UK house prices from the Land Registry of the UK
     * @param year      the year to load prices for
     * @return          the house price DataFrame
     */
    static DataFrame<Integer,String> loadHousePrices(int year) {
        return DataFrame.read().csv(options -> {
            options.setHeader(false);
            options.setParallel(true);
            options.setExcludeColumns("Column-0");
            options.setResource("/uk-house-prices-" + year + ".csv");
            options.getFormats().setDateFormat("Date", "yyyy-MM-dd HH:mm");
            options.setColumnNameMapping((colName, colOrdinal) -> {
                switch (colOrdinal) {
                    case 0:  return "Price";
                    case 1:  return "Date";
                    case 2:  return "PostCode";
                    case 3:  return "PropertyType";
                    case 4:  return "Old/New";
                    case 5:  return "Duration";
                    case 6:  return "PAON";
                    case 7:  return "SAON";
                    case 8:  return "Street";
                    case 9:  return "Locality";
                    case 10: return "Town/City";
                    case 11: return "District";
                    case 12: return "County";
                    case 13: return "PPDType";
                    case 14: return "RecordStatus";
                    default: return colName;
                }
            });
        });
    }


    @Test()
    public void groupRowsExample1() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        frame.out().print();

        DataFrameGrouping.Rows<Integer,String> grouping = frame.rows().groupBy("Surface", "Round");
        for (int depth=0; depth<grouping.getDepth(); ++depth) {
            System.out.printf("Groups for depth %s...\n", depth);
            grouping.getGroupKeys(depth).sorted().forEach(groupKey -> {
                DataFrame<Integer,String> group = grouping.getGroup(groupKey);
                System.out.printf("There are %s rows for group %s\n", group.rowCount(), groupKey);
            });
        }
    }


    @Test()
    public void groupRowsExample2() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        DataFrameGrouping.Rows<Integer,String> grouping = frame.rows().groupBy(row -> {
            String surface = row.getValue("Surface");
            String round = row.getValue("Round");
            return Tuple.of(surface, round);
        });
        for (int depth=0; depth<grouping.getDepth(); ++depth) {
            System.out.printf("Groups for depth %s...\n", depth);
            grouping.getGroupKeys(depth).sorted().forEach(groupKey -> {
                DataFrame<Integer,String> group = grouping.getGroup(groupKey);
                System.out.printf("There are %s rows for group %s\n", group.rowCount(), groupKey);
            });
        }
    }

    @Test()
    public void groupRowsExample3() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        frame.rows().groupBy(row -> {
            LocalDate date = row.getValue("Date");
            Month month = date.getMonth();
            return Tuple.of(month);
        }).forEach(0, (groupKey, group) -> {
            System.out.printf("There are %s rows for group %s\n", group.rowCount(), groupKey);
        });
    }

    @Test()
    public void groupRowExample4() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        DataFrameGrouping.Rows<Integer,String> grouping = frame.rows().groupBy("Court", "Surface", "Round");
        for (int depth=0; depth<grouping.getDepth(); ++depth) {
            System.out.printf("Groups for depth %s...\n", depth);
            grouping.getGroupKeys(depth).sorted().forEach(groupKey -> {
                DataFrame<Integer,String> group = grouping.getGroup(groupKey);
                System.out.printf("There are %s rows for group %s\n", group.rowCount(), groupKey);
            });
        }
    }

    @Test()
    public void groupRowExample5() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        DataFrameGrouping.Rows<Integer,String> grouping = frame.rows().groupBy("Court", "Surface", "Round");
        grouping.stats(0).mean().rows().sort(true).out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.00;-0.00", 1);
        });
        grouping.stats(1).mean().rows().sort(true).out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.00;-0.00", 1);
        });
        grouping.stats(2).mean().rows().sort(true).out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.00;-0.00", 1);
        });

    }


    @Test()
    public void groupColsExample1() {
        DataFrame<String,Integer> frame = loadTennisMatchData(2013).transpose();
        frame.left(20).out().print(10);
        DataFrameGrouping.Cols<String,Integer> grouping = frame.cols().groupBy("Court", "Surface");
        for (int depth=0; depth<grouping.getDepth(); ++depth) {
            System.out.printf("Groups for depth %s...\n", depth);
            grouping.getGroupKeys(depth).sorted().forEach(groupKey -> {
                DataFrame<String,Integer> group = grouping.getGroup(groupKey);
                System.out.printf("There are %s columns for group %s\n", group.colCount(), groupKey);
            });
        }
    }


    @Test()
    public void groupLarge() throws Exception {

        //Load UK house prices for 2006
        DataFrame<Integer,String> frame = loadHousePrices(2006);

        frame.out().print();

        //Run 10 iterations of sequential and parallel group by Town/City
        DataFrame<String,String> results = PerfStat.run(5, TimeUnit.MILLISECONDS, false, tasks -> {
            tasks.put("PropertyType", () -> frame.rows().groupBy("PropertyType"));
            tasks.put("Month", () -> frame.rows().groupBy(row -> {
                final LocalDate date = row.getValue("Date");
                return Tuple.of(date.getMonth());
            }));
            tasks.put("County", () -> frame.rows().groupBy("County"));
            tasks.put("District", () -> frame.rows().groupBy("District"));
            tasks.put("Town/City", () -> frame.rows().groupBy("Town/City"));
            tasks.put("Locality", () -> frame.rows().groupBy("Locality"));
        });

        //Plot the results of the combined DataFrame with timings
        Chart.of(results, chart -> {
            chart.plot(0).withBars(0d);
            chart.axes().domain().label().withText("Timing Statistic");
            chart.axes().range(0).label().withText("Time In Milliseconds");
            chart.title().withText("1-Dimensional Grouping of 1.35 Million Rows (Sequential)");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.subtitle().withText("Grouping of UK House Price Transactions in 2006 by various columns");
            chart.legend().on().bottom();
            chart.writerPng(new File("./docs/images/frame/data-frame-group-by-0.png"), 845, 400);
            chart.show();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void groupLarge1() throws Exception {

        //Load UK house prices for 2006
        DataFrame<Integer,String> frame = loadHousePrices(2006);

        //Run 10 iterations of sequential and parallel group by Town/City
        DataFrame<String,String> results = PerfStat.run(5, TimeUnit.MILLISECONDS, false, tasks -> {
            tasks.put("Sequential", () -> frame.rows().sequential().groupBy("County"));
            tasks.put("Parallel", () -> frame.rows().parallel().groupBy("County"));
        });

        //Plot the results of the combined DataFrame with timings
        Chart.of(results, chart -> {
            chart.plot(0).withBars(0d);
            chart.axes().domain().label().withText("Timing Statistic");
            chart.axes().range(0).label().withText("Time In Milliseconds");
            chart.title().withText("1-Dimensional Grouping of 1.35 Million Rows");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.subtitle().withText("Grouping of UK House Price Transactions in 2006 by County");
            chart.legend().on().bottom();
            chart.writerPng(new File("./docs/images/frame/data-frame-group-by-2.png"), 845, 400);
            chart.show();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void groupLarge2() throws Exception {

        //Load UK house prices for 2006
        DataFrame<Integer,String> frame = loadHousePrices(2006);

        //Run 10 iterations of sequential and parallel group by County and Town/City
        DataFrame<String,String> results = PerfStat.run(5, TimeUnit.MILLISECONDS, false, tasks -> {
            tasks.put("Sequential(1-D)", () -> frame.rows().sequential().groupBy("County"));
            tasks.put("Parallel(1-D)", () -> frame.rows().parallel().groupBy("County"));
            tasks.put("Sequential(2-D)", () -> frame.rows().sequential().groupBy("County", "County"));
            tasks.put("Parallel(2-D)", () -> frame.rows().parallel().groupBy("County", "County"));
        });

        //Plot the results of the combined DataFrame with timings
        Chart.of(results, chart -> {
            chart.plot(0).withBars(0d);
            chart.axes().domain().label().withText("Timing Statistic");
            chart.axes().range(0).label().withText("Time In Milliseconds");
            chart.title().withText("1-Dimension vs 2-Dimensional Grouping of 1.35 Million Rows");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.subtitle().withText("Grouping of UK House Price Transactions in 2006 by County x 2");
            chart.legend().on().bottom();
            chart.writerPng(new File("./docs/images/frame/data-frame-group-by-3.png"), 845, 400);
            chart.show();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void groupLarge3() throws Exception {

        //Load UK house prices for 2006
        DataFrame<Integer,String> frame = loadHousePrices(2006);

        //Run 10 iterations of sequential and parallel group by County and Town/City
        DataFrame<String,String> results = PerfStat.run(5, TimeUnit.MILLISECONDS, false, tasks -> {
            tasks.put("Method-1", () -> frame.rows().groupBy("County", "Town/City"));
            tasks.put("Method-2", () -> frame.rows().groupBy(row -> Tuple.of(
                row.<String>getValue("County"),
                row.<String>getValue("Town/City"))
            ));
        });

        //Plot the results of the combined DataFrame with timings
        Chart.of(results, chart -> {
            chart.plot(0).withBars(0d);
            chart.axes().domain().label().withText("Timing Statistic");
            chart.axes().range(0).label().withText("Time In Milliseconds");
            chart.title().withText("2-Dimensional Grouping of 1.35 Million Rows");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.subtitle().withText("Grouping of UK House Price Transactions in 2006 by County & Town/City");
            chart.legend().on().bottom();
            chart.writerPng(new File("./docs/images/frame/data-frame-group-by-4.png"), 845, 400);
            chart.show();
        });

        Thread.currentThread().join();
    }




}
