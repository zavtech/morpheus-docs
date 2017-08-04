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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Random;

import org.testng.annotations.Test;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.frame.DataFrameOptions;
import com.zavtech.morpheus.range.Range;

public class ReshapingDocs {


    @Test()
    public void addRows1() {
        //Create frame of Random doubles keyed by LocalDate and String
        DataFrame<LocalDate,String> frame = DataFrame.ofDoubles(
            Range.ofLocalDates("2014-01-01", "2014-01-05"),
            Range.of(0, 5).map(i -> "Column-" + i),
            value -> Math.random() * 10d
        );
        frame.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });

        //Add one row, ignore if the key is a duplicate
        frame.rows().add(LocalDate.of(2014, 1, 6));
        frame.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });

        //Add another row, inital values set to 2
        frame.rows().add(LocalDate.of(2014, 1, 7), v -> 2d);
        frame.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });

        //Add multiple rows, no initial values
        final Range<LocalDate> range1 = Range.ofLocalDates("2014-02-08", "2014-02-10");
        frame.rows().addAll(range1);
        frame.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });

        //Add multiple rows, with initial set to 3
        final Range<LocalDate> range2 = Range.ofLocalDates("2014-02-10", "2014-02-12");
        frame.rows().addAll(range2, v -> 3d);
        frame.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });
    }

    @Test()
    public void addRows2() {
        //Create a 5x5 DataFrame of doubles initialized with 1 for all values
        DataFrame<Integer,String> frame1 = DataFrame.ofDoubles(
            Array.of(0, 1, 2, 3, 4),
            Array.of("A", "B", "C", "D", "E"),
            value -> 1d
        );

        frame1.out().print();

        //Create a 7x5 DataFrame of doubles initialized with 2 for all values
        DataFrame<Integer,String> frame2 = DataFrame.ofDoubles(
            Array.of(3, 4, 5, 6, 7, 8, 9),
            Array.of("C", "D", "E", "F", "G"),
            value -> 2d
        );

        frame2.out().print();

        //Add all rows from frame2 to frame1 and print to std out
        frame1.rows().addAll(frame2);
        frame1.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.0;-0.0", 1);
        });
    }

    @Test()
    public void rowConcatenation() {
        //Create a 5x5 DataFrame of doubles initialized with 1 for all values
        DataFrame<Integer,String> frame1 = DataFrame.ofDoubles(
            Array.of(0, 1, 2, 3, 4),
            Array.of("A", "B", "C", "D", "E"),
            value -> 1d
        );
        frame1.out().print();

        //Create a 7x5 DataFrame of doubles initialized with 2 for all values
        DataFrame<Integer,String> frame2 = DataFrame.ofObjects(
            Array.of(3, 4, 5, 6, 7, 8, 9),
            Array.of("C", "D", "E", "F", "G"),
            value -> String.format("(%s, %s)", value.rowOrdinal(), value.colOrdinal())
        );
        frame2.out().print();

        //Concatenate rows from frame1 and frame2
        DataFrame<Integer,String> frame3 = DataFrame.concatRows(frame1, frame2);
        frame3.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.0;-0.0", 1);
        });

    }

    @Test()
    public void addRowsAndColumns() {
        Array<String> columns1 = Array.of("A", "B", "C");
        Array<String> columns2 = Array.of("D", "E", "F");
        DataFrame<Integer,String> frame1 = DataFrame.ofDoubles(Range.of(0, 5), columns1, v -> 1d);
        DataFrame<Integer,String> frame2 = DataFrame.ofDoubles(Range.of(5, 10), columns2, v -> 2d);

        frame1.out().print();
        frame2.out().print();

        frame1.addAll(frame2);
        frame1.out().print();
    }


    @Test()
    public void addColumns1() {
        LocalDate start = LocalDate.of(2014, 1, 1);
        DataFrame<LocalDate,String> frame = DataFrame.ofDoubles(
            Range.of(start, start.plusDays(10)),
            Array.of("A", "B"),
            value -> Math.random() * 10d
        );

        frame.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.00;-0.00", 1);
        });

        //Add single column of Booleans, no initials
        frame.cols().add("C", Boolean.class);
        //Add single column of type String with coordinate labels
        frame.cols().add("D", String.class, v -> String.format("(%s,%s)", v.rowOrdinal(), v.colOrdinal()));
        //Add single column given an Iterable of values
        frame.cols().add("E", Range.of(0, frame.rowCount()));
        //Add single column of type LocalDate, with initializing function
        frame.cols().add("F", LocalDate.class, v -> start.plusDays(v.rowOrdinal() * 2));
        //Add single column with explicit value array
        frame.cols().add("G", Array.of(10d, 20d, 30d, 40d));
        //Print first 10 rows to std out
        frame.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.00;-0.00", 1);
        });
    }


    @Test()
    public void addColumns2() {
        LocalDate start = LocalDate.of(2014, 1, 1);
        DataFrame<LocalDate,String> frame = DataFrame.ofDoubles(
            Range.of(start, start.plusDays(10)),
            Array.of("A", "B"),
            value -> Math.random() * 10d
        );
        //Add multiple columns to hold String data
        frame.cols().addAll(Array.of("C", "D", "E"), String.class);
        //Add multiple columns via consumer
        frame.cols().addAll(columns -> {
            columns.put("F", Array.of(10d, 11d, 12d, 13d, 14d));
            columns.put("G", Range.of(start.plusDays(3), start.plusDays(10)));
        });
        //Print first 10 rows to std out
        frame.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.00;-0.00", 1);
        });
    }


    @Test
    public void addColumns3() {
        //Create a 9x2 DataFrame of random double precision values
        DataFrame<Integer,String> frame1 = DataFrame.ofDoubles(
            Range.of(0, 9, 1),
            Array.of("A", "B"),
            value -> Math.random()
        );
        //Create 6x5 frame with intersecting rows and columns to the first frame
        DataFrame<Integer,String> frame2 = DataFrame.of(Range.of(0, 12, 2), String.class, columns -> {
            columns.add("B", Array.of(10, 20, 30, 40, 50, 60));
            columns.add("C", Array.of(1d, 3d, 5d, 7d, 9d, 11d));
            columns.add("D", Range.of(1, 7));
            columns.add("E", String.class, v -> String.format("(%s,%s)", v.rowOrdinal(), v.colOrdinal()));
            columns.add("F", Boolean.class, v -> Math.random() > 0.5d);
        });

        frame1.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });
        frame2.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });

        //Add all columns from second frame to first frame, copy data from intersecting rows
        frame1.cols().addAll(frame2);
        //Print frame to standard out with custom formatting
        frame1.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });
    }


    @Test
    public void addColumns4() {
        //Create a 9x2 DataFrame of random double precision values
        DataFrame<Integer,String> frame1 = DataFrame.ofDoubles(
            Range.of(0, 9),
            Array.of("A", "B"),
            value -> Math.random()
        );
        //Create 6x5 frame with intersecting rows and columns to the first frame
        DataFrame<Integer,String> frame2 = DataFrame.of(Range.of(0, 12, 2), String.class, columns -> {
            columns.add("B", Array.of(10, 20, 30, 40, 50, 60));
            columns.add("C", Array.of(1d, 3d, 5d, 7d, 9d, 11d));
            columns.add("D", Range.of(1, 7));
            columns.add("E", String.class, v -> String.format("(%s,%s)", v.rowOrdinal(), v.colOrdinal()));
            columns.add("F", Boolean.class, v -> Math.random() > 0.5d);
        });
        //Create a 9x2 DataFrame of random double precision values
        DataFrame<Integer,String> frame3 = DataFrame.ofDoubles(
            Range.of(0, 5),
            Array.of("B", "F", "G", "H"),
            value -> Math.random()
        );

        frame1.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });
        frame2.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });
        frame3.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });

        //Concatenate columns from all 3 frames to create a new result
        DataFrame<Integer,String> frame4 = DataFrame.concatColumns(frame1, frame2, frame3);
        //Print frame to standard out with custom formatting
        frame4.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });
    }

    @Test()
    public void union() {
        //Create a 4x2 DataFrame of random double precision values
        DataFrame<Integer,String> frame1 = DataFrame.ofDoubles(
            Range.of(0, 5),
            Array.of("A", "B"),
            value -> Math.random()
        );
        //Create 6x5 frame with intersecting rows and columns to the first frame
        DataFrame<Integer,String> frame2 = DataFrame.of(Range.of(0, 12, 2), String.class, columns -> {
            columns.add("B", Array.of(10, 20, 30, 40, 50, 60));
            columns.add("C", Array.of(1d, 3d, 5d, 7d, 9d, 11d));
            columns.add("D", Range.of(1, 7));
            columns.add("E", String.class, v -> String.format("(%s,%s)", v.rowOrdinal(), v.colOrdinal()));
            columns.add("F", Boolean.class, v -> Math.random() > 0.5d);
        });
        //Create a 5x4 DataFrame of random double precision values
        DataFrame<Integer,String> frame3 = DataFrame.ofDoubles(
            Range.of(0, 6),
            Array.of("B", "F", "G", "H"),
            value -> Math.random()
        );

        frame1.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });
        frame2.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });
        frame3.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });


        //Create the union of all 3 frames which should yield an 9x8 frame
        DataFrame<Integer,String> frame4 = DataFrameOptions.whileNotIgnoringDuplicates(() -> {
            return DataFrame.combineFirst(frame1, frame2, frame3).rows().sort(true);
        });
        frame4.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });

    }


    @Test()
    public void mapRowKey() {
        final Random random = new Random();

        //Create frame of ints keys by String x Integer keys
        final DataFrame<String,Integer> frame = DataFrame.ofInts(
            Array.of("A", "B", "C", "D", "E"),
            Array.of(0, 1, 2, 3, 4),
            value -> random.nextInt(10)
        );

        //Replace single row key
        frame.rows().replaceKey("A", "XY");
        frame.out().print();

        //Replace single column key
        frame.cols().replaceKey(4, 100);
        frame.out().print();

        //Map row keys to new data type (String -> Month)
        DataFrame<Month,Integer> frame1 = frame.rows().mapKeys(row -> Month.of(row.ordinal()+1));

        //Map column keys new data type (Integer -> String)
        DataFrame<Month,String> frame2 = frame1.cols().mapKeys(col -> "C-" + col.ordinal());

        //Print result to std out
        frame2.out().print();
    }

    @Test
    public void replaceKey() {
        //Create a 5x5 DataFrame of random doubles
        DataFrame<Integer,String> frame = DataFrame.ofDoubles(
            Array.of(0, 1, 2, 3, 4),
            Array.of("A", "B", "C", "D", "E"),
            value -> Math.random() * 10d
        );

        frame.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });

        frame.rows().replaceKey(4, 40);
        frame.cols().replaceKey("C", "X");
        frame.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });
    }

    @Test()
    public void dateShift() {
        //Create a 10x4 DataFrame of random doubles
        DataFrame<LocalDate,String> frame = DataFrame.ofDoubles(
            Range.ofLocalDates("2014-01-01", "2014-01-11"),
            Array.of("A", "B", "C", "D"),
            value -> Math.random() * 10d
        );

        frame.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });

        DataFrame<LocalDateTime,String> shifted = frame.rows().mapKeys(row -> {
            final LocalDate rowKey = row.key().plusDays(5);
            return LocalDateTime.of(rowKey, LocalTime.of(13, 30));
        });
        shifted.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });

        //Zero out shifted frame values, and print original frame
        shifted.applyDoubles(v -> 0d);
        frame.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.000;-0.000", 1);
        });
    }


    @Test()
    public void transpose() {

        //Create a 5x5 DataFrame of random doubles
        DataFrame<Integer,String> frame = DataFrame.ofDoubles(
            Array.of(0, 1, 2, 3, 4),
            Array.of("A", "B", "C", "D", "E"),
            value -> Math.random() * 10d
        );

        //Tranpose the transposed frame to get back to a colum store which can be re-shaped
        DataFrame<String,Integer> transposed = frame.transpose();
        transposed.transpose().rows().add(5);
        transposed.out().print(formats -> {
            formats.setDecimalFormat(Double.class, "0.00;-0.00", 1);
        });

    }

}
