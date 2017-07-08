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

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.util.Collect;
import com.zavtech.morpheus.util.IO;
import com.zavtech.morpheus.util.Tuple;
import com.zavtech.morpheus.util.text.printer.Printer;

public class FindingDocs {

    @Test()
    public void findFirstRow() {
        DataFrame<Tuple,String> frame = DemoData.loadPopulationDatasetWeights();

        frame.out().print(formats -> {
            formats.setPrinter("All Males", Printer.ofDouble("0;-0"));
            formats.setPrinter("All Persons", Printer.ofDouble("0;-0"));
            formats.setPrinter("All Females", Printer.ofDouble("0;-0"));
            formats.setPrinter(Double.class, Printer.ofDouble("0.00'%';-0.00'%'", 100));
        });

        frame.rows().last(row -> {
            double total = row.getDouble("All Persons");
            double maleWeight = row.getDouble("All Males") / total;
            double femaleWeight = row.getDouble("All Females") / total;
            return Math.abs(maleWeight - femaleWeight) > 0.1;
        }).ifPresent(row -> {
            int year = row.key().item(0);
            String borough = row.key().item(1);
            double total = row.getDouble("All Persons");
            double males = (row.getDouble("All Males") / total) * 100d;
            double females = (row.getDouble("All Females") / total) * 100d;
            IO.printf("Male weight: %.2f%%, Female weight: %.2f%% for %s in %s", males, females, year, borough);
        });
    }

    @Test()
    public void findFirstValueInRow() {
        DataFrame<Tuple,String> frame = DemoData.loadPopulationDatasetWeights();
        frame.rows().filter(r -> r.getValue("Borough").equals("Kensington and Chelsea")).forEach(row -> {
            row.first(v -> v.colKey().matches("[MF]\\s+\\d+") && v.getDouble() > 0.01).ifPresent(v -> {
                Tuple rowKey = v.rowKey();
                String group = v.colKey();
                double weight = v.getDouble() * 100d;
                IO.printf("Age group %s has a population of %.2f%% for %s\n", group, weight, rowKey);
            });
        });
    }


    @Test()
    public void findMaxValue() {
        DataFrame<Tuple,String> frame = DemoData.loadPopulationDatasetWeights();
        frame.max(v ->
            v.row().getValue("Borough").equals("Islington") &&
            v.colKey().matches("[MF]\\s+\\d+") &&
            v.getDouble() > 0
        ).ifPresent(max -> {
            int year = max.rowKey().item(0);
            String group = max.colKey();
            double weight = max.getDouble() * 100;
            String borough = max.row().getValue("Borough");
            System.out.printf("Max population is %.2f%% for age group %s in %s, %s", weight, group, borough, year);
        });
    }


    @Test()
    public void findMaxRowValue() {
        Tuple rowKey = Tuple.of(2000, "Islington");
        DataFrame<Tuple,String> frame = DemoData.loadPopulationDatasetWeights();
        frame.rowAt(rowKey).max(v -> v.colKey().matches("[MF]\\s+\\d+") && v.getDouble() > 0).ifPresent(max -> {
            String group = max.colKey();
            int year = max.rowKey().item(0);
            String borough = max.rowKey().item(1);
            double weight = max.getDouble() * 100d;
            System.out.printf("Max population weight for %s in %s is %.2f%% for %s", borough, year, weight, group);
        });
    }


    @Test()
    public void findMaxColumnValue() {
        Set<String> boroughs = Collect.asSet("Islington", "Wandsworth", "Kensington and Chelsea");
        DataFrame<Tuple,String> frame = DemoData.loadPopulationDatasetWeights();
        frame.colAt("F 30").max(v -> boroughs.contains((String)v.row().getValue("Borough"))).ifPresent(max -> {
            int year = max.rowKey().item(0);
            double weight = max.getDouble() * 100d;
            String borough = max.row().getValue("Borough");
            System.out.printf("Max female population weight aged 30 is %.2f%% in %s, %s", weight, borough, year);
        });
    }




    @Test()
    public void binarySearchInColumn() {
        final Tuple expected = Tuple.of(2000, "Islington");
        final DataFrame<Tuple,String> frame = DemoData.loadPopulationDatasetWeights();
        final double weight = frame.data().getDouble(expected, "F 30");
        frame.rows().sort(true, "F 30"); //Ensure data is sorted
        frame.colAt("F 30").binarySearch(weight).ifPresent(value -> {
            assert(value.rowKey().equals(expected));
            assert(value.getDouble() == weight);
        });
    }


    @Test()
    public void argmin() {



    }

    @Test()
    public void findMaxRow() {
        final DataFrame<Tuple,String> frame = DemoData.loadPopulationDatasetWeights();
        frame.rows().min((row1, row2) -> {
            double ratio1 = row1.getDouble("F 0") / row1.getDouble("M 0");
            double ratio2 = row2.getDouble("F 0") / row2.getDouble("M 0");
            return Double.compare(ratio1, ratio2);
        }).ifPresent(row -> {
            Tuple rowKey = row.key();
            double males = row.getDouble("M 0") * 100d;
            double females = row.getDouble("F 0") * 100d;
            IO.printf("Smallest female / male births = %.2f%%/%.2f%% for %s", females, males, rowKey);
        });
    }


    @Test()
    public void previousRow() {
        LocalDate start = LocalDate.of(2014, 1, 1);
        Range<String> columns = Range.of(0, 10).map(i -> "Column-" + i);
        Range<LocalDate> monthEnds = Range.of(0, 12).map(i -> start.plusMonths(i+1).minusDays(1));
        DataFrame<LocalDate,String> frame = DataFrame.ofDoubles(monthEnds, columns, v -> Math.random() * 100d);
        frame.out().print(100, formats -> {
            formats.setDecimalFormat(Double.class, "0.00;-0.00", 1);
        });

        //Iterate over first 35 days of row axis at daily frequency
        Range<LocalDate> dates = Range.of(monthEnds.start(), monthEnds.start().plusDays(35), Period.ofDays(1));
        dates.forEach(date -> {
            if (frame.rows().contains(date)) {
                IO.println("Exact match for: " + date);
            } else {
                Optional<LocalDate> lowerKey = frame.rows().lowerKey(date);
                assert(lowerKey.isPresent());
                assert(lowerKey.get().equals(date.withDayOfMonth(1).minusDays(1)));
                IO.printf("Lower match for %s is %s%n", date, lowerKey.get());
            }
        });
    }

}
