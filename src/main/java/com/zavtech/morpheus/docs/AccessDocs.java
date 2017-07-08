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
import java.time.Month;
import java.time.Year;
import java.util.Optional;
import java.util.function.ToDoubleFunction;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.frame.DataFrameColumn;
import com.zavtech.morpheus.frame.DataFrameRow;
import com.zavtech.morpheus.frame.DataFrameValue;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.util.Asserts;
import com.zavtech.morpheus.util.Tuple;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AccessDocs {


    @DataProvider(name="frame")
    public Object[][] getFrame() {
        //Create 5x5 frame with columns of different types.
        final Range<Year> years = Range.of(2000 ,2005).map(Year::of);
        final DataFrame<Year,String> frame = DataFrame.of(years, String.class, columns -> {
            columns.add("Column-0", Array.of(true, false, false, true, true));
            columns.add("Column-1", Array.of(1, 2, 3, 4, 5));
            columns.add("Column-2", Array.of(10L, 11L, 12L, 13L, 14L));
            columns.add("Column-3", Array.of(20d, 21d, 22d, 23d, 24d));
            columns.add("Column-4", Array.of("Hello", LocalDate.of(1998, 1, 1), Month.JANUARY, 56.45d, true));
        });
        return new Object[][] { { frame } };
    }


    @Test()
    public void testFrame() {
        //Create 5x5 frame with columns of different types.
        final Range<Year> years = Range.of(2000 ,2005).map(Year::of);
        final DataFrame<Year,String> frame = DataFrame.of(years, String.class, columns -> {
            columns.add("Column-0", Array.of(true, false, false, true, true));
            columns.add("Column-1", Array.of(1, 2, 3, 4, 5));
            columns.add("Column-2", Array.of(10L, 11L, 12L, 13L, 14L));
            columns.add("Column-3", Array.of(20d, 21d, 22d, 23d, 24d));
            columns.add("Column-4", Array.of("Hello", LocalDate.of(1998, 1, 1), Month.JANUARY, 56.45d, true));
        });

        frame.out().print();
    }


    @Test(dataProvider="frame")
    public void testAccessMethods(DataFrame<Year,String> frame) {

        frame.out().print();

        //Random access to primitive boolean values via ordinal or keys or any combination thereof
        boolean b1 = frame.data().getBoolean(0, 0);
        boolean b2 = frame.data().getBoolean(Year.of(2003), 0);
        boolean b3 = frame.data().getBoolean(0, "Column-0");
        boolean b4 = frame.data().getBoolean(Year.of(2001), "Column-0");

        //Random access to primitive int values via ordinal or keys or any combination thereof
        int i1 = frame.data().getInt(4, 1);
        int i2 = frame.data().getInt(Year.of(2003), 1);
        int i3 = frame.data().getInt(0, "Column-1");
        int i4 = frame.data().getInt(Year.of(2001), "Column-1");

        //Random access to primitive long values via ordinal or keys or any combination thereof
        long l1 = frame.data().getLong(4, 2);
        long l2 = frame.data().getLong(Year.of(2003), 2);
        long l3 = frame.data().getLong(0, "Column-2");
        long l4 = frame.data().getLong(Year.of(2001), "Column-2");

        //Random access to primitive double values via ordinal or keys or any combination thereof
        double d1 = frame.data().getDouble(4, 3);
        double d2 = frame.data().getDouble(Year.of(2003), 3);
        double d3 = frame.data().getDouble(0, "Column-3");
        double d4 = frame.data().getDouble(Year.of(2001), "Column-3");

        //Random access to any values via ordinal or keys or any combination thereof
        String o1 = frame.data().getValue(0, 4);
        LocalDate o2 = frame.data().getValue(Year.of(2001), 4);
        Month o3 = frame.data().getValue(2, "Column-4");
        Double o4 = frame.data().getValue(Year.of(2003), "Column-4");
    }


    @Test()
    public void testAllAccess() {

        //Create DataFrame of random doubles
        DataFrame<Integer,String> frame = DataFrame.ofDoubles(
            Array.of(0, 1, 2, 3, 4, 5, 6, 7),
            Array.of("A", "B", "C", "D"),
            value -> Math.random()
        );

        //Count number of values > 0.5d first sequentially, then in parallel
        long count1 = frame.values().filter(v -> v.getDouble() > 0.5d).count();
        long count2 = frame.values().parallel().filter(v -> v.getDouble() > 0.5d).count();
        Assert.assertEquals(count1, count2);

    }

    @Test()
    public void testPercentageConversion() {
        final DataFrame<Tuple,String> frame = DemoData.loadPopulationDataset();
        //Sequential: Convert male & female population counts into weights
        frame.rows().parallel().forEach(row -> row.forEach(value -> {
            if (value.colKey().matches("M\\s+\\d+")) {
                double totalMales = value.row().getDouble("All Males");
                double count = value.getDouble();
                value.setDouble(count / totalMales);
            } else if (value.colKey().matches("F\\s+\\d+")) {
                double totalFemales = value.row().getDouble("All Females");
                double count = value.getDouble();
                value.setDouble(count / totalFemales);
            }
        }));

        //Print frame to std out with custom formatting
        frame.out().print(formats -> {
            formats.setDecimalFormat("All Persons", "0;-0", 1);
            formats.setDecimalFormat("All Males", "0;-0", 1);
            formats.setDecimalFormat("All Females", "0;-0", 1);
            formats.setDecimalFormat(Double.class, "0.00'%';-0.00'%'", 100);
        });
    }



    @Test()
    public void testRandomAccess1() {
        final DataFrame<Tuple,String> frame = DemoData.loadPopulationDataset();

        //Random access to a row by ordinal or key
        DataFrameRow<Tuple,String> row1 = frame.rowAt(4);
        DataFrameRow<Tuple,String> row2 = frame.rowAt(Tuple.of(2003, "City of London"));

        //Random access to a column by ordinal or key
        DataFrameColumn<Tuple,String> column1 = frame.colAt(2);
        DataFrameColumn<Tuple,String> column2 = frame.colAt("All Persons");

        //Access first and last rows
        Optional<DataFrameRow<Tuple,String>> firstRow = frame.rows().first();
        Optional<DataFrameRow<Tuple,String>> lastRow = frame.rows().last();

        //Access first and last columns
        Optional<DataFrameColumn<Tuple,String>> firstColumn = frame.cols().first();
        Optional<DataFrameColumn<Tuple,String>> lastColumn = frame.cols().last();

    }


    @Test()
    public void testRandomAccess2() {
        final DataFrame<Tuple,String> frame = DemoData.loadPopulationDataset();

        // Find row with max value for All Persons column using column index
        frame.colAt(2).max().ifPresent(value -> {
            DataFrameRow<Tuple,String> row = frame.rowAt(value.rowKey());
            System.out.printf("Max population for %s in %s\n", row.getValue("Borough"), row.getInt("Year"));
        });

        //Access 5th row by row ordinal, and find first value that matches a predicate
        frame.rowAt(4).first(v -> v.isDouble() && v.getDouble() < 20).ifPresent(v -> {
            System.out.println("First match > 1000 in " + v.colKey());
        });


        // Find row with max value for "All Persons: column using column key
        final double expectedMax = frame.colAt("All Persons").stats().max();
        frame.colAt("All Persons").max().ifPresent(value -> {
            final DataFrameRow<Tuple,String> row = frame.rowAt(value.rowKey());
            final double actualMax = row.getDouble("All Persons");
            Asserts.assertEquals(actualMax, expectedMax, "The max values match");
        });

        //Access 5th row by row key, and find first value that matches a predicate
        frame.rowAt(Tuple.of("2003", "E09000001")).first(v -> v.isDouble() && v.getDouble() < 20).ifPresent(v -> {
            System.out.println("First match > 1000 in " + v.colKey());
        });

    }



    @Test()
    public void testArrayAccess() {
        final Array<Boolean> booleanArray = Array.of(true, false, true, false);
        final Array<Integer> intArray = Array.of(1, 2, 3, 4, 5);
        final Array<Long> longArray = Array.of(1L, 2L, 3L, 4L, 5L);
        final Array<Double> doubleArray = Array.of(1d, 2d, 3d, 4d, 5d);
        final Array<Object> objectArray = Array.of("Hello", LocalDate.of(1998, 1, 1), Month.JANUARY, 56.45d, true);

        System.out.println(booleanArray.getClass().getName());
        System.out.println(intArray.getClass().getName());
        System.out.println(longArray.getClass().getName());
        System.out.println(doubleArray.getClass().getName());
        System.out.println(objectArray.getClass().getName());
    }


    @Test()
    public void demean() {

        //Load population dataset
        final DataFrame<Tuple,String> onsFrame = DemoData.loadPopulationDataset();

        //Iterate over male then female column set
        Array.of("M\\s+\\d++", "F\\s+\\d++").forEach(regex -> {
            onsFrame.cols().select(c -> c.key().matches(regex)).rows().parallel().forEach(row -> {
                final double mean = row.stats().mean();
                row.applyDoubles(v -> v.getDouble() - mean);
            });
        });

        //Print frame to standard out with formatting
        onsFrame.out().print(formats -> {
            formats.setDecimalFormat("All Persons", "0;-0", 1);
            formats.setDecimalFormat("All Males", "0;-0", 1);
            formats.setDecimalFormat("All Females", "0;-0", 1);
            formats.setDecimalFormat(Double.class, "0.00;-0.00", 1);
        });
    }

    @Test()
    public void weights() {
        //Load ONS population dataset
        final DataFrame<Tuple,String> onsFrame = DemoData.loadPopulationDataset();

        //Define function to compute population weight as a percentage of 2007 value per borough
        final ToDoubleFunction<DataFrameValue<Tuple,String>> compute = value -> {
            final String borough = value.rowKey().item(1);
            final Tuple rowKey2014 = Tuple.of(2007, borough);
            final double boroughCountIn2014 = onsFrame.data().getDouble(rowKey2014, "All Persons");
            return value.getDouble() / boroughCountIn2014;
        };

        //Apply function to various columns in order
        onsFrame.cols().select(c -> c.key().matches("[MF]\\s+\\d+")).applyDoubles(compute);
        onsFrame.colAt("All Males").applyDoubles(compute);
        onsFrame.colAt("All Females").applyDoubles(compute);
        onsFrame.out().print(formats -> {
            formats.setDecimalFormat("All Persons", "0.0;-0.0", 1);
            formats.setDecimalFormat(Double.class, "0.00'%';-0.00'%'", 100);
        });
    }

}
