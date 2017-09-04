package com.zavtech.morpheus.perf.frame;

import java.awt.Font;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;

import com.zavtech.morpheus.viz.chart.Chart;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.frame.DataFrameValue;
import com.zavtech.morpheus.util.PerfStat;
import com.zavtech.morpheus.range.Range;

public class DataFrameApplyDoubles {

    public static void main(String[] args) {

        //Sample size for timing statistics
        int count = 10;

        //Create frame with 50 million rows of Random doubles
        Range<Integer> rowKeys = Range.of(0, 50000000);
        Array<String> colKeys = Array.of("A", "B", "C", "D");
        DataFrame<Integer,String> frame = DataFrame.ofDoubles(rowKeys, colKeys).applyDoubles(v -> Math.random());

        //Time sequential and parallel capping of all elements in the DataFrame
        ToDoubleFunction<DataFrameValue<Integer,String>> cap = (v) -> v.getDouble() > 0.5 ? 0.5 : v.getDouble();
        DataFrame<String,String> timing = PerfStat.run(count, TimeUnit.MILLISECONDS, true, tasks -> {
            tasks.beforeEach(() -> frame.applyDoubles(v -> Math.random()));
            tasks.put("Sequential", () -> frame.sequential().applyDoubles(cap));
            tasks.put("Parallel", () -> frame.parallel().applyDoubles(cap));
        });

        //Plot timing statistics as a bar chart
        Chart.create().withBarPlot(timing, false, chart -> {
            chart.title().withText("Time to Cap 200 Million DataFrame Elements (Sample 10 times)");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Timing Statistic");
            chart.plot().axes().range(0).label().withText("Total Time in Milliseconds");
            chart.legend().on();
            chart.writerPng(new File("./docs/images/frame/data-frame-apply-doubles.png"), 845, 400, true);
            chart.show();
        });
    }
}
