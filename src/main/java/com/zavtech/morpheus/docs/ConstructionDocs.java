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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.util.Collect;
import com.zavtech.morpheus.util.PerfStat;
import com.zavtech.morpheus.util.text.parser.Parser;
import com.zavtech.morpheus.viz.chart.Chart;

public class ConstructionDocs {

    public static void main(String[] args) {
        example1();
        example2();
    }

    public static void example1() {
        //Create a list of column keys
        Iterable<Month> months = Array.of(Month.class, Month.values());
        //Create a list of row keys
        Iterable<Year> years = Range.of(1995, 2000).map(Year::of);

        //Create frames optimized to hold various types of data.
        DataFrame<Year,Month> booleans1 = DataFrame.ofBooleans(years, months);
        DataFrame<Year,Month> integers1 = DataFrame.ofInts(years, months);
        DataFrame<Year,Month> longs1 = DataFrame.ofLongs(years, months);
        DataFrame<Year,Month> doubles1 = DataFrame.ofDoubles(years, months);
        DataFrame<Year,Month> objects1 = DataFrame.ofObjects(years, months);

        //Create a frame with a single column initially
        DataFrame<Year,Month> booleans2 = DataFrame.ofBooleans(years, Month.JANUARY);
        DataFrame<Year,Month> integers2 = DataFrame.ofInts(years, Month.JANUARY);
        DataFrame<Year,Month> longs2 = DataFrame.ofLongs(years, Month.JANUARY);
        DataFrame<Year,Month> doubles2 = DataFrame.ofDoubles(years, Month.JANUARY);
        DataFrame<Year,Month> objects2 = DataFrame.ofObjects(years, Month.JANUARY);

        //Create a frame with a single row initially
        DataFrame<Year,Month> booleans3 = DataFrame.ofBooleans(Year.of(2014), months);
        DataFrame<Year,Month> integers3 = DataFrame.ofInts(Year.of(2014), months);
        DataFrame<Year,Month> longs3 = DataFrame.ofLongs(Year.of(2014), months);
        DataFrame<Year,Month> doubles3 = DataFrame.ofDoubles(Year.of(2014), months);
        DataFrame<Year,Month> objects3 = DataFrame.ofObjects(Year.of(2014), months);

        //Create a frame with 5 columns each optimized for a different data type and randomly initialized values
        Random rand = new java.util.Random();
        DataFrame<Year,Month> randomFrame = DataFrame.of(years, Month.class, columns -> {
            columns.add(Month.JANUARY, Boolean.class).applyBooleans(v -> rand.nextBoolean());  //Column of booleans
            columns.add(Month.MARCH, Integer.class).applyInts(v -> rand.nextInt());       //Column of ints
            columns.add(Month.JUNE, Long.class).applyLongs(v -> rand.nextLong());     //Column of longs
            columns.add(Month.SEPTEMBER, Double.class).applyDoubles(v -> rand.nextDouble());    //Column of doubles
            columns.add(Month.DECEMBER, Object.class).applyValues(v -> String.valueOf(rand.nextDouble()));    //Column of objects
        });
    }

    public static void example2() {
        Iterable<Year> years = Range.of(1995, 2000).map(Year::of);
        DataFrame<Year,Month> frame = DataFrame.of(years, Month.class, columns -> {

        });
    }


    public static void example3() {
        //Create an empty frame with initial capacity, then add rows and columns
        DataFrame<Year,Month> frame = DataFrame.empty(Year.class, Month.class);
        frame.rows().add(Year.of(1975));
        frame.rows().add(Year.of(1980));
        frame.rows().addAll(Range.of(1995, 2014).map(Year::of));
        frame.cols().add(Month.JANUARY, Double.class);
        frame.cols().add(Month.MARCH, Double.class);
        frame.cols().addAll(Array.of(Month.APRIL, Month.JULY), Double.class);
        frame.out().print();
    }


    @Test()
    public void csvPerformanceLargeFile() throws Exception {

        final String path = "/Users/witdxav/Dropbox/data/fxcm/AUDUSD/2012/AUDUSD-2012.csv";
        final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        DataFrame<String,String> timingStats = PerfStat.run(5, TimeUnit.MILLISECONDS, false, tasks -> {

            tasks.put("Sequential", () -> DataFrame.read().<LocalDateTime>csv(options -> {
                options.setHeader(false);
                options.setParallel(false);
                options.setResource(path);
                options.setExcludeColumnIndexes(1);
                options.setRowKeyParser(LocalDateTime.class, row -> {
                    final LocalDate date = LocalDate.parse(row[0], dateFormat);
                    final LocalTime time = LocalTime.parse(row[1], timeFormat);
                    return LocalDateTime.of(date, time);
                });
            }));

            tasks.put("Parallel", () -> DataFrame.read().<LocalDateTime>csv(options -> {
                options.setHeader(false);
                options.setParallel(true);
                options.setResource(path);
                options.setExcludeColumnIndexes(1);
                options.setRowKeyParser(LocalDateTime.class, row -> {
                    final LocalDate date = LocalDate.parse(row[0], dateFormat);
                    final LocalTime time = LocalTime.parse(row[1], timeFormat);
                    return LocalDateTime.of(date, time);
                });
            }));

        });

        Chart.create().withBarPlot(timingStats, false, chart -> {
            chart.title().withText("CSV Parsing Performance (Sequential vs Parallel)");
            chart.subtitle().withText("File Size: 40MB, 760,000 lines, 6 columns");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 16));
            chart.plot().axes().domain().label().withText("Statistic");
            chart.plot().axes().range(0).label().withText("Time in Milliseconds");
            chart.legend().on();
            chart.writerPng(new File("./docs/images/morpheus-parse-csv-times.png"), 845, 450, true);
            chart.show();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void parseCsv1() {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String url = "http://chart.finance.yahoo.com/table.csv?s=SPY&a=0&b=1&c=2013&d=5&e=6&f=2014&g=d&ignore=.csv";
        DataFrame<LocalDate,String> frame = DataFrame.read().csv(options -> {
            options.setResource(url);
            options.setRowKeyParser(LocalDate.class, values -> LocalDate.parse(values[0], dateFormat));
            options.setParser("Volume", Long.class);
        });

        frame.out().print();
    }


    @Test()
    public void parseCsv2() {
        //Parse file or classpath resource, with first row as header
        DataFrame<Integer,String> frame1 = DataFrame.read().csv("/temp/data.csv");

        //Parse URL, with first row as header
        DataFrame<Integer,String> frame2 = DataFrame.read().csv("http://www.domain.com/data?file.csv");

        //Parse file, with first row as header, and row keys parsed as LocalDates from the first column, index=0
        DataFrame<LocalDate,String> frame3 = DataFrame.read().csv(options -> {
            options.setResource("/temp/data.csv");
            options.setRowKeyParser(LocalDate.class, row -> LocalDate.parse(row[0]));
        });
    }


    @Test()
    public void parseCsv3() {

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Set<String> columnSet = Collect.asSet("Open", "Close", "Adj Close");
        String url = "http://chart.finance.yahoo.com/table.csv?s=SPY&a=0&b=1&c=2013&d=5&e=6&f=2014&g=d&ignore=.csv";
        DataFrame<LocalDate,String> frame = DataFrame.read().csv(options -> {
            options.setResource(url);
            options.setColNamePredicate(columnSet::contains);
            options.setRowKeyParser(LocalDate.class, values -> LocalDate.parse(values[0], dateFormat));
            options.setParser("Volume", Long.class);
            options.setRowPredicate(row -> {
                LocalDate date = LocalDate.parse(row[0], dateFormat);
                return date.getDayOfWeek() == DayOfWeek.MONDAY;
            });
        });

        frame.out().print();

    }
}
