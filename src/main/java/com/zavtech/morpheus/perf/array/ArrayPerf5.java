package com.zavtech.morpheus.perf.array;

import java.awt.Font;
import java.io.File;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.zavtech.morpheus.viz.chart.Chart;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.stats.StatType;
import com.zavtech.morpheus.util.PerfStat;

public class ArrayPerf5 {

    public static void main(String[] args) {

        final int count = 5;
        final int size = 5000000;

        final List<Class<?>> types = Arrays.asList(int.class, long.class, double.class, Date.class, LocalDate.class, String.class, ZonedDateTime.class);
        final Array<String> colKeys = Array.of("Native", "Morpheus (sequential)", "Morpheus (parallel)");
        final List<String> rowKeys = types.stream().map(Class::getSimpleName).collect(Collectors.toList());
        final DataFrame<String,String> memory = DataFrame.ofDoubles(rowKeys, colKeys);
        final DataFrame<String,String> times = DataFrame.ofDoubles(rowKeys, colKeys);
        types.forEach(type -> {
            for (int style : new int[] {0, 1, 2}) {
                System.out.println("Running tests for " + type);
                final String key = type.getSimpleName();
                final Callable<Object> callable = createCallable(style, type, size);
                final PerfStat stats = PerfStat.run(key, count, TimeUnit.MILLISECONDS, callable);
                final double runTime = stats.getCallTime(StatType.MEDIAN);
                final double gcTime = stats.getGcTime(StatType.MEDIAN);
                times.data().setDouble(key, style, runTime + gcTime);
                memory.data().setDouble(key, style, stats.getUsedMemory(StatType.MEDIAN));
            }
        });

        Chart.of(times, chart -> {
            chart.plot(0).withBars(0d);
            chart.title().withText("Native vs Morpheus (Sequential & Parallel) Median Aggregate Times (inc-GC), 5 Million Entries (Sample " + count + ")");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.axes().domain().label().withText("Data Type");
            chart.axes().range(0).label().withText("Time (Milliseconds)");
            chart.legend().on();
            chart.orientation().horizontal();
            chart.writerPng(new File("./morpheus-docs/docs/images/native-vs-morpheus-aggregate-times.png"), 845, 400);
            chart.show();
        });
    }

    private static Callable<Object> createCallable(int style, Class<?> type, int size) {
        switch (style) {
            case 0: return ArrayPerf4.createNativeCallable(type, size);
            case 1: return ArrayPerf4.createMorpheusCallable(type, size, false);
            case 2: return ArrayPerf4.createMorpheusCallable(type, size, true);
            default:  throw new IllegalArgumentException("Unsupported style: " + style);
        }
    }
}
