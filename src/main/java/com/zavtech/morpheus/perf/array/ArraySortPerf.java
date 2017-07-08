package com.zavtech.morpheus.perf.array;

import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.zavtech.morpheus.viz.chart.Chart;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.util.PerfStat;

public class ArraySortPerf {

    public static void main(String[] args) {
        testWithDoubles(5);
        testWithLocalDatesTimes(5);
    }



    /**
     * Tests Native sort performance for a range of lengths
     * @param sample    the number of samples to run
     */
    private static void testWithDoubles(int sample) {

        Range<Integer> arrayLengths = Range.of(1, 11).map(i -> i * 100000);
        Array<String> labels = Array.of("Native(Seq)", "Morpheus(Seq)", "Native(Par)", "Morpheus(Par)");
        DataFrame<String,String> results = DataFrame.ofDoubles(arrayLengths.map(String::valueOf), labels);

        arrayLengths.forEach(length -> {

            System.out.println("Running sort test for array length " + length);
            double[] array1 = new double[length];
            Array<Double> array2 = Array.of(Double.class, length);

            DataFrame<String,String> timing = PerfStat.run(sample, TimeUnit.MILLISECONDS, false, tasks -> {
                tasks.put("Native(Seq)", () -> { Arrays.sort(array1); return array1; });
                tasks.put("Morpheus(Seq)", () -> array2.sort(true) );
                tasks.put("Native(Par)", () -> { Arrays.parallelSort(array1); return array1; });
                tasks.put("Morpheus(Par)", () -> array2.parallel().sort(true));
                tasks.beforeEach(() -> {
                    array2.applyDoubles(v -> Math.random());
                    array2.forEachValue(v -> array1[v.index()] = v.getDouble());
                });
            });

            String label = String.valueOf(length);
            results.data().setDouble(label, "Native(Seq)", timing.data().getDouble("Mean", "Native(Seq)"));
            results.data().setDouble(label, "Morpheus(Seq)", timing.data().getDouble("Mean", "Morpheus(Seq)"));
            results.data().setDouble(label, "Native(Par)", timing.data().getDouble("Mean", "Native(Par)"));
            results.data().setDouble(label, "Morpheus(Par)", timing.data().getDouble("Mean", "Morpheus(Par)"));
        });

        Chart.of(results, chart -> {
            chart.plot(0).withBars(0d);
            chart.title().withText("Sorting Performance for Array of Random Doubles (Sample " + sample + ")");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 16));
            chart.subtitle().withText("Dual-Pivot Quick Sort (Native) vs Single-Pivot Quick Sort (FastUtil)");
            chart.subtitle().withFont(new Font("Verdana", Font.PLAIN, 14));
            chart.axes().domain().label().withText("Array Length");
            chart.axes().range(0).label().withText("Total Time in Milliseconds");
            chart.legend().on();
            chart.writerPng(new File("./morpheus-docs/docs/images/array-sort-native-vs-morpheus-1.png"), 845, 400);
            chart.show();
        });
    }



    private static void testWithLocalDatesTimes(int sample) {

        Range<Integer> arrayLengths = Range.of(1, 11).map(i -> i * 100000);
        Array<String> labels = Array.of("Native(Seq)", "Morpheus(Seq)", "Native(Par)", "Morpheus(Par)");
        DataFrame<String,String> results = DataFrame.ofDoubles(arrayLengths.map(String::valueOf), labels);

        arrayLengths.forEach(length -> {

            System.out.println("Running sort test for array length " + length);
            final LocalDateTime start = LocalDateTime.now();
            final LocalDateTime[] array1 = new LocalDateTime[length];
            final Array<LocalDateTime> array2 = Range.of(0, length.intValue()).map(start::plusSeconds).toArray();

            DataFrame<String,String> timing = PerfStat.run(sample, TimeUnit.MILLISECONDS, false, tasks -> {
                tasks.put("Native(Seq)", () -> { Arrays.sort(array1); return array1; });
                tasks.put("Morpheus(Seq)", () -> array2.sort(true) );
                tasks.put("Native(Par)", () -> { Arrays.parallelSort(array1); return array1; });
                tasks.put("Morpheus(Par)", () -> array2.parallel().sort(true));
                tasks.beforeEach(() -> {
                    array2.shuffle(2);
                    array2.forEachValue(v -> array1[v.index()] = v.getValue());
                });
            });

            String label = String.valueOf(length);
            results.data().setDouble(label, "Native(Seq)", timing.data().getDouble("Mean", "Native(Seq)"));
            results.data().setDouble(label, "Morpheus(Seq)", timing.data().getDouble("Mean", "Morpheus(Seq)"));
            results.data().setDouble(label, "Native(Par)", timing.data().getDouble("Mean", "Native(Par)"));
            results.data().setDouble(label, "Morpheus(Par)", timing.data().getDouble("Mean", "Morpheus(Par)"));
        });

        Chart.of(results, chart -> {
            chart.plot(0).withBars(0d);
            chart.title().withText("Sorting Performance for Array of Random LocalDateTimes (Sample " + sample + ")");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 16));
            chart.subtitle().withText("Dual-Pivot Quick Sort (Native) vs Single-Pivot Quick Sort (FastUtil)");
            chart.subtitle().withFont(new Font("Verdana", Font.PLAIN, 14));
            chart.axes().domain().label().withText("Array Length");
            chart.axes().range(0).label().withText("Total Time in Milliseconds");
            chart.legend().on();
            chart.writerPng(new File("./morpheus-docs/docs/images/array-sort-native-vs-morpheus-2.png"), 845, 400);
            chart.show();
        });
    }


}
