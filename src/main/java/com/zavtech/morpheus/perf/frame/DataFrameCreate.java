package com.zavtech.morpheus.perf.frame;

import java.awt.*;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.zavtech.morpheus.viz.chart.Chart;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.util.PerfStat;
import com.zavtech.morpheus.range.Range;

public class DataFrameCreate {

    public static void main(String[] args) {

        final int sample = 5;

        Range<Integer> rowCounts = Range.of(1,6).map(i -> i * 1000000);
        Stream<String> typeName = Stream.of("Integer", "Date", "Instant", "LocalDateTime", "ZonedDateTime");
        List<String> labels = typeName.map(s -> "DataFrame<" + s + ",?>").collect(Collectors.toList());
        DataFrame<String,String> results = DataFrame.ofDoubles(rowCounts.map(String::valueOf), labels);
        Array<String> colKeys = Array.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J");

        rowCounts.forEach(rowCount -> {

            DataFrame<String,String> timing = PerfStat.run(sample, TimeUnit.MILLISECONDS, false, tasks -> {

                Range<Integer> rowIndexes = Range.of(0, rowCount.intValue());

                tasks.put("DataFrame<Integer,?>", () -> DataFrame.ofDoubles(rowIndexes, colKeys));

                tasks.put("DataFrame<Date,?>", () -> {
                    final long now = System.currentTimeMillis();
                    final Range<Date> rowKeys = rowIndexes.map(i -> new Date(now + (i * 1000)));
                    return DataFrame.ofDoubles(rowKeys, colKeys);
                });

                tasks.put("DataFrame<Instant,?>", () -> {
                    final Instant now = Instant.now();
                    final Range<Instant> rowKeys = rowIndexes.map(now::plusSeconds);
                    return DataFrame.ofDoubles(rowKeys, colKeys);
                });

                tasks.put("DataFrame<LocalDateTime,?>", () -> {
                    final Duration step = Duration.ofSeconds(1);
                    final LocalDateTime start = LocalDateTime.now().minusYears(10);
                    final LocalDateTime end = start.plusSeconds(rowCount);
                    final Range<LocalDateTime> rowKeys = Range.of(start, end, step);
                    return DataFrame.ofDoubles(rowKeys, colKeys);
                });

                tasks.put("DataFrame<ZonedDateTime,?>", () -> {
                    final Duration step = Duration.ofSeconds(1);
                    final ZonedDateTime start = ZonedDateTime.now();
                    final ZonedDateTime end = start.plusSeconds(rowCount);
                    final Range<ZonedDateTime> rowKeys = Range.of(start, end, step);
                    return DataFrame.ofDoubles(rowKeys, colKeys);
                });
            });

            String label = String.valueOf(rowCount);
            results.data().setDouble(label, 0, timing.data().getDouble("Mean", 0));
            results.data().setDouble(label, 1, timing.data().getDouble("Mean", 1));
            results.data().setDouble(label, 2, timing.data().getDouble("Mean", 1));
            results.data().setDouble(label, 3, timing.data().getDouble("Mean", 2));
            results.data().setDouble(label, 4, timing.data().getDouble("Mean", 2));
        });

        String initTitle = "DataFrame Create Times, 1-5 Million rows x 10 columns of Doubles (Sample " + sample + ")";
        Chart.of(results, chart -> {
            chart.plot(0).withBars(0d);
            chart.title().withText(initTitle);
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.axes().domain().label().withText("Row Count");
            chart.axes().range(0).label().withText("Time (Milliseconds)");
            chart.legend().on().bottom();
            chart.writerPng(new File("./morpheus-docs/docs/images/data-frame-init-times.png"), 845, 400);
            chart.show();
        });
    }

}
