package com.zavtech.morpheus.perf.index;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.zavtech.morpheus.viz.chart.Chart;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.index.Index;
import com.zavtech.morpheus.util.PerfStat;
import com.zavtech.morpheus.range.Range;

public class IndexPerf1 {

    public static void main(String[] args) {

        final int size = 10000000;

        DataFrame<String,String> times = PerfStat.run(10, TimeUnit.MILLISECONDS, false, tasks -> {

            tasks.put("int", () -> {
                final Range<Integer> range = Range.of(0, size);
                return Index.of(range);
            });

            tasks.put("Date", () -> {
                final long now = System.currentTimeMillis();
                final Range<Date> range = Range.of(0, size).map(i -> new Date(now + (i * 1000)));
                return Index.of(range);
            });
            tasks.put("Instant", () -> {
                final long now = System.currentTimeMillis();
                final Range<Instant> range = Range.of(0, size).map(i -> Instant.ofEpochMilli(now + (i * 1000)));
                return Index.of(range);
            });

            tasks.put("LocalDateTime", () -> {
                final Duration step = Duration.ofSeconds(1);
                final LocalDateTime start = LocalDateTime.now().minusYears(10);
                final LocalDateTime end = start.plusSeconds(size);
                final Range<LocalDateTime> range = Range.of(start, end, step);
                return Index.of(range);
            });

            tasks.put("ZonedDateTime", () -> {
                final Duration step = Duration.ofSeconds(1);
                final ZonedDateTime start = ZonedDateTime.now();
                final ZonedDateTime end = start.plusSeconds(size);
                final Range<ZonedDateTime> range = Range.of(start, end, step);
                return Index.of(range);
            });
        });

        Chart.create().withBarPlot(times, false, chart -> {
            chart.title().withText("Median Index Creation Times, 10 million entries");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Timing Statistic");
            chart.plot().axes().range(0).label().withText("Time (Milliseconds)");
            chart.legend().on().bottom();
            //chart.writerPng(new File("./morpheus-docs/docs/images/data-frame-create-times.png"), 845, 400);
            chart.show();
        });

    }
}
