package com.zavtech.morpheus.perf.util;

import java.awt.*;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.zavtech.morpheus.viz.chart.Chart;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.util.PerfStat;


public class RangePerf2 {

    public static void main(String[] args) {

        final int sample = 5;

        final Map<String,Integer> countMap = new LinkedHashMap<>();
        countMap.put("1K", 1000);
        countMap.put("5K", 5000);
        countMap.put("10K", 10000);
        countMap.put("50K", 50000);
        countMap.put("100K", 100000);
        countMap.put("500K", 500000);
        countMap.put("1M", 1000000);
        countMap.put("2M", 1000000);
        countMap.put("3M", 1000000);
        countMap.put("4M", 1000000);
        countMap.put("5M", 5000000);
        countMap.put("10M", 10000000);
        countMap.put("15M", 15000000);
        countMap.put("20M", 20000000);

        final DataFrame<String,String> sequential = PerfStat.run(sample, TimeUnit.MILLISECONDS, false, tasks -> {
            for (String key : countMap.keySet()) {
                final int count = countMap.get(key);
                System.out.println("Running task " + key);
                tasks.put(key, () -> {
                    final Duration step = Duration.ofSeconds(1);
                    final ZonedDateTime start = ZonedDateTime.now();
                    final ZonedDateTime end = start.plusSeconds(count);
                    final Range<ZonedDateTime> range = Range.of(start, end, step);
                    return range.toArray(false);
                });
            }
        });

        final DataFrame<String,String> parallel = PerfStat.run(sample, TimeUnit.MILLISECONDS, false, tasks -> {
            for (String key : countMap.keySet()) {
                final int count = countMap.get(key);
                System.out.println("Running task " + key);
                tasks.put(key, () -> {
                    final Duration step = Duration.ofSeconds(1);
                    final ZonedDateTime start = ZonedDateTime.now();
                    final ZonedDateTime end = start.plusSeconds(count);
                    final Range<ZonedDateTime> range = Range.of(start, end, step);
                    return range.toArray(true);
                });
            }
        });

        sequential.out().print();
        parallel.out().print();

        final Set<String> rowKeys = countMap.keySet();
        final DataFrame<String,String> results = DataFrame.of(rowKeys, String.class, columns -> {
            columns.add("Sequential", Double.class).applyDoubles(v -> sequential.data().getDouble("Mean", v.rowKey()));
            columns.add("Parallel", Double.class).applyDoubles(v -> parallel.data().getDouble("Mean", v.rowKey()));
        });


        Chart.create().withBarPlot(results, false, chart -> {
            chart.title().withText("Array construction times from Range");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Timing Statistic");
            chart.plot().axes().range(0).label().withText("Time (Milliseconds)");
            chart.legend().on().right();
            chart.show();
        });
    }
}
