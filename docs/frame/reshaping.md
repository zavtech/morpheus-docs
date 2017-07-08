### Reshaping

It is often useful to be able to expand a `DataFrame` in either the row or column dimension, or both, in order to store
calculated data or other state as part of some analysis. For example, a frame could be used to capture real-time in-memory 
performance metrics or other high frequency observations for expanding window statistical analysis, requiring new rows to be 
added as data is collected. The following sections provide some demonstrations of how to use the API to add rows & columns, 
and discusses some of the caveats one needs to be aware of.

#### Adding Rows

##### Singular 

The `DataFrameRows` interface provides a number of methods for **efficiently** adding rows to an existing frame in place 
(that is, modifying the existing frame as opposed to creating an entirely new instance). This is very much analogous to the 
way that Java collections like `List`, `Set` and `Map` can be expanded in place.  Consider as a starting point a 4x5 frame 
of doubles initialized with random values as follows.

<?prettify?>
```java
//Create frame of Random doubles keyed by LocalDate and String
DataFrame<LocalDate,String> frame = DataFrame.ofDoubles(
    Range.ofLocalDates("2014-01-01", "2014-01-05"),
    Range.of(0, 5).map(i -> "Column-" + i),
    value -> Math.random() * 10d
);
```

<div class="frame"><pre class="frame">
   Index     |  Column-0  |  Column-1  |  Column-2  |  Column-3  |  Column-4  |
-------------------------------------------------------------------------------
 2014-01-01  |     7.378  |     5.395  |     8.915  |     7.491  |     7.747  |
 2014-01-02  |     1.829  |     3.493  |     8.910  |     7.978  |     1.154  |
 2014-01-03  |     1.416  |     8.203  |     1.520  |     8.569  |     8.612  |
 2014-01-04  |     9.619  |     7.539  |     7.204  |     5.950  |     0.532  |
</pre></div>
    
Below we add a single row with a `LocalDate` key that does not exist in the row axis. If the key did already exist, the operation 
would be a no-op, however this behaviour can be controlled via `DataFrameOptions` (see section below on duplicate handling). Since
all the columns of this frame are of type `double`, and we have not provided any initial values for the row, they all initialize
as `Double.NaN`.

<?prettify?>
```java
//Add one row, ignore if the key is a duplicate
frame.rows().add(LocalDate.of(2014, 1, 6));
frame.out().print(formats -> {
    formats.withDecimalFormat(Double.class, "0.000;-0.000", 1);
});
```

<div class="frame"><pre class="frame">
   Index     |  Column-0  |  Column-1  |  Column-2  |  Column-3  |  Column-4  |
-------------------------------------------------------------------------------
 2014-01-01  |     7.378  |     5.395  |     8.915  |     7.491  |     7.747  |
 2014-01-02  |     1.829  |     3.493  |     8.910  |     7.978  |     1.154  |
 2014-01-03  |     1.416  |     8.203  |     1.520  |     8.569  |     8.612  |
 2014-01-04  |     9.619  |     7.539  |     7.204  |     5.950  |     0.532  |
 2014-01-06  |       NaN  |       NaN  |       NaN  |       NaN  |       NaN  |
</pre></div>

It is possible to initialize values for a newly added row by providing a lambda function that consumes a `DataFrameValue`
as shown below. In this case, we add another row with values initialized to `2.0`. A more involved initialization function 
is easy to imagine of course.

<?prettify?>
```java
//Add another row, inital values set to 2
frame.rows().add(LocalDate.of(2014, 1, 7), v -> 2d);
frame.out().print(formats -> {
    formats.withDecimalFormat(Double.class, "0.000;-0.000", 1);
});
```

<div class="frame"><pre class="frame">
   Index     |  Column-0  |  Column-1  |  Column-2  |  Column-3  |  Column-4  |
-------------------------------------------------------------------------------
 2014-01-01  |     7.378  |     5.395  |     8.915  |     7.491  |     7.747  |
 2014-01-02  |     1.829  |     3.493  |     8.910  |     7.978  |     1.154  |
 2014-01-03  |     1.416  |     8.203  |     1.520  |     8.569  |     8.612  |
 2014-01-04  |     9.619  |     7.539  |     7.204  |     5.950  |     0.532  |
 2014-01-06  |       NaN  |       NaN  |       NaN  |       NaN  |       NaN  |
 2014-01-07  |     2.000  |     2.000  |     2.000  |     2.000  |     2.000  |
</pre></div>

##### Multiple

Adding multiple rows is also supported via overloaded `addAll()` methods, which expects an `Iterable` of keys as an argument. 
Once again, any duplicates will be ignored unless this behaviour is disabled via `DataFrameOptions` (see duplicate handling 
below). Continuing with the frame from the previous section, the code below adds additional rows based on a `Range` of `LocalDate` 
objects, which works becase `Range` implements `Iterable` (note that a range's start value is inclusive, and the end value is 
exclusive).

<?prettify?>
```java
final Range<LocalDate> range1 = Range.ofLocalDates("2014-02-08", "2014-02-10");
frame.rows().addAll(range1);
frame.out().print(formats -> {
    formats.withDecimalFormat(Double.class, "0.000;-0.000", 1);
});
```    

<div class="frame"><pre class="frame">
   Index     |  Column-0  |  Column-1  |  Column-2  |  Column-3  |  Column-4  |
-------------------------------------------------------------------------------
 2014-01-01  |     7.378  |     5.395  |     8.915  |     7.491  |     7.747  |
 2014-01-02  |     1.829  |     3.493  |     8.910  |     7.978  |     1.154  |
 2014-01-03  |     1.416  |     8.203  |     1.520  |     8.569  |     8.612  |
 2014-01-04  |     9.619  |     7.539  |     7.204  |     5.950  |     0.532  |
 2014-01-06  |       NaN  |       NaN  |       NaN  |       NaN  |       NaN  |
 2014-01-07  |     2.000  |     2.000  |     2.000  |     2.000  |     2.000  |
 2014-02-08  |       NaN  |       NaN  |       NaN  |       NaN  |       NaN  |
 2014-02-09  |       NaN  |       NaN  |       NaN  |       NaN  |       NaN  |
</pre></div>
    
Finally, an initializing lambda function can be provided to set the values for newly added rows. In the example below,
we add rows based on another date `Range` and set initial values to 3.

<?prettify?>
```java
//Add multiple rows, with initial set to 3
final Range<LocalDate> range2 = Range.ofLocalDates("2014-02-10", "2014-02-12");
frame.rows().addAll(range2, v -> 3d);
frame.out().print(formats -> {
    formats.withDecimalFormat(Double.class, "0.000;-0.000", 1);
});
```

<div class="frame"><pre class="frame">
   Index     |  Column-0  |  Column-1  |  Column-2  |  Column-3  |  Column-4  |
-------------------------------------------------------------------------------
 2014-01-01  |     7.378  |     5.395  |     8.915  |     7.491  |     7.747  |
 2014-01-02  |     1.829  |     3.493  |     8.910  |     7.978  |     1.154  |
 2014-01-03  |     1.416  |     8.203  |     1.520  |     8.569  |     8.612  |
 2014-01-04  |     9.619  |     7.539  |     7.204  |     5.950  |     0.532  |
 2014-01-06  |       NaN  |       NaN  |       NaN  |       NaN  |       NaN  |
 2014-01-07  |     2.000  |     2.000  |     2.000  |     2.000  |     2.000  |
 2014-02-08  |       NaN  |       NaN  |       NaN  |       NaN  |       NaN  |
 2014-02-09  |       NaN  |       NaN  |       NaN  |       NaN  |       NaN  |
 2014-02-10  |     3.000  |     3.000  |     3.000  |     3.000  |     3.000  |
 2014-02-11  |     3.000  |     3.000  |     3.000  |     3.000  |     3.000  |
</pre></div>

##### Frame

The scenarios discussed in the previous sections illustrate how rows can be added by specifying one or more keys, with an optional 
function to set the initial values for the newly added rows. It is often convenient to be able to add all the rows from one frame 
to another, in place (i.e. without creating a third frame). In these situations, data from the added frame is copied across based 
on **intersecting columns** between the two frames. Consider the following example where we construct two frames that have both 
intersecting row and column keys.

<?prettify?>
```java
//Create a 5x5 DataFrame of doubles initialized with 1 for all values
DataFrame<Integer,String> frame1 = DataFrame.ofDoubles(
    Array.of(0, 1, 2, 3, 4),
    Array.of("A", "B", "C", "D", "E"),
    value -> 1d
);
```

<div class="frame"><pre class="frame">
 Index  |   A   |   B   |   C   |   D   |   E   |
-------------------------------------------------
     0  |  1.0  |  1.0  |  1.0  |  1.0  |  1.0  |
     1  |  1.0  |  1.0  |  1.0  |  1.0  |  1.0  |
     2  |  1.0  |  1.0  |  1.0  |  1.0  |  1.0  |
     3  |  1.0  |  1.0  |  1.0  |  1.0  |  1.0  |
     4  |  1.0  |  1.0  |  1.0  |  1.0  |  1.0  |
</pre></div>

<?prettify?>
```java
//Create a 7x5 DataFrame of doubles initialized with 2 for all values
DataFrame<Integer,String> frame2 = DataFrame.ofDoubles(
    Array.of(3, 4, 5, 6, 7, 8, 9),
    Array.of("C", "D", "E", "F", "G"),
    value -> 2d
);
```

<div class="frame"><pre class="frame">
 Index  |   C   |   D   |   E   |   F   |   G   |
-------------------------------------------------
     3  |  2.0  |  2.0  |  2.0  |  2.0  |  2.0  |
     4  |  2.0  |  2.0  |  2.0  |  2.0  |  2.0  |
     5  |  2.0  |  2.0  |  2.0  |  2.0  |  2.0  |
     6  |  2.0  |  2.0  |  2.0  |  2.0  |  2.0  |
     7  |  2.0  |  2.0  |  2.0  |  2.0  |  2.0  |
     8  |  2.0  |  2.0  |  2.0  |  2.0  |  2.0  |
     9  |  2.0  |  2.0  |  2.0  |  2.0  |  2.0  |
</pre></div>

These two frames have intersecting row keys, namely `3` and `4`, and intersecting column keys, namely `C`, `D` and `E`. We
can add all rows from `frame2` to `frame1` as per the code below, and since by default duplicates are ignored, the rows `3` and 
`4` from `frame2` will not be added. In addition, only data for intersecting columns will be copied resulting in a 10x5 frame as 
shown below.
 
<?prettify?>
```java
//Add all rows from frame2 to frame1 and print to std out
frame1.rows().addAll(frame2);
frame1.out().print(formats -> {
    formats.withDecimalFormat(Double.class, "0.0;-0.0", 1);
});
```

<div class="frame"><pre class="frame">
 Index  |   A   |   B   |   C   |   D   |   E   |
-------------------------------------------------
     0  |  1.0  |  1.0  |  1.0  |  1.0  |  1.0  |
     1  |  1.0  |  1.0  |  1.0  |  1.0  |  1.0  |
     2  |  1.0  |  1.0  |  1.0  |  1.0  |  1.0  |
     3  |  1.0  |  1.0  |  1.0  |  1.0  |  1.0  |
     4  |  1.0  |  1.0  |  1.0  |  1.0  |  1.0  |
     5  |  NaN  |  NaN  |  2.0  |  2.0  |  2.0  |
     6  |  NaN  |  NaN  |  2.0  |  2.0  |  2.0  |
     7  |  NaN  |  NaN  |  2.0  |  2.0  |  2.0  |
     8  |  NaN  |  NaN  |  2.0  |  2.0  |  2.0  |
     9  |  NaN  |  NaN  |  2.0  |  2.0  |  2.0  |
</pre></div>

##### Concatenation

When adding rows from one `DataFrame` to another as presented in the previous section, the data types for the intersecting 
columns must be compatible, otherwise a `DataFrameException` will be raised. In the prior example, both frames were made up 
of double precision columns, so this was not an issue. If the second frame, for argument sake, consisted of columns of type 
`String` the `addAll()` method would have failed when an attempt was made to copy the String data from the second frame to
the first.

There are situations when it is necessary to combine the rows of two frames that have **incompatible** data types, or when it is
preferable to create a third frame with the combined data instead of mutating a frame in place. This is where row concatenation
comes in. If we consider a similar example to the previous section, but in this case initialize the second frame with `String`
values, we can still combine the two frames using `DataFrame.concatRows()`.

In this scenario we initialize the second frame with `String` values as follows:

<?prettify?>
```java
//Create a 7x5 DataFrame of doubles initialized with coordinate string of the form (i,j)
DataFrame<Integer,String> frame2 = DataFrame.ofObjects(
    Array.of(3, 4, 5, 6, 7, 8, 9),
    Array.of("C", "D", "E", "F", "G"),
    value -> String.format("(%s, %s)", value.rowOrdinal(), value.colOrdinal())
);
```
    
<div class="frame"><pre class="frame">
 Index  |    C     |    D     |    E     |    F     |    G     |
----------------------------------------------------------------
     3  |  (0, 0)  |  (0, 1)  |  (0, 2)  |  (0, 3)  |  (0, 4)  |
     4  |  (1, 0)  |  (1, 1)  |  (1, 2)  |  (1, 3)  |  (1, 4)  |
     5  |  (2, 0)  |  (2, 1)  |  (2, 2)  |  (2, 3)  |  (2, 4)  |
     6  |  (3, 0)  |  (3, 1)  |  (3, 2)  |  (3, 3)  |  (3, 4)  |
     7  |  (4, 0)  |  (4, 1)  |  (4, 2)  |  (4, 3)  |  (4, 4)  |
     8  |  (5, 0)  |  (5, 1)  |  (5, 2)  |  (5, 3)  |  (5, 4)  |
     9  |  (6, 0)  |  (6, 1)  |  (6, 2)  |  (6, 3)  |  (6, 4)  |
</pre></div>

To combine the rows of this frame with the first frame of doubles created earlier, we can create a third frame with column data types 
that support the combined data across all the frames in question. This technique can be used to concatenate the rows of any number of 
frames. The code below uses the `DataFrame.concatRows()` static method and prints the result to standard out.

<?prettify?>
```java
//Concatenate rows from frame1 and frame2
DataFrame<Integer,String> frame3 = DataFrame.concatRows(frame1, frame2);
frame3.out().print(formats -> {
    formats.withDecimalFormat(Double.class, "0.0;-0.0", 1);
});
```

<div class="frame"><pre class="frame">
 Index  |   A   |   B   |    C     |    D     |    E     |
----------------------------------------------------------
     0  |  1.0  |  1.0  |     1.0  |     1.0  |     1.0  |
     1  |  1.0  |  1.0  |     1.0  |     1.0  |     1.0  |
     2  |  1.0  |  1.0  |     1.0  |     1.0  |     1.0  |
     3  |  1.0  |  1.0  |     1.0  |     1.0  |     1.0  |
     4  |  1.0  |  1.0  |     1.0  |     1.0  |     1.0  |
     5  |  NaN  |  NaN  |  (2, 0)  |  (2, 1)  |  (2, 2)  |
     6  |  NaN  |  NaN  |  (3, 0)  |  (3, 1)  |  (3, 2)  |
     7  |  NaN  |  NaN  |  (4, 0)  |  (4, 1)  |  (4, 2)  |
     8  |  NaN  |  NaN  |  (5, 0)  |  (5, 1)  |  (5, 2)  |
     9  |  NaN  |  NaN  |  (6, 0)  |  (6, 1)  |  (6, 2)  |
</pre></div>

The resulting `DataFrame` includes the 5 columns of the first frame passed to `concatRows()`, however unlike the first
frame, columns `C`, `D` and `E` are now of type `Object` rather than of type `double` because these columns now hold both
double precision data (rows 0 through 4) and String data (rows 4 through 9).

#### Adding Columns

Consider the a 10x2 `DataFrame` of random double precision values as a starting point to illustrate the various
column expansion calls described in the following sections. The row axis is indexed by `LocalDate` values and the
column axis by `String` values.

<?prettify?>
```java
LocalDate start = LocalDate.of(2014, 1, 1);
DataFrame<LocalDate,String> frame = DataFrame.ofDoubles(
    Range.of(start, start.plusDays(10)),
    Array.of("A", "B"),
    value -> Math.random() * 10d
);
```

<div class="frame"><pre class="frame">
   Index     |   A    |   B    |
--------------------------------
 2014-01-01  |  8.72  |  9.19  |
 2014-01-02  |  6.42  |  3.42  |
 2014-01-03  |  1.75  |  1.52  |
 2014-01-04  |  1.15  |  0.94  |
 2014-01-05  |  7.80  |  2.10  |
 2014-01-06  |  9.23  |  0.41  |
 2014-01-07  |  3.26  |  0.04  |
 2014-01-08  |  1.84  |  5.10  |
 2014-01-09  |  2.87  |  4.82  |
 2014-01-10  |  6.61  |  7.15  |
</pre></div>

##### Singular 

The `DataFrameColumns` interface exposes various `add()` and `addAll()` methods, but they differ slightly to those on `DataFrameRows`.
Each column in a frame is associated with a specific data type, so when adding additional columns the type needs to be either explicitly 
specified, or implicitly defined through the data structure being added. Column data will ultimately end up being represented as a 
Morpehus `Array` within the frame, but it is possible to pass data as an `Iterable` thereby supporting many input types.

The code below demonstrates 5 distinct ways of adding columns to the 10x2 `DataFrame` illustrated above, using the various 
techniques that are supported for single column adds. In some cases we simply pass the desired data type for the column, while
in other cases we pass an `Iterable` in the form of a `Range` or `Array` object. The final state of the frame is shown below.
 
<?prettify?>
```java
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
    formats.withDecimalFormat(Double.class, "0.00;-0.00", 1);
});
```
<div class="frame"><pre class="frame">
   Index     |   A    |   B    |    C    |    D    |  E  |      F       |    G    |
-----------------------------------------------------------------------------------
 2014-01-01  |  4.09  |  2.23  |  false  |  (0,3)  |  0  |  2014-01-01  |  10.00  |
 2014-01-02  |  4.49  |  4.08  |  false  |  (1,3)  |  1  |  2014-01-03  |  20.00  |
 2014-01-03  |  6.77  |  1.94  |  false  |  (2,3)  |  2  |  2014-01-05  |  30.00  |
 2014-01-04  |  1.81  |  4.48  |  false  |  (3,3)  |  3  |  2014-01-07  |  40.00  |
 2014-01-05  |  4.56  |  5.70  |  false  |  (4,3)  |  4  |  2014-01-09  |    NaN  |
 2014-01-06  |  8.56  |  2.96  |  false  |  (5,3)  |  5  |  2014-01-11  |    NaN  |
 2014-01-07  |  7.71  |  7.07  |  false  |  (6,3)  |  6  |  2014-01-13  |    NaN  |
 2014-01-08  |  4.70  |  3.07  |  false  |  (7,3)  |  7  |  2014-01-15  |    NaN  |
 2014-01-09  |  9.78  |  4.28  |  false  |  (8,3)  |  8  |  2014-01-17  |    NaN  |
 2014-01-10  |  8.32  |  4.62  |  false  |  (9,3)  |  9  |  2014-01-19  |    NaN  |
</pre></div>

##### Multiple

Adding multiple columns in one call is also supported as described in this section. The code below creates a similar 10x2 
frame of random double precision values as a starting point, and then adds three columns labelled `A`, `B` and `C` of type 
`String`, and two additional columns presenting the column data directory via a `Range` and `Array` object. One thing to note
about this example is that the data structures for `F` and `G` are shorter than the row count of the original frame, so
they get extended as required with values initializing to the defaults for the type in question. If the data structures are
longer than the row count, they are effectively truncated to fit the frame.

<?prettify?>
```java
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
    formats.withDecimalFormat(Double.class, "0.00;-0.00", 1);
});
```
<div class="frame"><pre class="frame">
   Index     |   A    |   B    |   C    |   D    |   E    |    F    |      G       |
------------------------------------------------------------------------------------
 2014-01-01  |  2.65  |  3.49  |  null  |  null  |  null  |  10.00  |  2014-01-04  |
 2014-01-02  |  4.80  |  2.07  |  null  |  null  |  null  |  11.00  |  2014-01-05  |
 2014-01-03  |  5.81  |  2.70  |  null  |  null  |  null  |  12.00  |  2014-01-06  |
 2014-01-04  |  8.46  |  7.00  |  null  |  null  |  null  |  13.00  |  2014-01-07  |
 2014-01-05  |  4.65  |  4.72  |  null  |  null  |  null  |  14.00  |  2014-01-08  |
 2014-01-06  |  3.39  |  3.95  |  null  |  null  |  null  |    NaN  |  2014-01-09  |
 2014-01-07  |  3.18  |  5.29  |  null  |  null  |  null  |    NaN  |  2014-01-10  |
 2014-01-08  |  1.22  |  7.64  |  null  |  null  |  null  |    NaN  |        null  |
 2014-01-09  |  8.32  |  9.39  |  null  |  null  |  null  |    NaN  |        null  |
 2014-01-10  |  0.66  |  3.38  |  null  |  null  |  null  |    NaN  |        null  |
</pre></div>

##### Frame

Similar to adding rows from one frame to another, it is also possible to add columns from one frame to another. Only columns 
that are missing from the first frame are added by default, unless duplicate handling is changed via `DataFrameOptions`. In 
addition, only data from intersecting rows will be copied. Consider a 9x2 frame initialized with random double precision values
as follows:

<?prettify?>
```java
//Create a 9x2 DataFrame of random double precision values
DataFrame<Integer,String> frame1 = DataFrame.ofDoubles(
    Range.of(0, 9, 1),
    Array.of("A", "B"),
    value -> Math.random()
);
```

<div class="frame"><pre class="frame">
 Index  |    A    |    B    |
-----------------------------
     0  |  0.015  |  0.278  |
     1  |  0.577  |  0.229  |
     2  |  0.837  |  0.273  |
     3  |  0.400  |  0.059  |
     4  |  0.407  |  0.696  |
     5  |  0.318  |  0.657  |
     6  |  0.047  |  0.682  |
     7  |  0.344  |  0.365  |
     8  |  0.633  |  0.476  |
</pre></div>

A second 6x5 frame is initialized with intersecting rows for keys `0`, `2`, `4`, `6` and `8`, and one intersecting column, 
namely for label `B`. The columns for this frame include various data types, namely of `Integer`. `Double`, `String` and
`Boolean`, and all values are initialized by directly presenting a data structure representing the column values.

<?prettify?>
```java
//Create 6x5 frame with intersecting rows and columns to the first frame
DataFrame<Integer,String> frame2 = DataFrame.of(Range.of(0, 12, 2), String.class, columns -> {
    columns.add("B", Array.of(10, 20, 30, 40, 50, 60));
    columns.add("C", Array.of(1d, 3d, 5d, 7d, 9d, 11d));
    columns.add("D", Range.of(1, 7));
    columns.add("E", String.class, v -> String.format("(%s,%s)", v.rowOrdinal(), v.colOrdinal()));
    columns.add("F", Boolean.class, v -> Math.random() > 0.5d);
});
```

<div class="frame"><pre class="frame">
 Index  |  B   |    C     |  D  |    E    |    F    |
-----------------------------------------------------
     0  |  10  |   1.000  |  1  |  (0,3)  |   true  |
     2  |  20  |   3.000  |  2  |  (1,3)  |   true  |
     4  |  30  |   5.000  |  3  |  (2,3)  |   true  |
     6  |  40  |   7.000  |  4  |  (3,3)  |  false  |
     8  |  50  |   9.000  |  5  |  (4,3)  |  false  |
    10  |  60  |  11.000  |  6  |  (5,3)  |  false  |
</pre></div>

Using the `addAll()` method on the `DataFrameColumns` interface we can add columns from the second 6x5 frame to the original 
9x2 frame. Since duplicates are ignored by default, we expect that column `B` from the first frame will be retained and not 
get over written by the values from the second frame. In addition, only data for intersecting rows will get transferred. The
code to combine these frames and the 9x6 result is shown below. 

<?prettify?>
```java
//Add all columns from second frame to first frame, copy data from intersecting rows
frame1.cols().addAll(frame2);
//Print frame to standard out with custom formatting
frame1.out().print(formats -> {
    formats.withDecimalFormat(Double.class, "0.000;-0.000", 1);
});
```
<div class="frame"><pre class="frame">
 Index  |    A    |    B    |    C    |  D  |    E    |    F    |
-----------------------------------------------------------------
     0  |  0.015  |  0.278  |  1.000  |  1  |  (0,3)  |   true  |
     1  |  0.577  |  0.229  |    NaN  |  0  |   null  |  false  |
     2  |  0.837  |  0.273  |  3.000  |  2  |  (1,3)  |   true  |
     3  |  0.400  |  0.059  |    NaN  |  0  |   null  |  false  |
     4  |  0.407  |  0.696  |  5.000  |  3  |  (2,3)  |   true  |
     5  |  0.318  |  0.657  |    NaN  |  0  |   null  |  false  |
     6  |  0.047  |  0.682  |  7.000  |  4  |  (3,3)  |  false  |
     7  |  0.344  |  0.365  |    NaN  |  0  |   null  |  false  |
     8  |  0.633  |  0.476  |  9.000  |  5  |  (4,3)  |  false  |
</pre></div>

##### Concatenation

The above examples demonstrate adding columns to an existing frame which mutates the frame in place. At times it is preferable
however to create a new frame which combines the data of two or more input frames. To do this, a static `concatColumns()` method 
on the `DataFrame` interface exists which cam combine any number of frames. Again, duplicate columns across the input frames are 
ignored with the first frame being the winner, and only data from intersecting rows to the first frame are captured.

Consider 3 frames initialized as follows:

<?prettify?>
```java
//Create a 9x2 DataFrame of random double precision values
DataFrame<Integer,String> frame1 = DataFrame.ofDoubles(
    Range.of(0, 9),
    Array.of("A", "B"),
    value -> Math.random()
);
```

<div class="frame"><pre class="frame">
 Index  |    A    |    B    |
-----------------------------
     0  |  0.027  |  0.526  |
     1  |  0.643  |  0.154  |
     2  |  0.149  |  0.110  |
     3  |  0.670  |  0.085  |
     4  |  0.939  |  0.779  |
     5  |  0.106  |  0.524  |
     6  |  0.665  |  0.842  |
     7  |  0.221  |  0.662  |
     8  |  0.506  |  0.484  |
</pre></div>

<?prettify?>
```java
//Create 6x5 frame with intersecting rows and columns to the first frame
DataFrame<Integer,String> frame2 = DataFrame.of(Range.of(0, 12, 2), String.class, columns -> {
    columns.add("B", Array.of(10, 20, 30, 40, 50, 60));
    columns.add("C", Array.of(1d, 3d, 5d, 7d, 9d, 11d));
    columns.add("D", Range.of(1, 7));
    columns.add("E", String.class, v -> String.format("(%s,%s)", v.rowOrdinal(), v.colOrdinal()));
    columns.add("F", Boolean.class, v -> Math.random() > 0.5d);
});
```

<div class="frame"><pre class="frame">
 Index  |  B   |    C     |  D  |    E    |    F    |
-----------------------------------------------------
     0  |  10  |   1.000  |  1  |  (0,3)  |  false  |
     2  |  20  |   3.000  |  2  |  (1,3)  |   true  |
     4  |  30  |   5.000  |  3  |  (2,3)  |  false  |
     6  |  40  |   7.000  |  4  |  (3,3)  |   true  |
     8  |  50  |   9.000  |  5  |  (4,3)  |   true  |
    10  |  60  |  11.000  |  6  |  (5,3)  |   true  |
</pre></div>

<?prettify?>
```java
//Create a 9x4 DataFrame of random double precision values
DataFrame<Integer,String> frame3 = DataFrame.ofDoubles(
    Range.of(0, 5),
    Array.of("B", "F", "G", "H"),
    value -> Math.random()
);
```

<div class="frame"><pre class="frame">
 Index  |    B    |    F    |    G    |    H    |
-------------------------------------------------
     0  |  0.899  |  0.959  |  0.182  |  0.960  |
     1  |  0.138  |  0.941  |  0.111  |  0.124  |
     2  |  0.582  |  0.739  |  0.446  |  0.868  |
     3  |  0.049  |  0.594  |  0.133  |  0.602  |
     4  |  0.592  |  0.738  |  0.832  |  0.380  |
</pre></div>

Now we use `concatColumns()` to create a new frame combining the columns of all 3, ignoring duplicates. The second frame's
column `B` will be ignored in favour of the first frame, and the third frame's column `B` and `F` will be ignored in favour
of the first and second frame respectively. Only data from rows intersecting with the first frame's row axis will be included
in the combined frame.

<?prettify?>
```java
//Concatenate columns from all 3 frames to create a new result
DataFrame<Integer,String> frame4 = DataFrame.concatColumns(frame1, frame2, frame3);
//Print frame to standard out with custom formatting
frame4.out().print(formats -> {
    formats.withDecimalFormat(Double.class, "0.000;-0.000", 1);
});
```

<div class="frame"><pre class="frame">
 Index  |    A    |    B    |    C    |  D  |    E    |    F    |    G    |    H    |
-------------------------------------------------------------------------------------
     0  |  0.027  |  0.526  |  1.000  |  1  |  (0,3)  |  false  |  0.182  |  0.960  |
     1  |  0.643  |  0.154  |    NaN  |  0  |   null  |  false  |  0.111  |  0.124  |
     2  |  0.149  |  0.110  |  3.000  |  2  |  (1,3)  |   true  |  0.446  |  0.868  |
     3  |  0.670  |  0.085  |    NaN  |  0  |   null  |  false  |  0.133  |  0.602  |
     4  |  0.939  |  0.779  |  5.000  |  3  |  (2,3)  |  false  |  0.832  |  0.380  |
     5  |  0.106  |  0.524  |    NaN  |  0  |   null  |  false  |    NaN  |    NaN  |
     6  |  0.665  |  0.842  |  7.000  |  4  |  (3,3)  |   true  |    NaN  |    NaN  |
     7  |  0.221  |  0.662  |    NaN  |  0  |   null  |  false  |    NaN  |    NaN  |
     8  |  0.506  |  0.484  |  9.000  |  5  |  (4,3)  |   true  |    NaN  |    NaN  |
</pre></div>

#### DataFrame Union

Row and column concatenation only copies data based on columns and rows respectively that intersect with the entries in the 
first frame. At times a full union of N frames is desirable, also taking into account the fact that the combined columns may 
not share the same data type. In order to support this, a static method called `union()` on the `DataFrame` interface is 
provided for this purpose.

Consider a 5x2, 6x5 and 6x4 frame with intersecting rows and columns initialized as follows. 

<?prettify?>
```java
//Create a 5x2 DataFrame of random double precision values
DataFrame<Integer,String> frame1 = DataFrame.ofDoubles(
    Range.of(0, 5),
    Array.of("A", "B"),
    value -> Math.random()
);
```

<div class="frame"><pre class="frame">
 Index  |    A    |    B    |
-----------------------------
     0  |  0.968  |  0.013  |
     1  |  0.105  |  0.593  |
     2  |  0.012  |  0.312  |
     3  |  0.489  |  0.015  |
     4  |  0.304  |  0.269  |
</pre></div>

<?prettify?>
```java
//Create 6x5 frame with intersecting rows and columns to the first frame
DataFrame<Integer,String> frame2 = DataFrame.of(Range.of(0, 12, 2), String.class, columns -> {
    columns.add("B", Array.of(10, 20, 30, 40, 50, 60));
    columns.add("C", Array.of(1d, 3d, 5d, 7d, 9d, 11d));
    columns.add("D", Range.of(1, 7));
    columns.add("E", String.class, v -> String.format("(%s,%s)", v.rowOrdinal(), v.colOrdinal()));
    columns.add("F", Boolean.class, v -> Math.random() > 0.5d);
});
```

<div class="frame"><pre class="frame">
 Index  |  B   |    C     |  D  |    E    |    F    |
-----------------------------------------------------
     0  |  10  |   1.000  |  1  |  (0,3)  |  false  |
     2  |  20  |   3.000  |  2  |  (1,3)  |  false  |
     4  |  30  |   5.000  |  3  |  (2,3)  |   true  |
     6  |  40  |   7.000  |  4  |  (3,3)  |   true  |
     8  |  50  |   9.000  |  5  |  (4,3)  |  false  |
    10  |  60  |  11.000  |  6  |  (5,3)  |   true  |
</pre></div>

<?prettify?>
```java
//Create a 6x4 DataFrame of random double precision values
DataFrame<Integer,String> frame3 = DataFrame.ofDoubles(
    Range.of(0, 6),
    Array.of("B", "F", "G", "H"),
    value -> Math.random()
);
```

<div class="frame"><pre class="frame">
 Index  |    B    |    F    |    G    |    H    |
-------------------------------------------------
     0  |  0.924  |  0.975  |  0.751  |  0.764  |
     1  |  0.441  |  0.160  |  0.626  |  0.477  |
     2  |  0.777  |  0.549  |  0.568  |  0.209  |
     3  |  0.148  |  0.178  |  0.320  |  0.500  |
     4  |  0.880  |  0.351  |  0.025  |  0.998  |
     5  |  0.780  |  0.839  |  0.479  |  0.607  |
</pre></div>

The union of these 3 frames would be a 9x8 structure, as duplicates would be handled on a combine first basis. Note
that column `B` in the first and third frame is of type `double` and in the second frame it's of type `int`. The union
frame will have to represent the combined column `B` as type `Object` for this to work.

The code to create the union frame and the resulting structure are shown below:

<?prettify?>
```java
//Create the union of all 3 frames which should yield an 9x8 frame
DataFrame<Integer,String> frame4 =  DataFrame.union(frame1, frame2, frame3);
//Print frame to standard out with custom formatting
frame4.rows().sort(true);
frame4.out().print(formats -> {
    formats.withDecimalFormat(Double.class, "0.000;-0.000", 1);
});
```

<div class="frame"><pre class="frame">
 Index  |    A    |    B    |    C     |  D  |    E    |    F    |    G    |    H    |
--------------------------------------------------------------------------------------
     0  |  0.968  |  0.013  |   1.000  |  1  |  (0,3)  |  false  |  0.751  |  0.764  |
     1  |  0.105  |  0.593  |     NaN  |  0  |   null  |  false  |  0.626  |  0.477  |
     2  |  0.012  |  0.312  |   3.000  |  2  |  (1,3)  |  false  |  0.568  |  0.209  |
     3  |  0.489  |  0.015  |     NaN  |  0  |   null  |  false  |  0.320  |  0.500  |
     4  |  0.304  |  0.269  |   5.000  |  3  |  (2,3)  |   true  |  0.025  |  0.998  |
     5  |    NaN  |  0.780  |     NaN  |  0  |   null  |  false  |  0.479  |  0.607  |
     6  |    NaN  |     40  |   7.000  |  4  |  (3,3)  |   true  |    NaN  |    NaN  |
     8  |    NaN  |     50  |   9.000  |  5  |  (4,3)  |  false  |    NaN  |    NaN  |
    10  |    NaN  |     60  |  11.000  |  6  |  (5,3)  |   true  |    NaN  |    NaN  |
</pre></div>

#### Duplicate Handling

By design, a `DataFrame` row and column axis does not support duplicate keys. When expanding a frame, an attempt 
to add row or column keys which are already present in the respective axis results in a no-op. In addition, 
functions to concatenate or create a union of two or more frames operate on a combine first basis. While this 
may suffice for most use cases, there are scenarios where you would not expect duplicates to exist across frames, 
and would rather such an event raise an exception as opposed to operating on the combine first approach. This 
behaviour, among others, can be achieved via the `DataFrameOptions` class which provides various methods to change 
the behaviour of the currently executing thread via the use of internal `ThreadLocal` variables.

If we consider the union example from the previous section, we can cause it to fail (because there are duplicates 
keys across the input frames) by wrapping the code in a `Callable` to `whileNotIgnoringDuplicates()` as follows.

<?prettify?>
```java
DataFrame<Integer,String> frame4 = DataFrameOptions.whileNotIgnoringDuplicates(() -> {
    return DataFrame.union(frame1, frame2, frame3).rows().sort(true);
});
```

Running this code results in an exception as follows:

<pre>
    Caused by: com.zavtech.morpheus.frame.DataFrameException: A column for key already exists in this frame: B
        at com.zavtech.morpheus.reference.XDataFrameColumns.lambda$addAll$121(XDataFrameColumns.java:139)
        at com.zavtech.morpheus.reference.XDataFrameAxisBase.forEach(XDataFrameAxisBase.java:281)
        at com.zavtech.morpheus.reference.XDataFrameColumns.addAll(XDataFrameColumns.java:135)
        at com.zavtech.morpheus.reference.XDataFrameFactory.lambda$union$8(XDataFrameFactory.java:161)
        at java.util.Arrays$ArrayList.forEach(Arrays.java:3880)
        at com.zavtech.morpheus.reference.XDataFrameFactory.union(XDataFrameFactory.java:159)
        at com.zavtech.morpheus.frame.DataFrame.union(DataFrame.java:407)
        at com.zavtech.morpheus.examples.ReshapingDocs.lambda$union$49(ReshapingDocs.java:304)
        at com.zavtech.morpheus.frame.DataFrameOptions.whileNotIgnoringDuplicates(DataFrameOptions.java:219)
</pre>
 

#### Re-Labelling

A `DataFrame` row and column axis contains keys of a certain data type, and it is often useful to either replace
one or more keys or to switch out an axis entirely. While this does not in effect change the shape of the frame in
terms of the row and column count, it does change its constitution. This section demonstrates how the API can be
used to replace one or more keys in place, or to generate a shallow copy of a frame with an entirely new axis.

Consider a 5x5 frame keyed by `Integer` rows and `String` columns initialized with random doubles as follows:

<?prettify?>
```java
//Create a 5x5 DataFrame of random doubles
DataFrame<Integer,String> frame = DataFrame.ofDoubles(
    Array.of(0, 1, 2, 3, 4),
    Array.of("A", "B", "C", "D", "E"),
    value -> Math.random() * 10d
);
```

<div class="frame"><pre class="frame">
 Index  |    A    |    B    |    C    |    D    |    E    |
-----------------------------------------------------------
     0  |  2.995  |  9.439  |  2.817  |  0.973  |  5.424  |
     1  |  9.350  |  5.138  |  8.626  |  0.368  |  7.935  |
     2  |  5.407  |  2.157  |  4.207  |  8.540  |  4.385  |
     3  |  8.410  |  6.536  |  8.796  |  8.220  |  7.743  |
     4  |  0.248  |  6.475  |  2.560  |  8.391  |  5.810  |
</pre></div>

The `DataFraneAxis` interface exposes a `replaceKey()` method that can be used to change any number of keys in place. 
The only two caveats are that the replacement key must not already exist in the axis, as duplicates are not supported,
and the replacment key must be of the same data type. If the replacement key does already exist, expect a `DataFrameException` 
to be raised. The code below demonstrates how to replace a single key in both the row oe column axis, and prints the result. 

<?prettify?>
```java
frame.rows().replaceKey(4, 40);
frame.cols().replaceKey("C", "X");
frame.out().print(formats -> {
    formats.withDecimalFormat(Double.class, "0.000;-0.000", 1);
});
```

<div class="frame"><pre class="frame">
 Index  |    A    |    B    |    X    |    D    |    E    |
-----------------------------------------------------------
     0  |  2.995  |  9.439  |  2.817  |  0.973  |  5.424  |
     1  |  9.350  |  5.138  |  8.626  |  0.368  |  7.935  |
     2  |  5.407  |  2.157  |  4.207  |  8.540  |  4.385  |
     3  |  8.410  |  6.536  |  8.796  |  8.220  |  7.743  |
    40  |  0.248  |  6.475  |  2.560  |  8.391  |  5.810  |
</pre></div>

While the above technique is useful to replace specific keys, it is often necessary to replace an axis entirely, and 
possibly with a different key type. Consider the 10x5 frame of random double precision values below which is keyed by 
`LocalDate` on the row axis, and `String` on the column axis.

<?prettify?>
```java
//Create a 10x4 DataFrame of random doubles
DataFrame<LocalDate,String> frame = DataFrame.ofDoubles(
    Range.ofLocalDates("2014-01-01", "2014-01-11"),
    Array.of("A", "B", "C", "D"),
    value -> Math.random() * 10d
);
```

<div class="frame"><pre class="frame">
   Index     |    A    |    B    |    C    |    D    |
------------------------------------------------------
 2014-01-01  |  1.725  |  7.107  |  1.979  |  5.899  |
 2014-01-02  |  1.232  |  5.779  |  6.550  |  7.534  |
 2014-01-03  |  9.706  |  7.316  |  1.277  |  1.122  |
 2014-01-04  |  2.382  |  9.778  |  6.011  |  1.200  |
 2014-01-05  |  3.800  |  7.071  |  2.743  |  8.742  |
 2014-01-06  |  5.157  |  1.242  |  3.834  |  0.047  |
 2014-01-07  |  4.474  |  0.511  |  5.953  |  5.698  |
 2014-01-08  |  4.784  |  6.298  |  5.771  |  3.975  |
 2014-01-09  |  9.525  |  7.301  |  8.257  |  3.144  |
 2014-01-10  |  6.245  |  9.614  |  8.318  |  0.056  |
</pre></div>

Let us assume that the data for this hypothetical experient needs to be date shifted forward by 5 calendar days,
and the row axis `LocalDate` objects need to be converted to `LocalDateTime` objects with a time set to 13:30. One 
way to achieve this would be to create an entirely new frame, and copy the data across, although this would not be 
particularly efficient, especially if the frame was extremely large. A better way is to use the `mapKeys()` method
on `DataFeameAxis` and provide a mapping function that does the conversion as shown below.

<?prettify?>
```java
DataFrame<LocalDateTime,String> shifted = frame.rows().mapKeys(row -> {
    final LocalDate rowKey = row.key().plusDays(5);
    return LocalDateTime.of(rowKey, LocalTime.of(13, 30));
});
```

<div class="frame"><pre class="frame">
      Index        |    A    |    B    |    C    |    D    |
------------------------------------------------------------
 2014-01-06T13:30  |  1.725  |  7.107  |  1.979  |  5.899  |
 2014-01-07T13:30  |  1.232  |  5.779  |  6.550  |  7.534  |
 2014-01-08T13:30  |  9.706  |  7.316  |  1.277  |  1.122  |
 2014-01-09T13:30  |  2.382  |  9.778  |  6.011  |  1.200  |
 2014-01-10T13:30  |  3.800  |  7.071  |  2.743  |  8.742  |
 2014-01-11T13:30  |  5.157  |  1.242  |  3.834  |  0.047  |
 2014-01-12T13:30  |  4.474  |  0.511  |  5.953  |  5.698  |
 2014-01-13T13:30  |  4.784  |  6.298  |  5.771  |  3.975  |
 2014-01-14T13:30  |  9.525  |  7.301  |  8.257  |  3.144  |
 2014-01-15T13:30  |  6.245  |  9.614  |  8.318  |  0.056  |
</pre></div>

Unlike the `replaceKey()` method, `mapKeys()` takes a lambda function to generate an entirely new axis, and returns
a new `DataFrame` initialized with the replacement axis. The data in the shifted frame is referentially the same as the 
original frame, so the cost of this operation is limited to the cost of creating the new axis. We can confirm this by 
setting all the values in the shifted frame to zero, and then confirming the original frame has also been zero'd out.

<?prettify?>
```java
//Zero out shifted frame values, and print original frame
shifted.applyDoubles(v -> 0d);
frame.out().print(formats -> {
    formats.withDecimalFormat(Double.class, "0.000;-0.000", 1);
});
```

<div class="frame"><pre class="frame">
   Index     |    A    |    B    |    C    |    D    |
------------------------------------------------------
 2014-01-01  |  0.000  |  0.000  |  0.000  |  0.000  |
 2014-01-02  |  0.000  |  0.000  |  0.000  |  0.000  |
 2014-01-03  |  0.000  |  0.000  |  0.000  |  0.000  |
 2014-01-04  |  0.000  |  0.000  |  0.000  |  0.000  |
 2014-01-05  |  0.000  |  0.000  |  0.000  |  0.000  |
 2014-01-06  |  0.000  |  0.000  |  0.000  |  0.000  |
 2014-01-07  |  0.000  |  0.000  |  0.000  |  0.000  |
 2014-01-08  |  0.000  |  0.000  |  0.000  |  0.000  |
 2014-01-09  |  0.000  |  0.000  |  0.000  |  0.000  |
 2014-01-10  |  0.000  |  0.000  |  0.000  |  0.000  |
</pre></div>

#### Transpose

When a `DataFrame` is transposed, it changes from being a column store data structure to row store, making the tranpose
operation itself, even for very large frames, very cheap. One drawback of this design choice however is that a transposed 
frame cannot be reshaped by adding rows and/or columns.  Attempting to re-shape a transposed `DataFrame` will result in a 
`DataFrameException` as the following example demonstrates.

<?prettify?>
```java
//Create a 5x5 DataFrame of random doubles
DataFrame<Integer,String> frame = DataFrame.ofDoubles(
    Array.of(0, 1, 2, 3, 4),
    Array.of("A", "B", "C", "D", "E"),
    value -> Math.random() * 10d
);

//Transpose and try add a row...
frame.transpose().rows().add("F");
```

<pre>
com.zavtech.morpheus.frame.DataFrameException: Cannot add rows to a transposed DataFrame, call transpose() and then add columns
	at com.zavtech.morpheus.reference.XDataFrameContent.addRow(XDataFrameContent.java:280)
	at com.zavtech.morpheus.reference.XDataFrameRows.add(XDataFrameRows.java:77)
	at com.zavtech.morpheus.reference.XDataFrameRows.add(XDataFrameRows.java:64)
	at com.zavtech.morpheus.examples.ReshapingDocs.transpose(ReshapingDocs.java:403)
</pre>

The workaround to this limitation is to call `transpose()` on the row major version of the frame, which yields the un-tranposed 
original that can be re-shaped by adding rows and/or columns. Consider the same 5x5 frame created above, which we transpose,
and then add a new column to the transposed frame by calling `addRow()` as shown below.

<?prettify?>
```java
//Tranpose the transposed frame to get back to a colum store which can be re-shaped
DataFrame<String,Integer> transposed = frame.transpose();
transposed.transpose().rows().add(5);
transposed.out().print(formats -> {
    formats.withDecimalFormat(Double.class, "0.00;-0.00", 1);
});
```

<div class="frame"><pre class="frame">
 Index  |   0    |   1    |   2    |   3    |   4    |   5   |
--------------------------------------------------------------
     A  |  4.23  |  6.74  |  3.33  |  7.19  |  6.87  |  NaN  |
     B  |  4.61  |  7.58  |  5.55  |  9.78  |  7.32  |  NaN  |
     C  |  0.54  |  3.68  |  6.18  |  4.06  |  2.88  |  NaN  |
     D  |  9.48  |  9.36  |  4.10  |  8.89  |  3.68  |  NaN  |
     E  |  1.44  |  1.64  |  3.81  |  7.75  |  6.25  |  NaN  |
</pre></div>
