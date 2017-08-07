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
package com.zavtech.morpheus.docs.basic;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.testng.annotations.Test;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import static com.zavtech.morpheus.stats.StatType.*;

import com.zavtech.morpheus.frame.DataFrameRow;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.stats.StatType;
import com.zavtech.morpheus.util.Bounds;
import com.zavtech.morpheus.util.Collect;
import com.zavtech.morpheus.util.text.parser.Parser;

public class FilteringDocs {


    /**
     * Returns the ATP match results for the year specified
     * @param year      the year for ATP results
     * @return          the ATP match results
     */
    static DataFrame<Integer,String> loadTennisMatchData(int year) {
        final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yy");
        return DataFrame.read().csv(options -> {
            options.setHeader(true);
            options.setResource("http://www.zavtech.com/data/tennis/atp/atp-" + year + ".csv");
            options.setParser("Date", Parser.ofLocalDate(dateFormat));
            options.setExcludeColumns("ATP");
        });
    }

    @Test()
    public void load() {
        //Print first 10 rows of DataFrame
        loadTennisMatchData(2013).out().print(10);
    }

    @Test()
    public void example1() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        DataFrame<Integer,String> filter = frame.rows().select(row -> {
            final int wonSets = row.getInt("Wsets");
            final int lostSets = row.getInt("Lsets");
            return  wonSets + lostSets == 5;
        });
        //Print first 10 rows
        filter.out().print(10);
    }

    @Test()
    public void example2() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        DataFrame<Integer,String> filter = frame.cols().select("B365W", "EXW", "LBW", "PSW" ,"SJW");
        filter.out().print(10);
    }


    @Test()
    public void finalOdds() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        frame.rows().last().ifPresent(row -> {
            String winner = row.getValue("Winner");
            String loser = row.getValue("Loser");
            double avgWinOdds = row.getDouble("AvgW");
            double avgLoseOdds = row.getDouble("AvgL");
            System.out.printf("%s odds = %.2f, %s odds = %.2f", winner, avgWinOdds, loser, avgLoseOdds);
        });
    }


    @Test()
    public void filterRowsAndColumns() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);

        //Convert the betting odds to standardized z-scores
        Array<String> bookies = Array.of("B365", "EX", "LB", "PS" ,"SJ");
        Stream.of("W", "L").forEach(x -> {
            Array<String> colNames = bookies.map(v -> v.getValue() + x);
            frame.cols().select(colNames).rows().forEach(row -> {
                double mean = row.stats().mean();
                double stdDev = row.stats().stdDev();
                row.values().forEach(v -> {
                    double rawValue = v.getDouble();
                    double zScore = (rawValue - mean) / stdDev;
                    v.setDouble(zScore);
                });
            });
        });

        frame.right(14).out().print(10, options -> {
            options.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });

        //Select z-score odds on eventual winner only across all bookmakers
        Set<String> colNames = Collect.asSet("B365W", "EXW", "LBW", "PSW" ,"SJW");
        DataFrame<Integer,String> winnerOdds = frame.select(
            row -> row.getValue("Round").equals("The Final"),
            col -> colNames.contains(col.key())
        );

        //Compute the mean of these z-scores and print
        winnerOdds.cols().stats().mean().out().print(options -> {
            options.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });
    }


    @Test()
    public void head() {
        //Filter on the first 5 rows
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        frame.head(5).out().print(5);
    }


    @Test()
    public void tail() {
        //Filter on the last 5 rows
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        frame.tail(5).out().print(5);
    }

    @Test()
    public void left() {
        //Filter on the first 5 columns
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        frame.left(5).out().print();
    }

    @Test()
    public void right() {
        //Filter on the last 14 columns
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        frame.right(14).out().print();
    }

    @Test()
    public void rightLeft() {
        //Filter on all betting ods, excluding max & averages
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        frame.right(14).left(10).out().print();
    }


    @Test()
    public void firstRow() {

        //Load the Wimbledon dataset
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);

        //Select the first row, and assert match date is 24-06-2013
        frame.rows().first().ifPresent(row -> {
            LocalDate matchDate = row.getValue("Date");
            assert(matchDate.equals(LocalDate.of(2013, 6, 24)));
        });

        //Select last row, and assert the winner is A Murray.
        frame.rows().last().ifPresent(row -> {
            String winner = row.getValue("Winner");
            assert(winner.equals("Murray A."));
        });

        //Select first column, namely the date column, and find max date
        frame.cols().first().ifPresent(column -> {
            Optional<LocalDate> matchDate = column.max().map(v -> (LocalDate)v.getValue());
            LocalDate finalDate = LocalDate.of(2013, 7, 7);
            assert(matchDate.isPresent() && matchDate.get().equals(finalDate));
        });

        //Select last column and find the max AvgL betting odds
        frame.cols().last().ifPresent(column -> {
            double matchDate = column.stats().max();
            assert(matchDate == 23.26d);
        });
    }


    @Test()
    public void filterRows() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        frame.rows().filter(row -> {
            String round = row.getValue("Round");
            double avgW = row.getDouble("AvgW");
            double avgL = row.getDouble("AvgL");
            return round.equals("The Final") && avgW > avgL;
        }).forEach(row -> {
            String loser = row.getValue("Loser");
            String winner = row.getValue("Winner");
            String tournament = row.getValue("Tournament");
            LocalDate date = row.getValue("Date");
            System.out.printf("Finals upset when %s beat %s at %s on %s\n", winner, loser, tournament, date);
        });
    }


    @Test()
    public void filterCols() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        frame.cols().filter("B365W", "EXW", "LBW", "PSW" ,"SJW").forEach(column -> {
            Optional<Bounds<Double>> bounds = column.bounds();
            bounds.ifPresent(b -> {
                System.out.println(column.key() + " max odds=" + b.upper() + ", min odds=" + b.lower());
            });
        });
    }


    @Test()
    public void gameCountStats1() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);

        //Select all 5 set matches, and the number of games won/lost in each set
        DataFrame<Integer,String> filter = frame.select(
            row -> row.getDouble("Wsets") + row.getDouble("Lsets") == 5,
            col -> col.key().matches("(W|L)\\d+")
        );

        filter.out().print();

        //Sum the rows which yields total games played per 5 set match
        DataFrame<Integer,StatType> gameCounts = filter.rows().stats().sum();

        //The game count frame is Nx1, so compute summary stats on game counts
        DataFrame<StatType,StatType> gameStats = gameCounts.cols().describe(MIN, MAX, MEAN, STD_DEV);

        //Print results to std-out
        gameStats.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.00;-0.00", 1);
        });

    }


    @Test()
    public void gameCountStats2() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        frame.select(
            row -> row.getDouble("Wsets") + row.getDouble("Lsets") == 5,
            col -> col.key().matches("(W|L)\\d+")
        ).rows().stats().sum().cols().describe(
            MIN, MAX, MEAN, STD_DEV
        ).out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.00;-0.00", 1);
        });
    }


    @Test()
    public void whenMutable() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);

        //Select rows where Djokovic was the victor
        DataFrame<Integer,String> filter = frame.rows().select(row -> row.getValue("Winner").equals("Djokovic N."));

        //Add a column to the filter and seed with number of games won by Djokovic
        Array<String> colNames = Range.of(1, 6).map(i -> "W" + i).toArray();
        filter.cols().add("WonCount", Double.class, value -> {
            final DataFrameRow<Integer,String> row = value.row(); //Access row associated with this value
            return colNames.mapToDoubles(v -> row.getDouble(v.getValue())).stats().sum().doubleValue();
        });

        //Print 10x10 section of right most columns
        filter.right(10).out().print(10);

        //Print 10x10 section of right most columns of original frame
        frame.right(10).out().print(10);
    }

    @Test()
    public void whenImmutable1() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        //Select rows where Djokovic was the victor
        DataFrame<Integer,String> filter = frame.rows().select(row -> row.getValue("Winner").equals("Djokovic N."));
        //Define a range of 10 additional keys
        Range<Integer> rowKeys = Range.of(frame.rowCount(), frame.rowCount()+5);
        //Try add rows for new row keys
        filter.rows().addAll(rowKeys);
    }


    @Test()
    public void whenImmutable2() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        //Select rows where Djokovic was the victor
        DataFrame<Integer,String> filter = frame.rows().select(row -> row.getValue("Winner").equals("Djokovic N."));
        //Define a range of 10 additional keys
        Range<Integer> rowKeys = Range.of(frame.rowCount(), frame.rowCount()+5);
        //Create a deep copy of the filter frame
        DataFrame<Integer,String> copy = filter.copy();
        //Add 5 new rows to the copy
        copy.rows().addAll(rowKeys);
        //Print last 10 rows
        copy.tail(10).out().print();
    }


    @Test()
    public void transpose1() {
        DataFrame<Integer,String> frame = loadTennisMatchData(2013);
        //Transpose the original DataFrame
        DataFrame<String,Integer> transpose = frame.transpose();
        //Print 10x5 section of the frame
        transpose.left(5).out().print();
        //Attempt to add a column, which we know will fail
        transpose.cols().add(transpose.colCount()+1, Double.class);
    }

}
