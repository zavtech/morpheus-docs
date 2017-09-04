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

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.stats.StatType;
import com.zavtech.morpheus.util.PerfStat;

public class ArrayPerf2 {

    public static void main(String[] args) throws Exception {

        final int count = 5;
        final int size = 1000000;

        final Map<String,Callable<Object>> callableMap = new LinkedHashMap<>();
        callableMap.put("boolean", createMorpheusCallable(boolean.class, size));
        callableMap.put("int", createMorpheusCallable(int.class, size));
        callableMap.put("long", createMorpheusCallable(long.class, size));
        callableMap.put("double", createMorpheusCallable(double.class, size));
        callableMap.put("Date", createMorpheusCallable(Date.class, size));
        callableMap.put("LocalDate", createMorpheusCallable(LocalDate.class, size));
        callableMap.put("String", createMorpheusCallable(String.class, size));
        callableMap.put("ZonedDateTime", createMorpheusCallable(ZonedDateTime.class, size));

        final Set<String> rowKeys = callableMap.keySet();
        final DataFrame<String,String> gcTimes = DataFrame.ofDoubles(rowKeys, "GCTime");
        final DataFrame<String,String> initTimes = DataFrame.ofDoubles(rowKeys, "InitTime");
        final DataFrame<String,String> memory = DataFrame.ofDoubles(rowKeys, "Memory");
        for (String key : callableMap.keySet()) {
            final Callable<Object> callable = callableMap.get(key);
            final PerfStat stats = PerfStat.run(key, count, TimeUnit.MILLISECONDS, callable);
            initTimes.data().setDouble(key, 0, stats.getCallTime(StatType.MEDIAN));
            gcTimes.data().setDouble(key, 0, stats.getGcTime(StatType.MEDIAN));
            memory.data().setDouble(key, 0, stats.getUsedMemory(StatType.MEDIAN));
        }

        Chart.create().withBarPlot(initTimes, false, chart -> {
            chart.title().withText("Morpheus Array Median Initialisation Times, 1 Million Entries (Sample " + count + ")");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Data Type");
            chart.plot().axes().range(0).label().withText("Time (Milliseconds)");
            chart.plot().orient().horizontal();
            chart.legend().on();
            chart.writerPng(new File("./docs/images/morpheus-array-init-times.png"), 845, 345, true);
            chart.show();
        });

        Chart.create().withBarPlot(gcTimes, false, chart -> {
            chart.title().withText("Morpheus Array Median Garbage Collection Times, 1 Million Entries (Sample " + count + ")");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Data Type");
            chart.plot().axes().range(0).label().withText("Time (Milliseconds)");
            chart.plot().orient().horizontal();
            chart.legend().off();
            chart.writerPng(new File("./docs/images/morpheus-array-gc-times.png"), 845, 360, true);
            chart.show();
        });

        Chart.create().withBarPlot(memory, false, chart -> {
            chart.title().withText("Morpheus Array Median Memory Usage, 1 Million Entries (Sample " + count + ")");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Data Type");
            chart.plot().axes().range(0).label().withText("Memory Usage (MB)");
            chart.plot().orient().horizontal();
            chart.legend().on();
            chart.writerPng(new File("./docs/images/morpheus-array-memory.png"), 845, 345, true);
            chart.show();
        });

    }

    /**
     * Returns a callable to create an Morpheus array of the type specified
     * @param type  the type for array
     * @param size  the size for array
     * @return      the callable for performance test
     */
    static Callable<Object> createMorpheusCallable(Class<?> type, int size) {
        if (type == boolean.class) {
            return () -> {
                final Array<Boolean> array = Array.of(Boolean.class, size);
                IntStream.range(0, array.length()).forEach(i -> array.setBoolean(i, i % 2 == 0));
                return array;
            };
        } else if (type == int.class) {
            return () -> {
                final Array<Integer> array = Array.of(Integer.class, size);
                IntStream.range(0, array.length()).forEach(i -> array.setInt(i, i));
                return array;
            };
        } else if (type == long.class) {
            return () -> {
                final Array<Long> array = Array.of(Long.class, size);
                IntStream.range(0, array.length()).forEach(i -> array.setLong(i, i));
                return array;
            };
        } else if (type == double.class) {
            return  () -> {
                final Array<Double> array = Array.of(Double.class, size);
                IntStream.range(0, array.length()).forEach(i -> array.setDouble(i, i));
                return array;
            };
        } else if (type == Date.class) {
            return () -> {
                final Array<Date> array = Array.of(Date.class, size);
                IntStream.range(0, array.length()).forEach(i -> array.setValue(i, new Date(i)));
                return array;
            };
        } else if (type == LocalDate.class) {
            return () -> {
                final LocalDate now = LocalDate.now();
                final Array<LocalDate> array = Array.of(LocalDate.class, size);
                IntStream.range(0, array.length()).forEach(i -> array.setValue(i, now.plusDays(i)));
                return array;
            };
        } else if (type == String.class) {
            return () -> {
                final Array<String> array = Array.of(String.class, size);
                IntStream.range(0, array.length()).forEach(i -> array.setValue(i, String.valueOf(i)));
                return array;
            };
        } else if (type == ZonedDateTime.class) {
            return () -> {
                final ZonedDateTime now = ZonedDateTime.now();
                final Array<ZonedDateTime> array = Array.of(ZonedDateTime.class, size);
                IntStream.range(0, array.length()).forEach(i -> array.setValue(i, now.plusMinutes(i)));
                return array;
            };
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }
}
