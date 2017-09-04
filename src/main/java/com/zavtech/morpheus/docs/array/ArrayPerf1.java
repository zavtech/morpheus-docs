package com.zavtech.morpheus.docs.array;

import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import com.zavtech.morpheus.viz.chart.Chart;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.stats.StatType;
import com.zavtech.morpheus.util.PerfStat;

public class ArrayPerf1 {

    public static void main(String[] args) throws Exception {

        final int count = 5;
        final int size = 1000000;

        final Map<String,Callable<Object>> callableMap = new LinkedHashMap<>();
        callableMap.put("boolean", createNativeCallable(boolean.class, size));
        callableMap.put("int", createNativeCallable(int.class, size));
        callableMap.put("long", createNativeCallable(long.class, size));
        callableMap.put("double", createNativeCallable(double.class, size));
        callableMap.put("Boolean", createNativeCallable(Boolean.class, size));
        callableMap.put("Integer", createNativeCallable(Integer.class, size));
        callableMap.put("Long", createNativeCallable(Long.class, size));
        callableMap.put("Double", createNativeCallable(Double.class, size));
        callableMap.put("Date", createNativeCallable(Date.class, size));
        callableMap.put("LocalDate", createNativeCallable(LocalDate.class, size));
        callableMap.put("String", createNativeCallable(String.class, size));
        callableMap.put("ZonedDateTime", createNativeCallable(ZonedDateTime.class, size));

        final Set<String> rowKeys = callableMap.keySet();
        final DataFrame<String,String> gcTimes = DataFrame.ofDoubles(rowKeys, "GCTime");
        final DataFrame<String,String> initTimes = DataFrame.ofDoubles(rowKeys, "InitTime");
        final DataFrame<String,String> memory = DataFrame.ofDoubles(rowKeys, "Memory");
        for (String key : callableMap.keySet()) {
            System.out.printf("\nRunning array tests for %s", key);
            final Callable<Object> callable = callableMap.get(key);
            final PerfStat perfStat = PerfStat.run(key, count, TimeUnit.MILLISECONDS, callable);
            initTimes.data().setDouble(key, 0, perfStat.getCallTime(StatType.MEDIAN));
            gcTimes.data().setDouble(key, 0, perfStat.getGcTime(StatType.MEDIAN));
            memory.data().setDouble(key, 0, perfStat.getUsedMemory(StatType.MEDIAN));
        }

        Chart.create().withBarPlot(initTimes, false, chart -> {
            chart.title().withText("Native Array Median Initialisation Times, 1 Million Entries (Sample " + count + ")");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Data Type");
            chart.plot().axes().range(0).label().withText("Time (Milliseconds)");
            chart.plot().orient().horizontal();
            chart.legend().off();
            chart.writerPng(new File("./docs/images/native-array-create-times.png"), 845, 360, true);
            chart.show();
        });

        Chart.create().withBarPlot(gcTimes, false, chart -> {
            chart.title().withText("Native Array Median Garbage Collection Times, 1 Million Entries (Sample " + count + ")");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Data Type");
            chart.plot().axes().range(0).label().withText("Time (Milliseconds)");
            chart.plot().orient().horizontal();
            chart.legend().off();
            chart.writerPng(new File("./docs/images/native-array-gc-times.png"), 845, 360, true);
            chart.show();
        });

        Chart.create().withBarPlot(memory, false, chart -> {
            chart.title().withText("Native Array Median Memory Usage, 1 Million Entries (Sample " + count + ")");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Data Type");
            chart.plot().axes().range(0).label().withText("Memory Usage (MB)");
            chart.plot().orient().horizontal();
            chart.legend().on();
            chart.writerPng(new File("./docs/images/native-array-memory.png"), 845, 360, true);
            chart.show();
        });
    }


    /**
     * Returns a callable to create an Native array of the type specified
     * @param type  the type for array
     * @param size  the size for array
     * @return      the callable for performance test
     */
    static Callable<Object> createNativeCallable(Class<?> type, int size) {
        if (type == boolean.class) {
            return () -> {
                final boolean[] array = new boolean[size];
                IntStream.range(0, array.length).forEach(i -> array[i] = i % 2 == 0);
                return array;
            };
        } else if (type == int.class) {
            return () -> {
                final int[] array = new int[size];
                IntStream.range(0, array.length).forEach(i -> array[i] = i);
                return array;
            };
        } else if (type == long.class) {
            return () -> {
                final long[] array = new long[size];
                IntStream.range(0, array.length).forEach(i -> array[i] = i);
                return array;
            };
        } else if (type == double.class) {
            return () -> {
                final double[] array = new double[size];
                IntStream.range(0, array.length).forEach(i -> array[i] = i);
                return array;
            };
        } else if (type == Boolean.class) {
            return () -> {
                final Boolean[] array = new Boolean[size];
                IntStream.range(0, array.length).forEach(i -> array[i] = i % 2 == 0);
                return array;
            };
        } else if (type == Integer.class) {
            return () -> {
                final Integer[] array = new Integer[size];
                IntStream.range(0, array.length).forEach(i -> array[i] = i);
                return array;
            };
        } else if (type == Long.class) {
            return () -> {
                final Long[] array = new Long[size];
                IntStream.range(0, array.length).forEach(i -> array[i] = (long)i);
                return array;
            };
        } else if (type == Double.class) {
            return () -> {
                final Double[] array = new Double[size];
                IntStream.range(0, array.length).forEach(i -> array[i] = (double)i);
                return array;
            };
        } else if (type == Date.class) {
            return () -> {
                final Date[] array = new Date[size];
                IntStream.range(0, array.length).forEach(i -> array[i] = new Date(i * 1000));
                return array;
            };
        } else if (type == LocalDate.class) {
            return () -> {
                final LocalDate now = LocalDate.now();
                final LocalDate[] array = new LocalDate[size];
                IntStream.range(0, array.length).forEach(i -> array[i] = now.plusDays(i));
                return array;
            };
        } else if (type == String.class) {
            return () -> {
                final String[] array = new String[size];
                IntStream.range(0, array.length).forEach(i -> array[i] = String.valueOf(i));
                return array;
            };
        } else if (type == ZonedDateTime.class) {
            return () -> {
                final ZonedDateTime now = ZonedDateTime.now();
                final ZonedDateTime[] array = new ZonedDateTime[size];
                IntStream.range(0, array.length).forEach(i -> array[i] = now.plusMinutes(i));
                return array;
            };
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }
}
