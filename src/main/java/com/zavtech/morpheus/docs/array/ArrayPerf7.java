package com.zavtech.morpheus.docs.array;

import java.awt.*;
import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.zavtech.morpheus.viz.chart.Chart;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.util.PerfStat;

public class ArrayPerf7 {

    public static void main(String[] args) {

        final int sample = 5;
        final boolean includeGC = true;

        Range<Integer> arrayLengths = Range.of(1, 11).map(i -> i * 100000);
        Array<String> labels = Array.of("Native(Seq)", "Morpheus(Seq)", "Native(Par)", "Morpheus(Par)");
        DataFrame<String,String> results = DataFrame.ofDoubles(arrayLengths.map(String::valueOf), labels);

        arrayLengths.forEach(arrayLength -> {

            System.out.printf("\nRunng tests with array length of %s", arrayLength);

            DataFrame<String,String> timing = PerfStat.run(sample, TimeUnit.MILLISECONDS, includeGC, tasks -> {

                tasks.put("Native(Seq)", () -> {
                    final AtomicInteger count = new AtomicInteger();
                    final LocalDateTime start = LocalDateTime.now().minusYears(5);
                    final LocalDateTime[] array = new LocalDateTime[arrayLength];
                    for (int i=0; i<array.length; ++i) {
                        array[i] = start.plusMinutes(i);
                    }
                    for (LocalDateTime value : array) {
                        if (value.getDayOfWeek() == DayOfWeek.MONDAY) {
                            count.incrementAndGet();
                        }
                    }
                    return array;
                });

                tasks.put("Morpheus(Seq)", () -> {
                    final AtomicInteger count = new AtomicInteger();
                    final LocalDateTime start = LocalDateTime.now().minusYears(5);
                    final Array<LocalDateTime> array = Array.of(LocalDateTime.class, arrayLength);
                    array.applyValues(v -> start.plusMinutes(v.index()));
                    array.forEach(value -> {
                        if (value.getDayOfWeek() == DayOfWeek.MONDAY) {
                            count.incrementAndGet();
                        }
                    });
                    return array;
                });

                tasks.put("Native(Par)", () -> {
                    final AtomicInteger count = new AtomicInteger();
                    final LocalDateTime start = LocalDateTime.now().minusYears(5);
                    final IntStream indexes = IntStream.range(0, arrayLength).parallel();
                    final Stream<LocalDateTime> dates = indexes.mapToObj(start::plusMinutes);
                    final LocalDateTime[] array = dates.toArray(LocalDateTime[]::new);
                    Stream.of(array).parallel().forEach(value -> {
                        if (value.getDayOfWeek() == DayOfWeek.MONDAY) {
                            count.incrementAndGet();
                        }
                    });
                    return array;
                });

                tasks.put("Morpheus(Par)", () -> {
                    final AtomicInteger count = new AtomicInteger();
                    final LocalDateTime start = LocalDateTime.now().minusYears(5);
                    final Array<LocalDateTime> array = Array.of(LocalDateTime.class, arrayLength);
                    array.parallel().applyValues(v -> start.plusMinutes(v.index()));
                    array.parallel().forEach(value -> {
                        if (value.getDayOfWeek() == DayOfWeek.MONDAY) {
                            count.incrementAndGet();
                        }
                    });
                    return array;
                });

            });

            String label = String.valueOf(arrayLength);
            results.data().setDouble(label, "Native(Seq)", timing.data().getDouble("Mean", "Native(Seq)"));
            results.data().setDouble(label, "Morpheus(Seq)", timing.data().getDouble("Mean", "Morpheus(Seq)"));
            results.data().setDouble(label, "Native(Par)", timing.data().getDouble("Mean", "Native(Par)"));
            results.data().setDouble(label, "Morpheus(Par)", timing.data().getDouble("Mean", "Morpheus(Par)"));

        });

        //Create title from template
        final String prefix = "LocalDateTime Array Initialization + Traversal Times";
        final String title = prefix + (includeGC ? " (including-GC)" : " (excluding-GC)");

        //Record chart to file
        final String fileSuffix = includeGC ? "2.png" : "1.png";
        final String filePrefix = "./docs/images/native-vs-morpheus-array-sequential-vs-parallel";

        //Plot results as a bar chart
        Chart.create().withBarPlot(results, false, chart -> {
            chart.title().withText(title);
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Array Length");
            chart.plot().axes().range(0).label().withText("Time (Milliseconds)");
            chart.legend().on();
            chart.writerPng(new File(filePrefix + fileSuffix), 845, 400, true);
            chart.show();
        });
    }
}
