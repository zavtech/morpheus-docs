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
package com.zavtech.morpheus.docs.stats;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.viz.chart.Chart;

public class PCADocs1 {


    private Set<String> tickers() throws Exception {
        final List<String> lines = Files.readAllLines(Paths.get(getClass().getResource("/sp500-tickers.csv").toURI()));
        return lines.stream().filter(l -> l.trim().length() > 0).sorted().collect(Collectors.toSet());
    }

    /**
     * Loads load from the WIKI prices database from Quandl
     * @param tickers       the set of tickers to include
     * @param start         the start date
     * @param end           the end date
     * @return              the DataFrame of prices
     */
    private DataFrame<Integer,String> getQuotes(Set<String> tickers, LocalDate start, LocalDate end) {
        return DataFrame.read().csv(options -> {
            options.setResource("/Users/witdxav/Dropbox/data/quandl/wiki-prices/wiki-prices.csv");
            options.setIncludeColumns("ticker", "date", "adj_open", "adj_high", "adj_low", "adj_close", "adj_volume");
            options.setColumnNameMapping((name, index) -> {
                switch (name) {
                    case "ticker":      return "Ticker";
                    case "date":        return "Date";
                    case "adj_open":    return "Open";
                    case "adj_high":    return "High";
                    case "adj_low":     return "Low";
                    case "adj_close":   return "Close";
                    case "adj_volume":  return "Volume";
                    default:            return name;
                }
            });
            options.setRowPredicate(values -> {
                final String symbol = values[0];
                if (tickers.contains(symbol)) {
                    final LocalDate date = LocalDate.parse(values[1]);
                    return start.compareTo(date) <= 0 && end.compareTo(date) >= 0;
                } else {
                    return false;
                }
            });
        });
    }

    @DataProvider(name="sp500")
    public Object[][] sp500() throws Exception {
        return new Object[][] {{ tickers() }};
    }


    @Test(dataProvider="sp500")
    public void splitPrices(Set<String> tickers) {
        System.out.println("Loading price data...");
        final DataFrame<Integer,String> allPrices = DataFrame.read().csv(options -> {
            options.setRowCapacity(1500000);
            options.setResource("/Users/witdxav/Dropbox/data/quandl/wiki-prices/wiki-prices.csv");
        });
        System.out.println("Sorting columns...");
        allPrices.cols().sort((col1, col2) -> {
            final String key1 = col1.key();
            final String key2 = col2.key();
            if (key1.equals("date")) return -1;
            else if (key2.equals("date")) return 1;
            else return 0;
        });
        allPrices.out().print();
        System.out.println("Writing files...");
        tickers.forEach(ticker -> {
            System.out.println("Processing " + ticker);
            final DataFrame<Integer,String> prices = allPrices.rows().select(row -> row.getValue("ticker").equals(ticker));
            if (prices.rowCount() > 0) {
                prices.write().csv(options -> {
                    options.setFile(new File("/Users/witdxav/Dropbox/data/quandl/wiki-prices/tickers/" + ticker.toLowerCase() + ".csv"));
                    options.setTitle("Date");
                    options.setIncludeRowHeader(false);
                });
            }
        });
    }



    private DataFrame<LocalDate,String> getMonthlyReturns(Set<String> tickers, LocalDate start, LocalDate end) {
        final DataFrame<Integer,String> quotes = getQuotes(tickers, start, end);
        final Array<LocalDate> dates = quotes.colAt("Date").<LocalDate>distinct().sort(true);
        final LocalDate minDate = dates.getValue(20);
        final Array<LocalDate> returnDates = dates.filter(v -> v.getValue().compareTo(minDate) > 0);
        final DataFrame<LocalDate,String> returns = DataFrame.ofDoubles(returnDates, tickers, v -> 0d);
        final long t1 = System.nanoTime();
        quotes.rows().forEach(row -> {
            final int rowOrdinal = row.ordinal();
            final LocalDate date = row.getValue("Date");
            if (date.compareTo(minDate) > 0) {
                final String ticker = row.getValue("Ticker");
                final double closePrice = row.getDouble("Close");
                final double priorClose = row.frame().data().getDouble(rowOrdinal-20, "Close");
                final double returnValue = closePrice / priorClose - 1d;
                returns.data().setDouble(date, ticker, returnValue);
            }
        });
        final long t2 = System.nanoTime();
        System.out.printf("\nComputed asset returns in %s millis", ((t2-t1)/1000000));
        return returns;
    }











    @Test()
    public void loadData() throws Exception {
        final Set<String> tickers = tickers();
        final LocalDate start = LocalDate.of(2013, 1, 1);
        final LocalDate end = LocalDate.of(2013, 12, 31);
        final DataFrame<LocalDate,String> returns = getMonthlyReturns(tickers, start, end);
        System.out.println(returns);
        returns.out().print();

        final DataFrame<String,String> cov = returns.cols().stats().covariance();
        final RealMatrix x = cov.export().asApacheMatrix();
        final EigenDecomposition decomposition = new EigenDecomposition(x);
        final DataFrame<Integer,String> eigenValues = DataFrame.ofDoubles(Range.of(0, tickers.size()), Array.of("EigenValues"), v -> {
           return decomposition.getRealEigenvalue(v.rowOrdinal());
        });

        eigenValues.rows().sort(false, "EigenValues");
        eigenValues.out().print(100);


        Chart.create().withBarPlot(eigenValues.rows().select(row -> row.ordinal() < 50), false, chart -> {
            chart.plot().data().at(0).withLowerDomainInterval(v -> v + 1);
            chart.show();
        });

        Thread.currentThread().join();
    }

}
