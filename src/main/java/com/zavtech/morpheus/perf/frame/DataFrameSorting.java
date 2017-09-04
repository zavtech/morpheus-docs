package com.zavtech.morpheus.perf.frame;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.zavtech.morpheus.viz.chart.Chart;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.util.PerfStat;

public class DataFrameSorting {

    public static void main(String[] args) {

        int sample = 5;

        int[] lengths = IntStream.range(1, 11).map(i -> i * 1000000).toArray();

        DataFrame<String,String> results = DataFrame.ofDoubles(
            IntStream.of(lengths).mapToObj(String::valueOf).collect(Collectors.toList()),
            Arrays.asList("Sequential", "Parallel")
        );

        for (int length : lengths) {

            System.out.println("Running sort test for frame length " + length);
            Array<Integer> rowKeys = Range.of(0, length).toArray().shuffle(2);
            Array<String> colKeys = Array.of("A", "B", "C", "D");
            DataFrame<Integer,String> frame = DataFrame.ofDoubles(rowKeys, colKeys).applyDoubles(v -> Math.random());

            DataFrame<String,String> timing = PerfStat.run(sample, TimeUnit.MILLISECONDS, false, tasks -> {
                tasks.beforeEach(() -> frame.rows().sort(null));
                tasks.put("Sequential", () -> frame.rows().sequential().sort(true, "A"));
                tasks.put("Parallel", () -> frame.rows().parallel().sort(true, "A"));
            });

            String label = String.valueOf(length);
            results.data().setDouble(label, "Sequential", timing.data().getDouble("Mean", "Sequential"));
            results.data().setDouble(label, "Parallel", timing.data().getDouble("Mean", "Parallel"));
        }

        //Plot timing statistics as a bar chart
        Chart.create().withBarPlot(results, false, chart -> {
            chart.title().withText("Time to Sort Morpheus DataFrame of random doubles (Sample 5 times)");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Timing Statistic");
            chart.plot().axes().range(0).label().withText("Total Time in Milliseconds");
            chart.legend().on();
            chart.writerPng(new File("./morpheus-docs/docs/images/data-frame-sort.png"), 845, 400, true);
            chart.show();
        });
    }

}
