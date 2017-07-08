/**
 * Copyright (C) 2014-2017 Xavier Witdouck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zavtech.morpheus.docs;

import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Year;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.util.text.parser.Parser;
import com.zavtech.morpheus.viz.chart.Chart;

/**
 * An example that computes median house prices of apartments in 6 UK cities.
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public class LondonHousePriceAnalysis {

    public static void main(String[] args) throws Exception {

        String resource = "/Users/witdxav/Dropbox/data/uk-house-prices/uk-house-prices-%s.csv";

        //Create a data frame to capture the median prices of Apartments in the UK'a largest cities
        DataFrame<Year,String> results = DataFrame.ofDoubles(
            Range.of(1995, 2015).map(Year::of),
            Array.of("LONDON", "BIRMINGHAM", "SHEFFIELD", "LEEDS", "LIVERPOOL", "MANCHESTER")
        );

        final long t1 = System.currentTimeMillis();

        //Process yearly data in parallel to leverage all CPU cores
        results.rows().keys().forEach(year -> {
            System.out.printf("Loading UK house prices for %s...\n", year);
            DataFrame.read().csv(options -> {
                options.setResource(String.format(resource, year.getValue()));
                options.setHeader(false);
                options.setCharset(StandardCharsets.UTF_8);
                options.setIncludeColumnIndexes(1, 2, 4, 11);
                options.getFormats().setParser("TransactDate", Parser.ofLocalDate("yyyy-MM-dd HH:mm"));
                options.setColumnNameMapping((colName, colOrdinal) -> {
                    switch (colOrdinal) {
                        case 0:     return "PricePaid";
                        case 1:     return "TransactDate";
                        case 2:     return "PropertyType";
                        case 3:     return "City";
                        default:    return colName;
                    }
                });
            }).rows().select(row -> {
                //Filter rows to include only apartments in the relevant cities
                final String propType = row.getValue("PropertyType");
                final String city = row.getValue("City");
                final String cityUpperCase = city != null ? city.toUpperCase() : null;
                return propType != null && propType.equals("F") && results.cols().contains(cityUpperCase);
            }).rows().groupBy("City").forEach(0, (groupKey, group) -> {
                //Group row filtered frame so we can compute median prices in selected cities
                final String city = groupKey.item(0);
                final double priceStat = group.colAt("PricePaid").stats().median();
                results.data().setDouble(year, city, priceStat);
            });
        });

        //Map row keys to LocalDates, and map values to be percentage changes from start date
        final DataFrame<LocalDate,String> plotFrame = results.mapToDoubles(v -> {
            final double firstValue = v.col().getDouble(0);
            final double currentValue = v.getDouble();
            return (currentValue / firstValue - 1d) * 100d;
        }).rows().mapKeys(row -> {
            final Year year = row.key();
            return LocalDate.of(year.getValue(), 12, 31);
        });

        final long t2 = System.currentTimeMillis();

        System.out.printf("Analysis completed in %s millis\n", (t2-t1));

        //Create a plot, and display it
        Chart.of(plotFrame, chart -> {
            chart.title().withText("Median Nominal House Price Changes");
            chart.title().withFont(new Font("Arial", Font.BOLD, 14));
            chart.subtitle().withText("Date Range: 1995 - 2014");
            chart.axes().domain().label().withText("Year");
            chart.axes().range(0).label().withText("Percent Change from 1995");
            chart.axes().range(0).format().withPattern("0.##'%';-0.##'%'");
            chart.legend().on().bottom();
            chart.style("LONDON").withColor(Color.BLACK);
            chart.writerPng(new File("./morpheus-docs/docs/images/uk-house-prices.png"), 845, 480);
            chart.show();
        });
    }
}
