package com.zavtech.morpheus.perf.array;

import java.awt.*;
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

public class ArrayPerf3 {

    private static final Class[] types = new Class[] {
            int.class,
            long.class,
            double.class,
            Date.class,
            LocalDate.class,
            String.class,
            ZonedDateTime.class
    };

    public static void main(String[] args) {

        final int count = 5;
        final int size = 5000000;

        final Array<String> colKeys = Array.of("Native", "Morpheus");
        final List<String> rowKeys = Arrays.stream(types).map(Class::getSimpleName).collect(Collectors.toList());
        final DataFrame<String,String> memory = DataFrame.ofDoubles(rowKeys, colKeys);
        final DataFrame<String,String> runTimes = DataFrame.ofDoubles(rowKeys, colKeys);
        final DataFrame<String,String> totalTimes = DataFrame.ofDoubles(rowKeys, colKeys);
        Arrays.stream(types).forEach(type -> {
            for (int style : new int[] {0, 1}) {
                System.out.println("Running tests for " + type);
                final String key = type.getSimpleName();
                final Callable<Object> callable = createCallable(type, size, style);
                final PerfStat stats = PerfStat.run(key, count, TimeUnit.MILLISECONDS, callable);
                final double gcTime = stats.getGcTime(StatType.MEDIAN);
                final double runTime = stats.getCallTime(StatType.MEDIAN);
                runTimes.data().setDouble(key, style, runTime);
                totalTimes.data().setDouble(key, style, runTime +  gcTime);
                memory.data().setDouble(key, style, stats.getUsedMemory(StatType.MEDIAN));
            }
        });

        Chart.of(runTimes, chart -> {
            chart.plot(0).withBars(0d);
            chart.title().withText("Array Initialization Times, 5 Million Entries (Sample " + count + ")");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.axes().domain().label().withText("Data Type");
            chart.axes().range(0).label().withText("Time (Milliseconds)");
            chart.legend().on();
            chart.orientation().horizontal();
            chart.writerPng(new File("./morpheus-docs/docs/images/native-vs-morpheus-init-times.png"), 845, 345);
            chart.show();
        });

        Chart.of(totalTimes, chart -> {
            chart.plot(0).withBars(0d);
            chart.title().withText("Array Initialization + GC Times, 5 Million Entries (Sample " + count + ")");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.axes().domain().label().withText("Data Type");
            chart.axes().range(0).label().withText("Time (Milliseconds)");
            chart.legend().on();
            chart.orientation().horizontal();
            chart.writerPng(new File("./morpheus-docs/docs/images/native-vs-morpheus-gc-times.png"), 845, 345);
            chart.show();
        });

        Chart.of(memory, chart -> {
            chart.plot(0).withBars(0d);
            chart.title().withText("Array Memory Usage, 5 Million Entries (Sample " + count + ")");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.axes().domain().label().withText("Data Type");
            chart.axes().range(0).label().withText("Memory Usage (MB)");
            chart.legend().on();
            chart.orientation().horizontal();
            chart.writerPng(new File("./morpheus-docs/docs/images/native-vs-morpheus-memory.png"), 845, 345);
            chart.show();
        });
    }

    /**
     * Returns a newly created callable for the args specified
     * @param type  the array class type
     * @param size  the size of array
     * @param style the style
     * @return      the callable
     */
    private static Callable<Object> createCallable(Class<?> type, int size, int style) {
        switch (style) {
            case 0: return ArrayPerf1.createNativeCallable(type, size);
            case 1: return ArrayPerf2.createMorpheusCallable(type, size);
            default:    throw new IllegalArgumentException("Unsupported style specified: " + style);
        }
    }

}
