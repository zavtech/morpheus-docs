package com.zavtech.morpheus.perf.array;

import java.awt.*;
import java.io.File;
import java.util.concurrent.TimeUnit;

import com.zavtech.morpheus.viz.chart.Chart;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.util.PerfStat;

public class ArrayStatsPerf {

    public static void main(String[] args) {
        final int count = 10;
        final int size = 10000000;
        final Array<Double> array = Array.of(Double.class, size).applyDoubles(v -> Math.random() * 100);

        final DataFrame<String,String> times = PerfStat.run(count, TimeUnit.MILLISECONDS, true, tasks -> {
            tasks.put("Min", () -> array.stats().min());
            tasks.put("Max", () -> array.stats().max());
            tasks.put("Mean", () -> array.stats().mean());
            tasks.put("Count", () -> array.stats().count());
            tasks.put("Variance", () -> array.stats().variance());
            tasks.put("StdDev", () -> array.stats().stdDev());
            tasks.put("Sum", () -> array.stats().sum());
            tasks.put("Skew", () -> array.stats().skew());
            tasks.put("Kurtosis", () -> array.stats().kurtosis());
            tasks.put("Median", () -> array.stats().median());
            tasks.put("95th Percentile", () -> array.stats().percentile(95));
            tasks.put("AutCorrelation(20)", () -> array.stats().autocorr(20));
        });

        Chart.of(times.rows().select("Mean").transpose(), chart -> {
            chart.plot(0).withBars(0d);
            chart.title().withText("Morpheus Array Statistic Calculation Times, 10 Million Entries (Sample 10)");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.axes().domain().label().withText("Stat Type");
            chart.axes().range(0).label().withText("Time (Milliseconds)");
            chart.legend().off();
            chart.orientation().horizontal();
            chart.writerPng(new File("./morpheus-docs/docs/images/morpheus-stat-times.png"), 845, 400);
            chart.show();
        });
    }

}
