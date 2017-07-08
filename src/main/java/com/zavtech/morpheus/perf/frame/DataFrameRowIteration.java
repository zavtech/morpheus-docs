package com.zavtech.morpheus.perf.frame;

import java.awt.Font;
import java.io.File;
import java.util.concurrent.TimeUnit;

import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.util.PerfStat;

public class DataFrameRowIteration {

    public static void main(String[] args) {

        //Sample size for timing statistics
        int sample = 10;

        //Create frame with 50 million rows of Random doubles
        Range<Integer> rowKeys = Range.of(0, 10000000);
        Array<String> colKeys = Array.of("A", "B", "C", "D", "E", "F", "H");
        DataFrame<Integer,String> frame = DataFrame.ofDoubles(rowKeys, colKeys).applyDoubles(v -> Math.random());

        //Time sequential and parallel computation of mean over all rows
        DataFrame<String,String> timing = PerfStat.run(sample, TimeUnit.MILLISECONDS, false, tasks -> {
            tasks.put("Sequential", () -> {
                frame.sequential().rows().forEach(row -> row.stats().mean());
                return frame;
            });
            tasks.put("Parallel", () -> {
                frame.parallel().rows().forEach(row -> row.stats().mean());
                return frame;
            });
        });

        //Plot timing statistics as a bar chart
        Chart.of(timing, chart -> {
            chart.plot(0).withBars(0d);
            chart.title().withText("Time to Compute Arithmetic Mean of 50 Million rows (Sample 10 times)");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.axes().domain().label().withText("Timing Statistic");
            chart.axes().range(0).label().withText("Total Time in Milliseconds");
            chart.legend().on();
            chart.writerPng(new File("./morpheus-docs/docs/images/data-frame-row-iteration.png"), 845, 400);
            chart.show();
        });
    }
}
