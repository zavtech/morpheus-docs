package com.zavtech.morpheus.perf.frame;


import java.awt.*;
import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.zavtech.morpheus.util.MemoryEstimator;
import com.zavtech.morpheus.viz.chart.Chart;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;

public class DataFrameMemory {

    public static void main(String[] args) {

        final Array<String> groups = Array.of("Integer", "Long", "LocalDateTime");
        final Array<String> colKeys = Array.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J");
        final Array<Integer> counts = Array.of(
                500000, 1000000, 2500000, 5000000, 7500000, 10000000, 12500000, 15000000, 20000000
        );

        final MemoryEstimator memoryEstimator = new MemoryEstimator.DefaultMemoryEstimator();
        final List<String> rowKeys = counts.stream().values().map(Object::toString).collect(Collectors.toList());
        final DataFrame<String,String> results = DataFrame.ofDoubles(rowKeys, groups);

        counts.forEachValue(v -> {
            final String key = String.valueOf(v.getInt());
            final Range<Integer> range = Range.of(0, v.getInt());
            final DataFrame<Integer,String> frame = DataFrame.ofDoubles(range, colKeys);
            final long bytes = memoryEstimator.getObjectSize(frame);
            results.data().setDouble(key, "Integer", bytes / Math.pow(1024, 2));
        });

        counts.forEachValue(v -> {
            final String key = String.valueOf(v.getInt());
            final Range<Long> range = Range.of(0L, (long)v.getInt());
            final DataFrame<Long,String> frame = DataFrame.ofDoubles(range, colKeys);
            final long bytes = memoryEstimator.getObjectSize(frame);
            results.data().setDouble(key, "Long", bytes / Math.pow(1024, 2));
        });

        counts.forEachValue(v -> {
            final String key = String.valueOf(v.getInt());
            final LocalDateTime start = LocalDateTime.now();
            final Range<LocalDateTime> range = Range.of(start, start.plusSeconds(v.getInt()), Duration.ofSeconds(1));
            final DataFrame<LocalDateTime,String> frame = DataFrame.ofDoubles(range, colKeys);
            final long bytes = memoryEstimator.getObjectSize(frame);
            results.data().setDouble(key, "LocalDateTime", bytes / Math.pow(1024, 2));
        });

        Chart.create().withBarPlot(results, false, chart -> {
            chart.title().withText("DataFrame Memory Usage With Increasing Row Count (10 columns of doubles)");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Row Count");
            chart.plot().axes().range(0).label().withText("Memory Usage (MB)");
            chart.legend().on().bottom();
            chart.writerPng(new File("./docs/images/frame/data-frame-memory.png"), 845, 400, true);
            chart.show();
        });

    }


    public void gcTimes() {

    }

}
