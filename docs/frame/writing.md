### Writing Data

#### Introduction

The Morpheus library provides a simple yet powerful API to write a `DataFrame` to an output device for persistent
storage, which can then be later read back into memory. Currently there is built in support for writing a frame out 
to CSV and JSON formats, as well as to SQL databases (current support is limited to MYSQL, Microsoft SQL Server, HSQLDB 
and Sqlite). It is easy to write a custom sink which integrates naturally with the Morpheus API, and an example of this 
is provided in a later section below.

The `DataFrameWrite` interface which can be accessed by calling `write()` on a frame instance, provides format specific 
output methods, as well as a generalized method for user defined sinks. The following sections cover CSV, JSON and database
output, as well as a custom sink that records a frame as a Java serialized object. For the examples in this section, 
consider the following 10x7 containing various data types for the columns to illustrate how these can be mapped 
to the output.

<?prettify?>
```java
final LocalDate start = LocalDate.of(2014, 1, 1);
final Range<LocalDate> rowKeys = Range.of(start, start.plusDays(10));
DataFrame<LocalDate,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
    columns.add("Column-0", Boolean.class, v -> Math.random() > 0.5d);
    columns.add("Column-1", Double.class, v -> Math.random() * 10d);
    columns.add("Column-2", LocalTime.class, v -> LocalTime.now().plusMinutes(v.rowOrdinal()));
    columns.add("Column-3", Month.class, v -> Month.values()[v.rowOrdinal()+1]);
    columns.add("Column-4", Integer.class, v -> (int)(Math.random() * 10));
    columns.add("Column-5", String.class, v -> String.format("(%s,%s)", v.rowOrdinal(), v.colOrdinal()));
    columns.add("Column-6", LocalDateTime.class, v -> LocalDateTime.now().plusMinutes(v.rowOrdinal()));
});
```

<div class="frame"><pre class="frame">
   Index     |  Column-0  |   Column-1   |    Column-2    |  Column-3   |  Column-4  |  Column-5  |         Column-6          |
-------------------------------------------------------------------------------------------------------------------------------
 2014-01-01  |     false  |  1.25672365  |   21:00:13.73  |   FEBRUARY  |         4  |     (0,5)  |  2014-08-02T21:00:13.759  |
 2014-01-02  |      true  |  4.41265583  |  21:01:13.732  |      MARCH  |         3  |     (1,5)  |  2014-08-02T21:01:13.759  |
 2014-01-03  |     false  |  7.82861945  |  21:02:13.732  |      APRIL  |         4  |     (2,5)  |  2014-08-02T21:02:13.759  |
 2014-01-04  |      true  |  1.32584627  |  21:03:13.732  |        MAY  |         4  |     (3,5)  |  2014-08-02T21:03:13.759  |
 2014-01-05  |     false  |  0.55894277  |  21:04:13.732  |       JUNE  |         2  |     (4,5)  |  2014-08-02T21:04:13.759  |
 2014-01-06  |     false  |  3.52911319  |  21:05:13.732  |       JULY  |         2  |     (5,5)  |  2014-08-02T21:05:13.759  |
 2014-01-07  |      true  |  1.12132289  |  21:06:13.732  |     AUGUST  |         6  |     (6,5)  |  2014-08-02T21:06:13.759  |
 2014-01-08  |     false  |  6.78505742  |  21:07:13.732  |  SEPTEMBER  |         8  |     (7,5)  |  2014-08-02T21:07:13.759  |
 2014-01-09  |     false  |  2.79950377  |  21:08:13.732  |    OCTOBER  |         4  |     (8,5)  |  2014-08-02T21:08:13.759  |
 2014-01-10  |     false  |  6.16985695  |  21:09:13.732  |   NOVEMBER  |         4  |     (9,5)  |  2014-08-02T21:09:13.759  |
</pre></div>

#### CSV

Storing tabular data as CSV text is common practice, so it is only natural that a `DataFrame` can be written out in 
this format. The Morpheus library ships with a default CSV output handler called `CsvSink`, however it is possible to 
write a custom CSV sink if necessary. The example below writes the `DataFrame` presented in the introduction to a CSV 
file with various customizations applied via the `CsvSinkOptions` object. The customizations are as follows:

    1. Set the separator, which defulats to a comma.
    2. Set whether the row keys are included in output
    3. Set whether the column keys are included in output
    4. Set the text to print for null values
    5. Set the title for the row key column
    6. Set a custom Printer to format row keys (otherwise defaults to Object.toString())
    7. Set a custim Printer to format column keys (otherwise defaults to Object.toString())
    8. Set type specific and column specific Printers 

<?prettify?>
```java
frame.write().csv(options -> {
    options.setFile("DataFrame-1.csv");
    options.setSeparator(",");
    options.setIncludeRowHeader(true);
    options.setIncludeColumnHeader(true);
    options.setNullText("null");
    options.setTitle("Date");

    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MMM/yyyy");
    options.setRowKeyPrinter(Printer.ofLocalDate(dateFormat));

    options.setFormats(formats -> {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
        formats.setDecimalFormat(Double.class, "0.00##;-0.00##", 1);
        formats.setPrinter("Column-2", Printer.ofLocalTime(timeFormat));
        formats.<Month>setPrinter("Column-3", Printer.forObject(m -> m.name().toLowerCase()));
        formats.setPrinter("Column-6", Printer.ofLocalDateTime(dateTimeFormat));
    });
});
```

The resulting file output is shown below. Note how custom column formats are configured above using either a data type, 
or with an explicit column name. The `CsvSink` will attempt to look up a printer for the column name first, and if no
printer is registered, it will fall back onto a printer for the data type associated with the column. In addition,
an explicit printer can be configured for the row keys, and we can control whether the row header and column header 
are included in the output, which by default they are.

<div class="frame"><pre class="frame">
Date,Column-0,Column-1,Column-2,Column-3,Column-4,Column-5,Column-6
2014-01-01,false,1.2567,21:00,february,4,(0,5),02-Aug-2014 21:00
2014-01-02,true,4.4127,21:01,march,3,(1,5),02-Aug-2014 21:01
2014-01-03,false,7.8286,21:02,april,4,(2,5),02-Aug-2014 21:02
2014-01-04,true,1.3258,21:03,may,4,(3,5),02-Aug-2014 21:03
2014-01-05,false,0.5589,21:04,june,2,(4,5),02-Aug-2014 21:04
2014-01-06,false,3.5291,21:05,july,2,(5,5),02-Aug-2014 21:05
2014-01-07,true,1.1213,21:06,august,6,(6,5),02-Aug-2014 21:06
2014-01-08,false,6.7851,21:07,september,8,(7,5),02-Aug-2014 21:07
2014-01-09,false,2.7995,21:08,october,4,(8,5),02-Aug-2014 21:08
2014-01-10,false,6.1699,21:09,november,4,(9,5),02-Aug-2014 21:09
</pre></div>

The `CsvSinkOptions` allows the output resource to be specified as either a `File`, `URL` or an `OutputStream`. 
The latter is the most flexible in that a user has full control of how the output is written. For example, consider the 
same frame as above, but in this case we wish to write to a URL endpoint via http POST, expressing the frame as GZIP 
compressed CSV. In order to do this, we write the frame to a `ByteArrayOutputStream`, and then perform the http POST with
the resulting bytes.

<?prettify?>
```java
ByteArrayOutputStream baos = new ByteArrayOutputStream();
frame.write().csv(options -> {
    try {
        options.setOutputStream(new GZIPOutputStream(baos));
        options.setSeparator(",");
        options.setIncludeRowHeader(true);
        options.setIncludeColumnHeader(true);
        options.setNullText("null");
        options.setTitle("Date");
        options.setRowKeyPrinter(LocalDate::toString);
        options.setFormats(formats -> {
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
            DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
            formats.setDecimalFormat(Double.class, "0.00##;-0.00##", 1);
            formats.<Month>setPrinter("Column-2", v -> v.name().toLowerCase());
            formats.<LocalTime>setPrinter("Column-1", timeFormat::format);
            formats.<LocalDateTime>setPrinter("Column-5", dateTimeFormat::format);
        });
    } catch (IOException ex) {
        throw new RuntimeException(ex.getMessage(), ex);
    }
});

final byte[] bytes = baos.toByteArray();
HttpClient.getDefault().doPost(post -> {
    post.setRetryCount(5);
    post.setReadTimeout(5000);
    post.setConnectTimeout(1000);
    post.setUrl("http://www.domain.con/test");
    post.setContent(bytes);
    post.setContentType("application/x-gzip");
    post.setContentLength(bytes.length);
    post.setResponseHandler((status, stream) -> {
        if (status.getCode() == 200) {
            return Optional.empty();
        } else {
            throw new RuntimeException("Failed with response: " + status);
        }
    });
});
```

#### JSON

Writing a `DataFrame` out in JSON format works similar to the CSV example above, however the output options differ
slightly. If we use the same 10x7 frame introduced earlier, we can write out JSON with custom formatting as shown below. 

Meta-data in the payload is included to allow a subsequent reader to re-constitute the frame with the appropriate data 
types. For example, `Integer` and `Long` types are not distinguishable in standard JSON output, but the `dataType` field 
which is written as a JSON property for each column object definition, provides the required information. Other types, 
like `LocalTime`, and `LocalDateTime` in this example are formatted into a string which can be controlled via the options 
`Formats` entity, much the same way as CSV formatting works. The data type property for objects like these is mapped
to the simple name of the class.
 
<?prettify?>
```java
frame.write().json(options -> {
    options.setFile("DataFrame-1.json");
    options.setEncoding("UTF-8");
    options.setFormats(formats -> {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
        formats.setPrinter("Column-2", Printer.ofLocalTime(timeFormat));
        formats.<Month>setPrinter("Column-3", Printer.forObject(m -> m.name().toLowerCase()));
        formats.setPrinter("Column-6", Printer.ofLocalDateTime(dateTimeFormat));
    });
});
```

The resulting JSON output is shown below. It should be noted that the `JsonSink` implementation that ships with Morpheus
uses a stream based JsonWriter, making it very memory efficient even for extremely large frames. The same applies in reverse
with the `JsonSource` implementation. Finally, the JSON payload includes the row and column count for the frame which is
important to enable the `JsonSource` to pre-allocate the arrays when reading, which again, improves performance for very 
large frames.

```json
{
  "DataFrame": {
    "rowCount": 10,
    "colCount": 7,
    "rowKeys": {
      "type": "LocalDate",
      "values": [
        "2014-01-01",
        "2014-01-02",
        "2014-01-03",
        "2014-01-04",
        "2014-01-05",
        "2014-01-06",
        "2014-01-07",
        "2014-01-08",
        "2014-01-09",
        "2014-01-10"
      ]
    },
    "columns": [
      {
        "key": "Column-0",
        "keyType": "String",
        "dataType": "Boolean",
        "defaultValue": false,
        "values": [
          true,
          true,
          true,
          true,
          false,
          false,
          true,
          false,
          true,
          false
        ]
      },
      {
        "key": "Column-1",
        "keyType": "String",
        "dataType": "Double",
        "defaultValue": null,
        "values": [
          3.4243295493435575,
          1.1438780283716343,
          4.316506413470317,
          7.454588915346636,
          6.950360875904566,
          9.491531171790896,
          5.204554855321635,
          5.754177059739899,
          0.8195736745281579,
          6.988186292016216
        ]
      },
      {
        "key": "Column-2",
        "keyType": "String",
        "dataType": "LocalTime",
        "defaultValue": null,
        "values": [
          "21:24",
          "21:25",
          "21:26",
          "21:27",
          "21:28",
          "21:29",
          "21:30",
          "21:31",
          "21:32",
          "21:33"
        ]
      },
      {
        "key": "Column-3",
        "keyType": "String",
        "dataType": "Month",
        "defaultValue": null,
        "values": [
          "february",
          "march",
          "april",
          "may",
          "june",
          "july",
          "august",
          "september",
          "october",
          "november"
        ]
      },
      {
        "key": "Column-4",
        "keyType": "String",
        "dataType": "Integer",
        "defaultValue": 0,
        "values": [
          9,
          9,
          5,
          7,
          0,
          2,
          8,
          5,
          5,
          0
        ]
      },
      {
        "key": "Column-5",
        "keyType": "String",
        "dataType": "String",
        "defaultValue": null,
        "values": [
          "(0,5)",
          "(1,5)",
          "(2,5)",
          "(3,5)",
          "(4,5)",
          "(5,5)",
          "(6,5)",
          "(7,5)",
          "(8,5)",
          "(9,5)"
        ]
      },
      {
        "key": "Column-6",
        "keyType": "String",
        "dataType": "LocalDateTime",
        "defaultValue": null,
        "values": [
          "02-Aug-2014 21:24",
          "02-Aug-2014 21:25",
          "02-Aug-2014 21:26",
          "02-Aug-2014 21:27",
          "02-Aug-2014 21:28",
          "02-Aug-2014 21:29",
          "02-Aug-2014 21:30",
          "02-Aug-2014 21:31",
          "02-Aug-2014 21:32",
          "02-Aug-2014 21:33"
        ]
      }
    ]
  }
}
```

#### SQL

The third built-in sink that ships with the Morpheus library enables frames to be written out to a SQL database, with
support for [MYSQL](https://www.mysql.com/), [HSQLDB](http://hsqldb.org/), [SQLite](https://www.sqlite.org/), 
[H2 Database](http://www.h2database.com/html/main.html) and [Microsoft SQL Server](https://www.microsoft.com/en-us/sql-server).

The `DbSink` can be used to write a `DataFrame` to an existing table, or otherwise have a table automatically created if 
none exists. Obviously the latter scenario requires that the authenticated user has adequate permissions to create database
objects, otherwise an exception will be raised. The `DbSinkOptions` class allows some control for how the frame columns are 
mapped and stored in the database, both in terms of column names and column types.

Consider the code example below where we write the same 10x7 `DataFrame` presented in the introduction to an HSQLDB instance.
In this scenario, we pass the JDBC URL and credentials to the `DbSinkOptions`, however the connection can be passed via a
`java.sql.DataSource` or a `java.sql.Connection` object. For large frames, it is important to leverage SQL batch inserts, and
the batch size can be controlled via `DbSinkOptions`. Finally, the code below also demonstrates how to map the `DataFrame`
column keys to database column names, and also how to map types. While built in support exists for `LocalDate` and `Enum`
types, the example explicitly sets a custom mapping for illustration purposes.

<?prettify?>
```java
Class.forName("org.hsqldb.jdbcDriver");
frame.write().db(options -> {
    options.setBatchSize(1000);
    options.setTableName("TestTable");
    options.setConnection("jdbc:hsqldb:/Users/witdxav/morpheus/tests/DataFrame_3.db", "sa", null);
    options.setRowKeyMapping("Date", Date.class, Function1.toValue(Date::valueOf));
    options.setColumnMappings(mappings -> {
        mappings.add(Month.class, String.class, Function1.toValue(v -> {
            return v.<Month>getValue().name().toLowerCase();
        }));
        mappings.add(LocalDate.class, java.sql.Date.class, Function1.toValue(v -> {
            return Date.valueOf(v.<LocalDate>getValue());
        }));
    });
    options.setColumnNames(colKey -> {
        switch (colKey) {
            case "Column-1":    return "Column-A";
            case "Column-2":    return "Column-B";
            case "Column-3":    return "Column-C";
            default:            return colKey;
        }
    });
});
```


#### Custom Sink

Writing a custom sink is fairly trivial, and involves implementing two classes, namely an implementation of the `DataFrameSink`
interface, and a correponding options class that describes how the frame should be output. In this section, we show how one could
write a custom sink to record a `DataFrame` as a serialized object using standard java object serialization. The code below shows
an implementation of a `DataFrameSink` which has been called `CustomSink` for lack of a better name.

<?prettify?>
```java
public class CustomSink<R,C> implements DataFrameSink<R,C,CustomSinkOptions> {
    @Override
    public void write(DataFrame<R,C> frame, Consumer<CustomSinkOptions> configurator) {
        final CustomSinkOptions options = Initialiser.apply(new CustomSinkOptions(), configurator);
        ObjectOutputStream os = null;
        try {
            if (options.isCompressed()) {
                os = new ObjectOutputStream(new GZIPOutputStream(options.getOutput()));
                os.writeObject(frame);
            } else {
                os = new ObjectOutputStream(options.getOutput());
                os.writeObject(frame);
            }
        } catch (Exception ex) {
            throw new DataFrameException("Failed to write DataFrame to serialized output", ex);
        } finally {
            IO.close(os);
        }
    }
}
```

This class implements a single `write()` method which takes the frame to record, and a `Consumer` that is used to configure
the output options. The options class is usually sink specific, and in this example we have written a class called `CustomSinkOptions`
which carries the `OutputStream` to write to, and a flag to indicate whether output should be compressed. The options class
looks as follows.`

<?prettify?>
```java
public class CustomSinkOptions {

    private OutputStream output;
    private boolean compressed;

    /**
     * Returns the output stream to write to
     * @return      the output stream to write to
     */
    public OutputStream getOutput() {
        return output;
    }

    /**
     * Returns true if compression is enabled
     * @return  true if compression is enabled
     */
    public boolean isCompressed() {
        return compressed;
    }

    /**
     * Sets the output for these options
     * @param output    the output stream to write to
     */
    public void setOutput(OutputStream output) {
        this.output = output;
    }

    /**
     * Sets the file output for these options
     * @param file  the output file
     */
    public void setFile(File file) {
        this.output = Try.call(() -> new BufferedOutputStream(new FileOutputStream(file)));
    }

    /**
     * Sets whether the output should be wrapped in a GZIP stream
     * @param compressed    true to wrap output in GZIP compression
     */
    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }
}
```

To use this sink to write a frame, consider the following code using the same frame from the introduction. In this
case we create an instance of the sink to write to, and implement a `Consumer` to configure the sink specific options
as shown. which includes setting the output file and enabling GZIP compression.

<?prettify?>
```java
frame.write().to(new CustomSink<>(), options -> {
    options.setFile(new File("/Users/witdxav/test/DataFrame-1.gzip"));
    options.setCompressed(true);
});
```
