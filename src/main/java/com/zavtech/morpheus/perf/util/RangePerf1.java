package com.zavtech.morpheus.perf.util;

import java.awt.*;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import com.zavtech.morpheus.viz.chart.Chart;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.util.PerfStat;
import com.zavtech.morpheus.range.Range;

public class RangePerf1 {

    public static void main(String[] args) {

        final int sample = 10;
        final int size = 10000000;

        final PerfStat stats1 = PerfStat.run("ZDT-1", sample, TimeUnit.MILLISECONDS, () -> {
            final ZonedDateTime start = ZonedDateTime.now();
            final ZonedDateTime end = start.plusSeconds(size);
            final Range<ZonedDateTime> range = Range.of(start, end, Duration.ofSeconds(1));
            final long t1 = System.nanoTime();
            final Array<ZonedDateTime> array = range.toArray(true);
            final long t2 = System.nanoTime();
            System.out.println("Array-1 created in " + ((t2-t1)/1000000) + " millis, length=" + array.length());
            return array;
        });

        final PerfStat stats2 = PerfStat.run("ZDT-2", sample, TimeUnit.MILLISECONDS, () -> {
            ZonedDateTime value = ZonedDateTime.now();
            final long t1 = System.nanoTime();
            final Array<ZonedDateTime> array = Array.of(ZonedDateTime.class, size);
            for (int i=0; i<array.length(); ++i) {
                array.setValue(i, value);
                value = value.plusSeconds(1);
            }
            final long t2 = System.nanoTime();
            System.out.println("Array-2 created in " + ((t2-t1)/1000000) + " millis, length=" + array.length());
            return array;
        });

        final PerfStat stats3 = PerfStat.run("ZDT-3", sample, TimeUnit.MILLISECONDS, () -> {
            final ZonedDateTime start = ZonedDateTime.now();
            final ZonedDateTime end = start.plusSeconds(size);
            final Range<ZonedDateTime> range = Range.of(start, end, Duration.ofSeconds(1));
            final long t1 = System.nanoTime();
            int count = 0;
            final Iterator<ZonedDateTime> iterator = range.iterator();
            final Array<ZonedDateTime> array = Array.of(ZonedDateTime.class, size);
            while (iterator.hasNext()) {
                array.setValue(count++, iterator.next());
            }
            final long t2 = System.nanoTime();
            System.out.println("Array-3 created in " + ((t2-t1)/1000000) + " millis, length=" + array.length());
            return array;
        });

        final DataFrame<String,String> times = DataFrame.combineFirst(
            stats1.getRunStats(),
            stats2.getRunStats(),
            stats3.getRunStats()
        );

        //Plot initialization times
        String title = "DataFrame Create Times, 5 million rows by 10 columns of doubles (Sample " + sample + ")";
        Chart.of(times, chart -> {
            chart.plot(0).withBars(0d);
            chart.title().withText(title);
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.axes().domain().label().withText("Timing Statistic");
            chart.axes().range(0).label().withText("Time (Milliseconds)");
            chart.legend().on().bottom();
            chart.show();
        });


    }
}
