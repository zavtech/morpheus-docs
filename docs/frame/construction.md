### Introduction

A `DataFrame` is a column storage optimized structure where each column is configured to hold a specific data type,
and can either be densely or sparsely populated. Once created, a `DataFrame` can be reshaped by adding additional
rows and columns, with restrictions in some cases, which are discussed later.

More often than not, a `DataFrame` will likely be initialized from the contents of a file or a database, but it
is also possible to build one incrementally through the API. The following sections describe various ways in which
a DataFrame can be created.

### Construction

#### Programmatically

There are a number of static methods on the DataFrame interface that can be used create frames that are optimized
to hold specific types of data. For example, a commonly used frame for numerical analysis would be one optimized
to hold double precision values. The code below illustrates various common construction calls for the supported
data types.

<?prettify?>
```java
import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;

//Create a list of column keys
Iterable<Month> months = Array.of(Month.class, Month.values());
//Create a list of row keys
Iterable<Year> years = Range.of(1995, 2000).map(Year::of);

//Create frames optimized to hold various types of data.
DataFrame<Year,Month> booleans = DataFrame.ofBooleans(years, months);
DataFrame<Year,Month> integers = DataFrame.ofInts(years, months);
DataFrame<Year,Month> longs = DataFrame.ofLongs(years, months);
DataFrame<Year,Month> doubles = DataFrame.ofDoubles(years, months);
DataFrame<Year,Month> objects = DataFrame.ofValues(years, months);
```
#### Single Column

Factory methods are provided to easily create a frame with one column.

<?prettify?>
```java
//Create a frame with a single column initially
DataFrame<Year,Month> booleans = DataFrame.ofBooleans(years, Month.JANUARY);
DataFrame<Year,Month> integers = DataFrame.ofInts(years, Month.JANUARY);
DataFrame<Year,Month> longs = DataFrame.ofLongs(years, Month.JANUARY);
DataFrame<Year,Month> doubles = DataFrame.ofDoubles(years, Month.JANUARY);
DataFrame<Year,Month> objects = DataFrame.ofValues(years, Month.JANUARY);
```
#### Single Row

Factory methods are provided to easily create a frame with one row.

<?prettify?>
```java
//Create a frame with a single row initially
DataFrame<Year,Month> booleans = DataFrame.ofBooleans(Year.of(2014), months);
DataFrame<Year,Month> integers = DataFrame.ofInts(Year.of(2014), months);
DataFrame<Year,Month> longs = DataFrame.ofLongs(Year.of(2014), months);
DataFrame<Year,Month> doubles = DataFrame.ofDoubles(Year.of(2014), months);
DataFrame<Year,Month> objects = DataFrame.ofValues(Year.of(2014), months);
```
#### Mixed Column Types

The example below demonstrates how to create a `DataFrame` where each column is configured to hold
a different data type, similar to the way a SQL table might be structured. In this case, 5 columns 
are created via calls to `columns.add()` which takes the column key and the data type for the column. 
In addition, the column values in this example are initialized with random values via the type specific 
`applyXXX()` methods.

<?prettify?>
```java
//Create a frame with 5 columns each optimized for a different data type and randomly initialized values
Random rand = new java.util.Random();
DataFrame<Year,Month> randomFrame = DataFrame.of(years, Month.class, columns -> {
    columns.add(Month.JANUARY, Boolean.class).applyBooleans(v -> rand.nextBoolean()); 
    columns.add(Month.MARCH, Integer.class).applyInts(v -> rand.nextInt());       
    columns.add(Month.JUNE, Long.class).applyLongs(v -> rand.nextLong());           
    columns.add(Month.SEPTEMBER, Double.class).applyDoubles(v -> rand.nextDouble());    
    columns.add(Month.DECEMBER, Object.class).applyValues(v -> String.valueOf(rand.nextDouble()));
});
```
On occasion, it might be desirable to create an empty DataFrame which can be expanded over time, by adding
keys to the frame's row and column axis. Methods are provided to add keys individually or in bulk via 
`Iterable` as shown below. When adding columns, the class representing the data type for that column must
be specified.

<?prettify?>
```java
//Create an empty frame with initial capacity, then add rows and columns
DataFrame<Year,Month> frame = DataFrame.empty(Year.class, Month.class);
frame.rows().add(Year.of(1975));
frame.rows().add(Year.of(1980));
frame.rows().addAll(Range.of(1995, 2014).map(Year::of));
frame.cols().add(Month.JANUARY, Double.class);
frame.cols().add(Month.MARCH, Double.class);
frame.cols().addAll(Array.of(Month.APRIL, Month.JULY), Double.class);
```

### From CSV

The Morpheus library includes an API to initialise a `DataFrame` from a CSV data source. The parser 
will attempt to guess the column types from the first N rows in the sample (which is configurable), 
in the absence of explicitly configured column types. It supports CSV formats with and without a header,
and can optionally utilize a user provided function to generate the keys for the row axis. If a row key
generation function is not provided, a sequence of integers will be used by default. The following section 
illustrates various parsing examples, beginning with simple cases and progressing to more elaborate 
examples.

<?prettify?>
```java
//Parse file or classpath resource, with first row as header
DataFrame<Integer,String> frame1 = DataFrame.readCsv("/temp/data.csv");

//Parse URL, with first row as header
DataFrame<Integer,String> frame2 = DataFrame.readCsv("http://www.domain.com/data?file.csv");

//Parse file, with first row as header, and row keys parsed as LocalDates from the first column, index=0
DataFrame<LocalDate,String> frame3 = DataFrame.readCsv(options -> {
    options.withResource("/temp/data.csv");
    options.withRowKeyParser(LocalDate.class, row -> LocalDate.parse(row[0]));
});
```
The third case illustrated above demonstrates the `DataFrame.readCsv()` method that uses a lambda expression
which accepts a `CSVRequest` object that can be used to specify all sorts of customizations regarding how the
CSV data is parsed into a `DataFrame`. The features supported by the request descriptor are as follows:

* Column specific parsers
* Function to create row keys from raw tokens in a row
* Predicate to select a subset of rows
* Predicate to select a subset of columns by index
* Predicate to select a subset of columns by column name
* Specific character encoding, e.g. UTF-16
* Row count batch size which can influence performance
* Parallel processing of CSV content to improve performance for large files.

Let's consider a real-world example where we wish to parse a CSV file from Yahoo Finance which contains historical
prices for the S&P 500 index. A sample of the file can be downloaded [here](http://chart.finance.yahoo.com/table.csv?s=SPY&a=0&b=1&c=2013&d=5&e=6&f=2014&g=d&ignore=.csv) and the first 10 rows in the file
are shown below.

<pre class="frame">
Date,Open,High,Low,Close,Volume,Adj Close
2014-06-06,194.869995,195.429993,194.779999,195.380005,78696000,185.713099
2014-06-05,193.410004,194.649994,192.699997,194.449997,92103000,184.829105
2014-06-04,192.470001,193.300003,192.270004,193.190002,55529000,183.631452
2014-06-03,192.429993,192.899994,192.25,192.800003,65047000,183.260749
2014-06-02,192.949997,192.990005,191.970001,192.899994,64656000,183.355793
2014-05-30,192.190002,192.800003,192.029999,192.679993,76316000,183.146676
2014-05-29,191.820007,192.399994,191.330002,192.369995,64377000,182.852017
2014-05-28,191.520004,191.820007,191.059998,191.380005,66723000,181.911009
2014-05-27,191.059998,191.580002,190.949997,191.520004,72010000,182.044081
2014-05-23,189.759995,190.479996,189.589996,190.350006,61092800,180.931972
</pre>

The code below will parse this into a `DataFrame` with a row axis made up of `LocalDate` objects as per
the row key function. A specific parser is defined for the `Volume` column in order to force the type
to a `long`, and the rest of the columns are left to the default behaviour, which in this case will
all resolve to `double` type.

<?prettify?>
```java
DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
String url = "http://chart.finance.yahoo.com/table.csv?s=SPY&a=0&b=1&c=2013&d=5&e=6&f=2014&g=d&ignore=.csv";
DataFrame<LocalDate,String> frame = DataFrame.readCsv(options -> {
    options.withResource(url);
    options.withRowKeyParser(LocalDate.class, values -> LocalDate.parse(values[0], dateFormat));
    options.withParser("Volume", v -> v == null ? 0L : Long.parseLong(v));
});
```

<div class="frame"><pre class="frame">
   Index     |     Date     |     Open     |     High     |     Low      |    Close     |   Volume   |  Adj Close   |
---------------------------------------------------------------------------------------------------------------------
 2014-06-06  |  2014-06-06  |  194.869995  |  195.429993  |  194.779999  |  195.380005  |  78696000  |  184.624915  |
 2014-06-05  |  2014-06-05  |  193.410004  |  194.649994  |  192.699997  |  194.449997  |  92103000  |  183.746101  |
 2014-06-04  |  2014-06-04  |  192.470001  |  193.300003  |  192.270004  |  193.190002  |  55529000  |  182.555466  |
 2014-06-03  |  2014-06-03  |  192.429993  |  192.899994  |    192.2500  |  192.800003  |  65047000  |  182.186934  |
 2014-06-02  |  2014-06-02  |  192.949997  |  192.990005  |  191.970001  |  192.899994  |  64656000  |  182.281421  |
 2014-05-30  |  2014-05-30  |  192.190002  |  192.800003  |  192.029999  |  192.679993  |  76316000  |   182.07353  |
 2014-05-29  |  2014-05-29  |  191.820007  |  192.399994  |  191.330002  |  192.369995  |  64377000  |  181.780597  |
 2014-05-28  |  2014-05-28  |  191.520004  |  191.820007  |  191.059998  |  191.380005  |  66723000  |  180.845103  |
 2014-05-27  |  2014-05-27  |  191.059998  |  191.580002  |  190.949997  |  191.520004  |  72010000  |  180.977396  |
 2014-05-23  |  2014-05-23  |  189.759995  |  190.479996  |  189.589996  |  190.350006  |  61092800  |  179.871803  |
</pre></div>

#### Parallel Loading

Other features of the `CsvRequest` discussed below can improve performance when reading very large files. The
first is to turn on parallel processing and the second is to vary the batch size. The batch size can be
influential, especially when the parser which converts a raw string from the CSV content to another type is an
expensive operation. 

The chart below shows some performance statistics comparing parallel versus sequential loading of a 40MB file 
containing roughly 760,000 rows of CSV content.  While the absolute figures are very machine specific, the relative 
difference does suggest that parallel loading can make a material improvement on a multi-core machine, which
is pretty standard issue these days.

<p align="center">
    <img class="chart" src="../../images/morpheus-parse-csv-times.png"/>
</p>

The code to produce this plot is as follows:

<?prettify?>
```java
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.viz.chart.Chart;

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

Chart.create().withBarPlot(timingStats, false, chart -> {
    chart.title().withText("CSV Parsing Performance (Sequential vs Parallel)");
    chart.subtitle().withText("File Size: 40MB, 760,000 lines, 6 columns");
    chart.title().withFont(new Font("Verdana", Font.PLAIN, 16));
    chart.plot().axes().domain().label().withText("Statistic");
    chart.plot().axes().range(0).label().withText("Time in Milliseconds");
    chart.legend().on();
    chart.show();
});
```
#### Row / Column Filtering

Morpheus supports both CSV row and column filtering at load time, which can be convenient if you only
need to analyze a subsection of a very large file. The alternative would be to simply load the entire file,
and then filter the `DataFrame`, but this could be very inefficient from a memory perspective. The example
below demonstrates both row and column filtering of the **Yahoo Finance** quote data set described above, 
where we limit the extraction to the **Open**, **Close** and **Adj Close** columns, and also to only include 
rows that fall on a **Monday**.

<?prettify?>
```java
DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
Set<String> columnSet = Collect.asSet("Open", "Close", "Adj Close");
String url = "http://chart.finance.yahoo.com/table.csv?s=SPY&a=0&b=1&c=2013&d=5&e=6&f=2014&g=d&ignore=.csv";
DataFrame<LocalDate,String> frame = DataFrame.readCsv(options -> {
    options.withResource(url);
    options.withColNamePredicate(columnSet::contains);
    options.withRowKeyParser(LocalDate.class, values -> LocalDate.parse(values[0], dateFormat));
    options.withParser("Volume", v -> v == null ? 0L : Long.parseLong(v));
    options.withRowPredicate(row -> {
        LocalDate date = LocalDate.parse(row[0], dateFormat);
        return date.getDayOfWeek() == DayOfWeek.MONDAY;
    });
});
```

<pre class="frame">
   Index     |     Open     |    Close     |  Adj Close   |
-----------------------------------------------------------
 2014-06-02  |  192.949997  |  192.899994  |  182.281421  |
 2014-05-19  |  187.690002  |  188.740005  |  178.350428  |
 2014-05-12  |  188.800003  |  189.789993  |  179.342617  |
 2014-05-05  |  187.139999  |  188.419998  |  178.048036  |
 2014-04-28  |  187.050003  |  186.880005  |  176.592815  |
 2014-04-21  |  186.440002  |  187.039993  |  176.743996  |
 2014-04-14  |  182.929993  |  182.940002  |  172.869698  |
 2014-04-07  |  185.949997  |  184.339996  |  174.192626  |
 2014-03-31  |  186.669998  |  187.009995  |  176.715649  |
 2014-03-24  |  186.839996  |  185.429993  |  175.222621  |
</pre>

### From JSON

The Morpheus library has the ability to read and write a `DataFrame` to a JSON format, which makes it easy
to integrate with a web application. The parser leverages the [Google GSON](https://github.com/google/gson)
library, and specifically the stream based API for both reading and writing, which allows it to handle very large
JSON files. An example of the format is shown below, which depicts a frame with 3 rows and 2 columns, a row axis with
keys of type `LocalDate`, and a column of booleans and a column of integers.

<?prettify lang=json?>
```json
{
  "DataFrame": {
    "rowCount": 3,
    "colCount": 2,
    "rowKeys": {
      "type": "LocalDate",
      "values": [
        "2015-12-07",
        "2015-12-08",
        "2015-12-09",
      ]
    },
    "columns": [
      {
        "key": "ColumnName1",
        "keyType": "String",
        "dataType": "boolean",
        "nullValue": "false",
        "values": [
          "true",
          "false",
          "true",
        ]
      },
      {
        "key": "ColumnName2",
        "keyType": "String",
        "dataType": "int",
        "nullValue": "0",
        "values": [
          "884841579",
          "72476677",
          "478622964",
        ]
      }
   ]
  }
}
```

Two static `DataFrame.fromJson()` functions exist to load a frame from a file, URL or classpath resource,
in much the same way as the CSV functions. Row and column predicates can be used to select a subset of the
data as required. The following code demonstrates some basic examples:

<?prettify?>
```java
//Parse a file, or classpath resource from Morpheus JSON format
DataFrame<LocalDate,String> frame = DataFrame.readJson("/temp/data.json");
```
To select a subset of rows and columns, apply predicates to the request as below.

<?prettify?>
```java
//Parse a file, or classpath resource from Morpheus JSON format, selecting only a subset of rows & columns
final Set<String> columns = Stream.of("Date", "PostCode", "Street", "County").collect(Collectors.toSet());
DataFrame<LocalDate,String> frame = DataFrame.readJson(options -> {
    options.withResource("/temp/data.json");
    options.withCharset(StandardCharsets.UTF_16);
    options.withRowPredicate(rowKey -> rowKey.getDayOfWeek() == DayOfWeek.MONDAY);
    options.withColPredicate(columns::contains);
});
```


### From SQL

The Morpheus library ships with a `DataFrameSource` implementation designed to initialize `DataFrames` from
the results of a SQL query. The following section illustrates various examples of how the API can be used
in this regard. 

As a convenience, a static `readDb()` method exists on the `DataFrame` class which takes as an argument a 
function that configures the details of the request. At a minimum, two inputs are required on the request 
descriptor, namely the database connection (or the details of how to establish a connection), and a SQL 
expression. Many other aspects of the request can be tailored, but the most basic example is as follows:

<?prettify?>
```java
import com.zavtech.morpheus.frame.DataFrame;

//Ensure the JDBC driver is loaded 
Class.forName("org.h2.Driver");

//Create a frame from a select statement
DataFrame<Integer,String> frame = DataFrame.readDb(options -> {
    options.withConnection("jdbc:h2://databases/testDb", "sa", null);
    options.withSql("select * from Customer where city = 'London'");
});
```
By default, the first column in the SQL `ResultSet` is used to initialize the row keys of the `DataFrame`,
and in the example above, this amounts to the unique id of the Customer record. The API does provide
for greater control by supporting a user defined function that can be applied to generate the key for 
each row in the SQL `ResultSet`. A good example of where this would be necessary is the case of a 
composite key.

<?prettify?>
```java
import javax.sql.DataSource;
import com.zavtech.morpheus.util.Tuple;
import com.zavtech.morpheus.frame.DataFrame;

// Join products and inventory to see what we have where
javax.sql.DataSource dataSource = getDataSource();
DataFrame<Tuple,String> frame = DataFrame.readDb(options -> {
    options.withConnection(dataSource);
    options.withSql("select * from Product t1 inner join Inventory t2 on t1.productId = t2.productId");
    options.withExcludeColumns("productId", "warehouseId");  //not need as part of the key
    options.withRowCapacity(1000);
    options.withRowKeyFunction(rs -> {
        String productId = rs.getString("productId");
        String warehouseId = rs.getString("warehouseId");
        return Tuple.of(warehouseId, productId);
    });
});
```
In the above example, because we have supplied our own function to generate a row key from a record in the 
`ResultSet`, we know the resulting frame with be parameterized as `<Tuple,String>`. By default, frames
generated from a database query will always have a `String` based column axis representing the column
names in the `ResultSet`, but these can obviously be mapped to some other type post construction.

It is often desirable to transform data read from a SQL `ResultSet` so that it can be typed more appropriately
or modified in some way. The Morpheus API provides the capability to configure column specific extractors
to perform such transformations. Consider the example below, where we query Customer records which have a
`VARCHAR` column used to capture a user's time zone, and we wish to convert this to a `ZoneId` within 
the resulting `DataFrame`. This example also demonstrates a parameterized query where arguments are
applied using the `withParameters()` method on the request descriptor.

<?prettify?>
```java
import java.time.LocalDate;
import java.time.ZoneId;
import javax.sql.DataSource;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.util.sql.SQLExtractor;

//Convert the user's time zone into a ZoneId 
javax.sql.DataSource dataSource = getDataSource();
DataFrame<Integer,String> frame = DataFrame.readDb(options -> {
    options.withConnection(dataSource);
    options.withSql("select * from Customer where city = ? and dob > ?");
    options.withParameters("London", LocalDate.of(1970, 1, 1));
    options.withExtractor("UserZone", SQLExtractor.with(ZoneId.class, (rs, colIndex) -> {
        final String tzName = rs.getString(colIndex);
        return tzName != null ? ZoneId.of(tzName) : null;
    }));
});
```