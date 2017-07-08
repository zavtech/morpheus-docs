package com.zavtech.morpheus.perf.io;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.util.PerfStat;

public class CsvParse {

    public static void main(String[] args) {

        final String path = "/Users/witdxav/Dropbox/data/fxcm/AUDUSD/2012/AUDUSD-2012.csv";
        final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        DataFrame<String,String> timingStats = PerfStat.run(5, TimeUnit.MILLISECONDS, false, tasks -> {

            tasks.put("Sequential", () -> DataFrame.read().<LocalDateTime>csv(options -> {
                options.setHeader(false);
                options.setParallel(false);
                options.setResource(path);
                options.setExcludeColumnIndexes(1);
                options.setRowKeyParser(LocalDateTime.class, row -> {
                    final LocalDate date = LocalDate.parse(row[0], dateFormat);
                    final LocalTime time = LocalTime.parse(row[1], timeFormat);
                    return LocalDateTime.of(date, time);
                });
            }));

            tasks.put("Parallel", () -> DataFrame.read().<LocalDateTime>csv(options -> {
                options.setHeader(false);
                options.setParallel(true);
                options.setResource(path);
                options.setExcludeColumnIndexes(1);
                options.setRowKeyParser(LocalDateTime.class, row -> {
                    final LocalDate date = LocalDate.parse(row[0], dateFormat);
                    final LocalTime time = LocalTime.parse(row[1], timeFormat);
                    return LocalDateTime.of(date, time);
                });
            }));

        });

        Chart.of(timingStats, chart -> {
            chart.plot(0).withBars(0d);
            chart.title().withText("CSV Parsing Performance (Sequential vs Parallel)");
            chart.subtitle().withText("File Size: 40MB, 760,000 lines, 6 columns");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 16));
            chart.axes().domain().label().withText("Statistic");
            chart.axes().range(0).label().withText("Time in Milliseconds");
            chart.legend().on();
            chart.show();
        });

    }

}
