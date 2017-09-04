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
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.testng.annotations.Test;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.frame.DataFramePCA;
import com.zavtech.morpheus.frame.DataFrameRow;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.ChartShape;

public class EquityReturns {


    /**
     * Returns a DataFrame of load based on Wiki Prices loaded from Quandl.
     * @param ticker    the security ticker
     * @param start     the start date for selection
     * @param end       the end date for selection
     * @return          the DataFrame of load
     */
    DataFrame<LocalDate,String> loadPrices(LocalDate start, LocalDate end, String ticker) {
        final String path = "/Users/witdxav/Dropbox/data/quandl/wiki-prices/sp500/%s.csv";
        return DataFrame.read().csv(options -> {
            options.setResource(String.format(path, ticker.toLowerCase()));
            options.setIncludeColumns("adj_open", "adj_high", "adj_low", "adj_close", "adj_volume");
            options.setRowKeyParser(LocalDate.class, values -> LocalDate.parse(values[0]));
            options.setRowPredicate(values -> {
                final LocalDate date = LocalDate.parse(values[0]);
                return date.compareTo(start) >= 0 && date.compareTo(end) <= 0;
            });
            options.setColumnNameMapping((colName, colIndex) -> {
                switch (colName) {
                    case "adj_open":    return "Open";
                    case "adj_high":    return "High";
                    case "adj_low":     return "Low";
                    case "adj_close":   return "Close";
                    case "adj_volume":  return "Volume";
                    default:    return colName;
                }
            });
        });
    }


    /**
     * Returns a DataFrame of load based on Wiki Prices loaded from Quandl.
     * @param start     the start date for selection
     * @param end       the end date for selection
     * @param tickers   the security tickers
     * @return          the DataFrame of load
     */
    DataFrame<LocalDate,String> loadClosePrices(LocalDate start, LocalDate end, String... tickers) {
        return DataFrame.concatColumns(Stream.of(tickers).map(ticker -> loadPrices(start, end, ticker).cols().select("Close").copy().cols().replaceKey("Close", ticker)).collect(Collectors.toList()));
    }



    DataFrame<LocalDate,String> loadCumReturns(LocalDate start, LocalDate end, String... tickers) {
        Stream<DataFrame<LocalDate,String>> frames = Stream.of(tickers).map(ticker -> {
            DataFrame<LocalDate,String> prices = loadPrices(start, end, ticker);
            DataFrame<LocalDate,String> closePrice = prices.cols().select("Close");
            DataFrame<LocalDate,String> returns = DataFrame.ofDoubles(closePrice.rows().keyArray(), ticker);
            returns.rows().forEach(row -> {
                final int rowOrdinal = row.ordinal();
                if (rowOrdinal == 0) {
                    row.applyDoubles(v -> 0d);
                } else {
                    final double startPrice = closePrice.data().getDouble(0,0);
                    row.applyDoubles(v -> {
                        final double currentPrice = closePrice.data().getDouble(rowOrdinal, 0);
                        return currentPrice / startPrice - 1d;
                    });
                }
            });
            return returns;
        });
        return DataFrame.concatColumns(frames.collect(Collectors.toList()));
    }



    DataFrame<LocalDate,String> loadDailyReturns(LocalDate start, LocalDate end, String... tickers) {
        Stream<DataFrame<LocalDate,String>> frames = Stream.of(tickers).map(ticker -> {
            DataFrame<LocalDate,String> prices = loadPrices(start, end, ticker);
            DataFrame<LocalDate,String> closePrice = prices.cols().select("Close");
            DataFrame<LocalDate,String> returns = DataFrame.ofDoubles(closePrice.rows().keyArray(), ticker);
            returns.rows().forEach(row -> {
                final int rowOrdinal = row.ordinal();
                if (rowOrdinal == 0) {
                    row.applyDoubles(v -> 0d);
                } else {
                    row.applyDoubles(v -> {
                        final double priorPrice = closePrice.data().getDouble(rowOrdinal-1,0);
                        final double currentPrice = closePrice.data().getDouble(rowOrdinal, 0);
                        return currentPrice / priorPrice - 1d;
                    });
                }
            });
            return returns;
        });
        return DataFrame.concatColumns(frames.collect(Collectors.toList()));
    }


    /**
     * Computes returns based on the DataFrame of prices provided
     * @param prices    the DataFrame of prices
     * @return          the DataFrame of returns
     */
    DataFrame<LocalDate,String> computeReturns(DataFrame<LocalDate,String> prices) {
        final DataFrame<LocalDate,String> returns = prices.copy().applyDoubles(v -> 0d);
        return returns.applyDoubles(v -> {
            final int rowOrdinal = v.rowOrdinal();
            if (rowOrdinal == 0) {
                return 0d;
            } else {
                final int colOrdinal = v.colOrdinal();
                final double pricePrior = prices.data().getDouble(rowOrdinal-1, colOrdinal);
                final double priceCurrent = prices.data().getDouble(rowOrdinal, colOrdinal);
                return priceCurrent / pricePrior - 1d;
            }
        });
    }


    @Test()
    public void covm() {
        LocalDate start = LocalDate.of(2013, 1, 1);
        LocalDate end = LocalDate.of(2014, 7, 31);
        DataFrame<LocalDate,String> prices = loadClosePrices(start, end, "AAPL", "AMGN", "GE", "C", "ORCL", "BLK");

        DataFrame<LocalDate,String> returns = computeReturns(prices);
        DataFrame<LocalDate,String> returnsEwma10 = computeReturns(prices.smooth(false).ema(10));
        DataFrame<LocalDate,String> returnsEwma20 = computeReturns(prices.smooth(false).ema(20));
        DataFrame<LocalDate,String> returnsEwma30 = computeReturns(prices.smooth(false).ema(30));

        returns.cols().stats().correlation().out().print();
        returnsEwma10.cols().stats().correlation().out().print();
        returnsEwma20.cols().stats().correlation().out().print();
        returnsEwma30.cols().stats().correlation().out().print();
    }


    @Test()
    public void pcaOnCovariance() throws Exception {
        LocalDate start = LocalDate.of(2013, 1, 1);
        LocalDate end = LocalDate.of(2014, 7, 31);
        String[] tickers = new String[] {"AAPL", "GE", "ORCL", "GOOGL", "YHOO", "BLK"};
        DataFrame<LocalDate,String> prices = loadClosePrices(start, end, tickers);
        DataFrame<LocalDate,String> smoothed = prices.smooth(false).ema(50);
        DataFrame<LocalDate,String> returns = computeReturns(prices);
        DataFrame<String,String> covariance = returns.cols().stats().covariance();

        prices.out().print();
        smoothed.out().print();
        covariance.out().print();

        final RealMatrix x = covariance.export().asApacheMatrix();
        final EigenDecomposition decomposition = new EigenDecomposition(x);
        final DataFrame<Integer,String> eigenValues = DataFrame.ofDoubles(Range.of(0, tickers.length), Array.of("EigenValues"), v -> {
            return decomposition.getRealEigenvalue(v.rowOrdinal());
        });

        final DataFrame<String,Integer> eigenVectors = DataFrame.of(Array.of(tickers), Integer.class, columns -> {
            for (int i=0; i<tickers.length; ++i) {
                final RealVector eigenV = decomposition.getEigenvector(i);
                final Array<Double> values = Array.of(eigenV.toArray());
                columns.add(i, values);
            }
        });

        System.out.println("\nEigenVectors from first method");
        eigenValues.out().print();
        eigenVectors.out().print();

        System.out.println("\nEigenVectors from second method");
        returns.pca().apply(false, DataFramePCA.Solver.SVD, model -> {
            model.getEigenValues().out().print();
            model.getEigenVectors().out().print();
            return null;
        });


        System.out.println("\nEigenVectors from third method");
        returns.pca().apply(false, DataFramePCA.Solver.EVD_COV, model -> {
            model.getEigenValues().out().print();
            model.getEigenVectors().out().print();
            return null;
        });

        System.out.println("\nEigenVectors from fourth method");
        returns.pca().apply(false, DataFramePCA.Solver.EVD_COR, model -> {
            model.getEigenValues().out().print();
            model.getEigenVectors().out().print();
            return null;
        });


        /*
        eigenValues.rows().sort(false, "EigenValues");
        eigenValues.out().print(100);
        Chart.of(eigenValues.rows().select(row -> row.ordinal() < 50), chart -> {
            chart.plot(0).withBars(0d);
            chart.data().at(0).withDomainInterval(v -> v + 1);
            chart.show();
        });

        Thread.currentThread().join();
        */
    }


    @Test()
    public void pcaOnCorrelation() throws Exception {
        LocalDate start = LocalDate.of(2013, 1, 1);
        LocalDate end = LocalDate.of(2014, 7, 31);
        String[] tickers = new String[] {"AAPL", "BLK"};
        DataFrame<LocalDate,String> prices = loadClosePrices(start, end, tickers);
        DataFrame<LocalDate,String> returns = computeReturns(prices);

        DataFrame<Integer,Integer> v2 = getEigenVectors(returns.cols().stats().covariance());
        v2.out().print();

        final RealMatrix e = v2.export().asApacheMatrix().transpose();
        final RealMatrix ret = returns.export().asApacheMatrix().transpose();
        final DataFrame<Integer,Integer> result = toDataFrame(e.multiply(ret).transpose());
        result.cols().stats().correlation().out().print();

        DataFrame<Integer,Integer> cumSum = cumSum(result);
        Chart.create().withLinePlot(cumSum, chart -> {
            chart.show(1024, 768);
        });

        Thread.currentThread().join();



        /*
        final DataFrame<LocalDate,String> performance = DataFrame.ofDoubles(prices.rows().keyArray(), Array.of("P1", "P2"), v -> {
            final double asset1Weight = eigenVectors.data().getDouble(0, v.colOrdinal());
            final double asset1Return = returns.data().getDouble(v.rowOrdinal(), 0);
            final double asset2Weight = eigenVectors.data().getDouble(1, v.colOrdinal());
            final double asset2Return = returns.data().getDouble(v.rowOrdinal(), 1);
            return asset1Weight * asset1Return + asset2Weight * asset2Return;
        });

        System.out.println("\nEigenVectors...");
        eigenVectors.out().print();

        System.out.println("\nPortfolio Returns Correlation");
        performance.cols().stats().correlation().out().print();

        Chart.of(performance, chart -> {
            chart.plot(0).withLines();
            chart.show(1024, 768);
        });



        eigenVectors.out().print();
        eigenValues.rows().sort(false, "EigenValues");
        eigenValues.out().print(100);
        /*
        Chart.of(eigenValues.rows().select(row -> row.ordinal() < 50), chart -> {
            chart.plot(0).withBars(0d);
            chart.data().at(0).withDomainInterval(v -> v + 1);
            chart.show();
        });

        Thread.currentThread().join();
        */
    }


    private DataFrame<Integer,Integer> getEigenVectors(DataFrame<?,?> frame) {
        final RealMatrix x = frame.export().asApacheMatrix();
        final EigenDecomposition decomposition = new EigenDecomposition(x);
        return DataFrame.of(Range.of(0, frame.rowCount()), Integer.class, columns -> {
            for (int i=0; i<frame.rowCount(); ++i) {
                final RealVector eigenV = decomposition.getEigenvector(i);
                final Array<Double> values = Array.of(eigenV.toArray());
                columns.add(i, values);
            }
        });
    }


    @Test()
    public void plotReturnScatter() throws Exception {
        LocalDate start = LocalDate.of(2013, 1, 1);
        LocalDate end = LocalDate.of(2014, 7, 31);
        String[] tickers = new String[] {"AAPL", "GE"};
        DataFrame<LocalDate,String> prices = loadClosePrices(start, end, tickers);
        DataFrame<LocalDate,String> returns = computeReturns(prices);
        DataFrame<LocalDate,String> returnsEwma20 = computeReturns(prices.smooth(false).ema(20));
        Chart.create().withScatterPlot(returns, false, tickers[0], chart -> {
            chart.plot().style(tickers[1]).withColor(Color.RED).withPointsVisible(true);
            chart.plot().style(tickers[1]).withPointShape(ChartShape.DIAMOND);
            chart.show(800, 600);
        });
        Chart.create().withScatterPlot(returnsEwma20, false, tickers[0], chart -> {
            chart.plot().style(tickers[1]).withColor(Color.BLUE).withPointsVisible(true);
            chart.plot().style(tickers[1]).withPointShape(ChartShape.DIAMOND);
            chart.show(800, 600);
        });
        Thread.currentThread().join();
    }



    @Test()
    public void plotApple() throws Exception {
        LocalDate start = LocalDate.of(2013, 1, 1);
        LocalDate end = LocalDate.of(2014, 7, 31);
        DataFrame<LocalDate,String> prices = loadPrices(start, end, "AAPL");
        DataFrame<LocalDate,String> closePrices = prices.cols().select("Close");
        DataFrame<LocalDate,String> closePricesEma = closePrices.smooth(false).ema(10).cols().replaceKey("Close", "Close(EWMA)");
        DataFrame<LocalDate,String> combined = DataFrame.concatColumns(closePrices, closePricesEma);
        combined.out().print();
        Chart.create().withLinePlot(combined, chart -> {
            chart.plot().style("Close").withColor(Color.RED);
            chart.plot().style("Close(EWMA)").withColor(Color.BLUE);
            chart.legend().on().bottom();
            chart.show(1024, 768);
        });
        Thread.currentThread().join();
    }


    @Test()
    public void plotReturns() throws Exception {
        LocalDate start = LocalDate.of(2013, 1, 1);
        LocalDate end = LocalDate.of(2014, 7, 31);
        DataFrame<LocalDate,String> returns = loadCumReturns(start, end, "AAPL", "AMGN", "GE", "C", "ORCL", "BLK");
        returns.out().print();
        Chart.create().withLinePlot(returns, chart -> {
            chart.legend().on().bottom();
            chart.show(1024, 768);
        });
        Thread.currentThread().join();
    }


    @Test()
    public void simulateOutcomes() throws Exception {
        final int portfolioCount = 100000;
        final LocalDate start = LocalDate.of(2013, 1, 1);
        final LocalDate end = LocalDate.of(2013, 12, 31);
        DataFrame<LocalDate,String> returns = loadCumReturns(start, end, "AAPL", "GE", "ORCL", "GOOGL", "YHOO");
        DataFrame<Integer,String> holdings = DataFrame.ofDoubles(Range.of(0, portfolioCount), returns.cols().keyArray(), v -> Math.random());
        holdings.out().print();
        holdings.rows().forEach(row -> {
            final double sum = row.stats().sum();
            row.applyDoubles(v -> v.getDouble() / sum);
        });

        final DataFrame<LocalDate,String> totalReturn = returns.rows().last().map(DataFrameRow::toDataFrame).get();
        final DataFrame<String,String> covm = returns.cols().stats().covariance();

        covm.out().print();

        DataFrame<Integer,String> riskReturn = DataFrame.ofDoubles(Range.of(0, portfolioCount), Array.of("Risk", "Return"));
        holdings.rows().forEach(row -> {
            final DataFrame<Integer,String> w = row.toDataFrame();
            final double risk = computeRisk(w, covm);
            final double ret = computeReturn(w, totalReturn);
            riskReturn.data().setDouble(row.ordinal(), "Risk", risk);
            riskReturn.data().setDouble(row.ordinal(), "Return", ret);
        });

        Chart.create().withHistPlot(riskReturn, 250, "Return", chart -> {
            chart.show(1024, 768);
        });

        Chart.create().withScatterPlot(riskReturn, false, "Risk", chart -> {
            chart.title().withText("Risk / Return Profiles of 100,000 Random Portfolios");
            chart.plot().style("Return").withColor(Color.RED).withPointsVisible(true);
            chart.plot().axes().domain().label().withText("Portfolio Risk (StdDev of Return)");
            chart.plot().axes().range(0).label().withText("Portfolio Return");
            chart.show(1024, 768);
        });

        Thread.currentThread().join();

    }



    public double computeReturn(DataFrame<Integer,String> holdings, DataFrame<LocalDate,String> returns) {
        final RealMatrix w = holdings.export().asApacheMatrix();
        final RealMatrix r = returns.export().asApacheMatrix();
        final RealMatrix ret = w.multiply(r.transpose());
        if (ret.getRowDimension() == 1 && ret.getColumnDimension() == 1) {
            return ret.getEntry(0,0);
        } else {
            throw new IllegalStateException("Expected a 1x1 variance result for portfolio risk");
        }
    }


    public double computeRisk(DataFrame<Integer,String> holdings, DataFrame<String,String> covm) {
        final RealMatrix w = holdings.export().asApacheMatrix();
        final RealMatrix wT = w.transpose();
        final RealMatrix sigma = covm.export().asApacheMatrix();
        final RealMatrix variance = w.multiply(sigma.multiply(wT));
        if (variance.getRowDimension() == 1 && variance.getColumnDimension() == 1) {
            return Math.sqrt(variance.getEntry(0,0));
        } else {
            throw new IllegalStateException("Expected a 1x1 variance result for portfolio risk");
        }
    }


    /**
     * Returns a DataFrame representation of a matrix
     * @param matrix    the matrix reference
     * @return          the DataFrame representation
     */
    public DataFrame<Integer,Integer> toDataFrame(RealMatrix matrix) {
        final Range<Integer> rowKeys = Range.of(0, matrix.getRowDimension());
        final Range<Integer> colKeys = Range.of(0, matrix.getColumnDimension());
        return DataFrame.ofDoubles(rowKeys, colKeys, v -> matrix.getEntry(v.rowOrdinal(), v.colOrdinal()));
    }


    DataFrame<Integer,Integer> cumSum(DataFrame<Integer,Integer> data) {
        DataFrame<Integer,Integer> result = data.copy();
        result.rows().forEach(row -> {
            final int rowOrdinal = row.ordinal();
            if (rowOrdinal > 0) {
                row.applyDoubles(v -> {
                    final double value = v.getDouble();
                    final double prior = v.col().getDouble(rowOrdinal-1);
                    return prior + value;
                });
            }
        });
        return result;
    }

}
