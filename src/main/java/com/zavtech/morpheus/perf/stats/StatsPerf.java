package com.zavtech.morpheus.perf.stats;


import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.zavtech.morpheus.viz.chart.Chart;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.stats.Median;
import com.zavtech.morpheus.util.PerfStat;

public class StatsPerf {

    public static void main(String[] args) {
        median();
        //medianPerf();
    }


    private static void median() {

        final Median median = new Median();
        for (int i=0; i<1000; ++i) {
            median.add(Math.random());
        }
        System.out.println(median.getValue());

    }


    private static void medianPerf() {

        final double[] values = ThreadLocalRandom.current().doubles(10000000).toArray();

        final DataFrame<String,String> results = PerfStat.run(10, TimeUnit.MILLISECONDS, false, tasks -> {

            tasks.put("Morpheus", () -> {
                final Median median = new Median();
                for (double value : values) {
                    median.add(value);
                }
                return median.getValue();
            });

            tasks.put("Apache", () -> {
                final DescriptiveStatistics stats = new DescriptiveStatistics();
                for (double value : values) {
                    stats.addValue(value);
                }
                return stats.getPercentile(50d);
            });
        });

        //Plot timing statistics as a bar chart
        Chart.of(results, chart -> {
            chart.plot(0).withBars(0d);
            chart.title().withText("Median Calculation Times for Random Array of 10 Million elements");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.axes().domain().label().withText("Timing Statistic");
            chart.axes().range(0).label().withText("Total Time in Milliseconds");
            chart.legend().on();
            chart.show();
        });


    }
}
