### Grouping

#### Introduction

The contents of a Morpheus `DataFrame` can be grouped in either the row or column dimension making it easy 
to aggregate and compute summary statistics on these groups. A group is nothing more than a `DataFrame` filter, 
so grouping even large frames has fairly modest memory requirements. Grouping functions can operate in
either sequential mode or parallel mode for improved performance on large frames. Finally, rows or columns 
can be grouped based on derived data, not just from data that exists in the frame. For example, if a `DataFrame` 
contains a column with dates, it is possible to group the data into months without adding an explicit column 
to capture the month associated with each date (see examples below).

#### Example Data

In this section, we will continue to use the ATP 2013 dataset that was introduced earlier in the filtering 
discussion [here](./filtering/#example-data). This dataset makes for an ideal grouping candidate given the large 
number of categorical items such as `Surface`, `Tournament`, `Round` and so on. As a reminder, here are the first 
10 rows of the 2013 ATP match `DataFrame`.

<div class="frame"><pre class="frame">
 Index  |  Location  |        Tournament        |     Date     |  Series  |   Court   |  Surface  |    Round    |  Best of  |     Winner     |     Loser      |  WRank  |  LRank  |  WPts  |  LPts  |  W1  |  L1  |  W2  |  L2  |  W3  |  L3  |  W4  |  L4  |  W5  |  L5  |  Wsets  |  Lsets  |   Comment   |  B365W   |  B365L   |   EXW    |   EXL    |   LBW    |   LBL    |   PSW    |   PSL    |   SJW    |   SJL    |   MaxW   |   MaxL   |   AvgW   |   AvgL   |
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     0  |  Brisbane  |  Brisbane International  |  2012-12-31  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |      Mayer F.  |    Giraldo S.  |     28  |     57  |  1215  |   778  |   6  |   4  |   6  |   4  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.3600  |  3.0000  |  1.4500  |  2.6500  |  1.4400  |  2.6200  |  1.4700  |  2.8500  |  1.4400  |  2.6300  |  1.4700  |  3.2000  |  1.4200  |  2.7800  |
     1  |  Brisbane  |  Brisbane International  |  2012-12-31  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |   Nieminen J.  |  Benneteau J.  |     41  |     35  |   927  |  1075  |   6  |   3  |   2  |   6  |   6  |   1  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |  1.6100  |  2.2000  |  1.7500  |  2.0000  |  1.8000  |  1.9100  |  1.8000  |  2.1000  |  1.7300  |  2.0000  |  1.8000  |  2.2600  |  1.7300  |  2.0500  |
     2  |  Brisbane  |  Brisbane International  |  2012-12-31  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |  Nishikori K.  |  Matosevic M.  |     19  |     49  |  1830  |   845  |   7  |   5  |   6  |   2  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.2500  |  3.7500  |  1.2500  |  3.7500  |  1.2900  |  3.5000  |  1.3000  |  3.8500  |  1.3000  |  3.2000  |  1.3000  |  4.2000  |  1.2800  |  3.5800  |
     3  |  Brisbane  |  Brisbane International  |  2012-12-31  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |  Baghdatis M.  |   Mitchell B.  |     36  |    326  |  1070  |   137  |   6  |   4  |   6  |   4  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.0700  |  9.0000  |  1.0600  |  8.0000  |  1.0800  |  7.0000  |  1.0800  |  9.4300  |  1.0700  |  7.0000  |  1.1000  |  9.5000  |  1.0800  |  7.7600  |
     4  |  Brisbane  |  Brisbane International  |  2013-01-01  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |    Istomin D.  |     Klizan M.  |     43  |     30  |   897  |  1175  |   6  |   1  |   6  |   2  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.9000  |  1.8000  |  1.8700  |  1.8700  |  1.9100  |  1.8000  |  1.8800  |  2.0000  |  1.9100  |  1.8000  |  2.0500  |  2.0000  |  1.8800  |  1.8500  |
     5  |  Brisbane  |  Brisbane International  |  2013-01-01  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |    Millman J.  |        Ito T.  |    199  |     79  |   239  |   655  |   6  |   4  |   6  |   1  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.6100  |  2.2000  |  1.7500  |  2.0000  |  1.7300  |  2.0000  |  1.7000  |  2.2700  |  1.8000  |  1.9100  |  1.8500  |  2.2800  |  1.7100  |  2.0800  |
     6  |  Brisbane  |  Brisbane International  |  2013-01-01  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |      Falla A.  |     Levine J.  |     54  |    104  |   809  |   530  |   6  |   1  |   7  |   6  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  2.2000  |  1.6100  |  2.0800  |  1.6700  |  1.9100  |  1.8000  |  2.2600  |  1.7000  |  2.0000  |  1.7300  |  2.3200  |  1.8300  |  2.0800  |  1.7000  |
     7  |  Brisbane  |  Brisbane International  |  2013-01-01  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |     Melzer J.  |      Kudla D.  |     29  |    137  |  1177  |   402  |   2  |   6  |   6  |   4  |   6  |   4  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |  1.4400  |  2.6200  |  1.5500  |  2.3500  |  1.4400  |  2.6200  |  1.6000  |  2.4700  |  1.5000  |  2.5000  |  1.6300  |  2.8200  |  1.5200  |  2.4600  |
     8  |  Brisbane  |  Brisbane International  |  2013-01-01  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |    Robredo T.  |   Harrison R.  |    114  |     69  |   495  |   710  |   6  |   4  |   7  |   6  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  3.0000  |  1.3600  |  2.5000  |  1.5000  |  2.3800  |  1.5300  |  2.9300  |  1.4500  |  2.5000  |  1.5000  |  3.2500  |  1.5300  |  2.6600  |  1.4700  |
     9  |  Brisbane  |  Brisbane International  |  2013-01-01  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |   Dimitrov G.  |      Baker B.  |     48  |     61  |   866  |   756  |   6  |   3  |   7  |   6  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.3600  |  3.0000  |  1.4000  |  2.8000  |  1.4400  |  2.6200  |  1.3800  |  3.3000  |  1.4000  |  2.7500  |  1.4500  |  3.5500  |  1.3900  |  2.8700  |Groups for depth 0...
</pre></div>

#### Grouping Rows

Two `groupBy()` methods exist on the `DataFrameAxis` interface which can be accessed by calling `DataFrame.rows()` as 
shown below. The first `groupBy()` function takes one or more column keys from which to extract data to generate the
relavant groups, and the second takes a lambda expression which must return a `Tuple` representing the group for the 
row presented to it. The following code groups the frame by `Surface` and then by `Round`:

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);
DataFrameGrouping.Rows<Integer,String> grouping = frame.rows().groupBy("Surface", "Round");
```

The same grouping can be achieved using the lambda `groupBy()` method, albeit more verbosely, as follows:

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);
DataFrameGrouping.Rows<Integer,String> grouping = frame.rows().groupBy(row -> {
    String surface = row.getValue("Surface");
    String round = row.getValue("Round");
    return Tuple.of(surface, round);
});
```

This second example demonstrates to some degree what is going on under the hood in the first example, where
a `Tuple` that is used to represent the group key is explicitly created in user code. This latter method is useful 
because it allows for an arbitrary group to be created based on data that may not be present in the `DataFrame`. 
Consider the scenario below where we group the ATP tournament data into months by extracting the month from the 
tournament date as follows:

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);
frame.rows().groupBy(row -> {
    LocalDate date = row.getValue("Date");
    Month month = date.getMonth();
    return Tuple.of(month);
}).forEach(0, (groupKey, group) -> {
    System.out.printf("There are %s rows for group %s\n", group.rowCount(), groupKey);
});
```

    There are 252 rows for group (JANUARY)
    There are 332 rows for group (FEBRUARY)
    There are 206 rows for group (MARCH)
    There are 207 rows for group (APRIL)
    There are 298 rows for group (MAY)
    There are 275 rows for group (JUNE)
    There are 303 rows for group (JULY)
    There are 286 rows for group (AUGUST)
    There are 136 rows for group (SEPTEMBER)
    There are 300 rows for group (OCTOBER)
    There are 22 rows for group (NOVEMBER)
    There are 14 rows for group (DECEMBER)

##### Grouping Depth

The `DataFrameGrouping` interface, an instance of which is returned from the `groupBy()` methods, provides a versatile
API to query and analyze the grouped data. The previous example demonstrates how to iterate over groups at a given
depth using a user provided lambda `BiConsumer`. The depth equals the number of dimensions in the grouping, so in the
examples where we perform a 2-dimensional group-by, the depth equals 2.  The Morpheus API allows groups at different
depths to be analyzed independently for maximum versatility. For example, consider a 3-dimensional group-by first by
`Court`, `Surface` and then `Round` as follows:

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);
DataFrameGrouping.Rows<Integer,String> grouping = frame.rows().groupBy("Court", "Surface", "Round");
for (int depth=0; depth<grouping.getDepth(); ++depth) {
    System.out.printf("Groups for depth %s...\n", depth);
    grouping.getGroupKeys(depth).sorted().forEach(groupKey -> {
        DataFrame<Integer,String> group = grouping.getGroup(groupKey);
        System.out.printf("There are %s rows for group %s\n", group.rowCount(), groupKey);
    });
}
```

    Groups for depth 0...
    There are 514 rows for group (Indoor)
    There are 2117 rows for group (Outdoor)
    Groups for depth 1...
    There are 27 rows for group (Indoor,Clay)
    There are 487 rows for group (Indoor,Hard)
    There are 826 rows for group (Outdoor,Clay)
    There are 298 rows for group (Outdoor,Grass)
    There are 993 rows for group (Outdoor,Hard)
    Groups for depth 2...
    There are 12 rows for group (Indoor,Clay,1st Round)
    There are 8 rows for group (Indoor,Clay,2nd Round)
    There are 4 rows for group (Indoor,Clay,Quarterfinals)
    There are 2 rows for group (Indoor,Clay,Semifinals)
    There are 1 rows for group (Indoor,Clay,The Final)
    There are 216 rows for group (Indoor,Hard,1st Round)
    There are 136 rows for group (Indoor,Hard,2nd Round)
    There are 8 rows for group (Indoor,Hard,3rd Round)
    There are 64 rows for group (Indoor,Hard,Quarterfinals)
    There are 12 rows for group (Indoor,Hard,Round Robin)
    There are 34 rows for group (Indoor,Hard,Semifinals)
    There are 17 rows for group (Indoor,Hard,The Final)
    There are 368 rows for group (Outdoor,Clay,1st Round)
    There are 240 rows for group (Outdoor,Clay,2nd Round)
    There are 56 rows for group (Outdoor,Clay,3rd Round)
    There are 8 rows for group (Outdoor,Clay,4th Round)
    There are 88 rows for group (Outdoor,Clay,Quarterfinals)
    There are 44 rows for group (Outdoor,Clay,Semifinals)
    There are 22 rows for group (Outdoor,Clay,The Final)
    There are 144 rows for group (Outdoor,Grass,1st Round)
    There are 80 rows for group (Outdoor,Grass,2nd Round)
    There are 24 rows for group (Outdoor,Grass,3rd Round)
    There are 8 rows for group (Outdoor,Grass,4th Round)
    There are 24 rows for group (Outdoor,Grass,Quarterfinals)
    There are 12 rows for group (Outdoor,Grass,Semifinals)
    There are 6 rows for group (Outdoor,Grass,The Final)
    There are 436 rows for group (Outdoor,Hard,1st Round)
    There are 288 rows for group (Outdoor,Hard,2nd Round)
    There are 104 rows for group (Outdoor,Hard,3rd Round)
    There are 32 rows for group (Outdoor,Hard,4th Round)
    There are 76 rows for group (Outdoor,Hard,Quarterfinals)
    There are 38 rows for group (Outdoor,Hard,Semifinals)
    There are 19 rows for group (Outdoor,Hard,The Final)

##### Summary Statistics

The primary motivation for grouping data is very often for aggregation purposes or for computing summary statistics
on the groups. The `stats()` method on the `DataFrameGrouping` interface takes the grouping depth to operate on and 
returns a standard Morpheus `Stats` reference that itself produces `DataFrames` of summary statistics. The stats frames 
will only include numeric columns on which stats can be calculated. The examples below illustrate the results of computing 
the `mean()` on groups at different depths. 

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);
DataFrameGrouping.Rows<Integer,String> grouping = frame.rows().groupBy("Court", "Surface", "Round");
//Computes means for top level groups
grouping.stats(0).mean().rows().sort(true).out().print(formats -> {
    formats.withDecimalFormat(Double.class, "0.00;-0.00", 1);
});
```
<div class="frame"><pre class="frame">
   Index    |  Best of  |  WRank  |  LRank  |   WPts    |   LPts    |   W1   |   L1   |   W2   |   L2   |   W3   |   L3   |   W4   |   L4   |   W5   |   L5   |  Wsets  |  Lsets  |  B365W  |  B365L  |  EXW   |  EXL   |  LBW   |  LBL   |  PSW   |  PSL   |  SJW   |  SJL   |  MaxW  |  MaxL  |  AvgW  |  AvgL  |
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  (Indoor)  |     3.00  |  55.80  |  89.98  |  1908.29  |  1163.28  |  5.79  |  4.14  |  5.67  |  3.91  |  2.16  |  1.19  |  0.00  |  0.00  |  0.00  |  0.00  |   1.94  |   0.36  |   1.90  |   3.26  |  1.86  |  2.95  |  1.86  |  2.94  |  1.99  |  3.39  |  1.88  |  2.99  |  2.09  |  3.66  |  1.90  |  3.07  |
 (Outdoor)  |     3.48  |  52.63  |  84.87  |  2147.69  |  1144.18  |  5.76  |  4.00  |  5.65  |  3.80  |  3.01  |  1.78  |  0.61  |  0.43  |  0.30  |  0.18  |   2.18  |   0.42  |   1.85  |   4.39  |  1.79  |  3.58  |  1.80  |  3.76  |  1.93  |  4.71  |  1.82  |  3.78  |  2.04  |  5.30  |  1.83  |  3.94  |
</pre></div>

Extending this to groups one level below the top level, so therefore with depth = 1:

<?prettify?>
```java
//Computes means for second level, with depth = 1
grouping.stats(1).mean().rows().sort(true).out().print(formats -> {
    formats.withDecimalFormat(Double.class, "0.00;-0.00", 1);
});
```
<div class="frame"><pre class="frame">
      Index       |  Best of  |  WRank  |  LRank  |   WPts    |   LPts    |   W1   |   L1   |   W2   |   L2   |   W3   |   L3   |   W4   |   L4   |   W5   |   L5   |  Wsets  |  Lsets  |  B365W  |  B365L  |  EXW   |  EXL   |  LBW   |  LBL   |  PSW   |  PSL   |  SJW   |  SJL   |  MaxW  |  MaxL  |  AvgW  |  AvgL  |
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
   (Indoor,Clay)  |     3.00  |  74.67  |  91.37  |  1411.93  |   785.89  |  6.22  |  4.30  |  5.37  |  4.33  |  2.26  |  1.30  |  0.00  |  0.00  |  0.00  |  0.00  |   1.93  |   0.37  |   1.96  |   3.47  |  1.93  |  3.16  |  1.91  |  3.14  |  2.10  |  3.65  |  1.89  |  3.13  |  2.13  |  3.93  |  1.94  |  3.28  |
   (Indoor,Hard)  |     3.00  |  54.76  |  89.91  |  1935.81  |  1184.21  |  5.77  |  4.13  |  5.69  |  3.89  |  2.16  |  1.18  |  0.00  |  0.00  |  0.00  |  0.00  |   1.94  |   0.36  |   1.90  |   3.25  |  1.86  |  2.94  |  1.85  |  2.93  |  1.99  |  3.37  |  1.88  |  2.98  |  2.09  |  3.65  |  1.90  |  3.06  |
  (Outdoor,Clay)  |     3.31  |  55.96  |  92.25  |  1824.49  |  1091.88  |  5.73  |  3.88  |  5.62  |  3.75  |  2.74  |  1.51  |  0.35  |  0.26  |  0.20  |  0.13  |   2.10  |   0.40  |   1.84  |   4.15  |  1.80  |  3.48  |  1.80  |  3.58  |  1.91  |  4.37  |  1.82  |  3.58  |  2.02  |  4.86  |  1.83  |  3.75  |
 (Outdoor,Grass)  |     3.85  |  63.94  |  95.93  |  1898.03  |  1016.78  |  5.85  |  4.08  |  5.85  |  3.96  |  3.62  |  2.39  |  0.88  |  0.58  |  0.37  |  0.22  |   2.35  |   0.40  |   1.98  |   4.36  |  1.89  |  3.72  |  1.93  |  3.80  |  2.11  |  4.73  |  1.89  |  3.74  |  2.31  |  5.34  |  1.93  |  3.97  |
  (Outdoor,Hard)  |     3.51  |  46.47  |  75.41  |  2491.45  |  1225.92  |  5.76  |  4.08  |  5.61  |  3.81  |  3.05  |  1.83  |  0.74  |  0.52  |  0.37  |  0.21  |   2.19  |   0.45  |   1.81  |   4.61  |  1.76  |  3.62  |  1.77  |  3.90  |  1.89  |  4.98  |  1.80  |  3.95  |  1.98  |  5.65  |  1.80  |  4.08  |
</pre></div>

Finally the most granular grouping can be accessed with depth = 2:

<?prettify?>
```java
//Computes means for third level, with depth = 2
grouping.stats(2).mean().rows().sort(true).out().print(formats -> {
    formats.withDecimalFormat(Double.class, "0.00;-0.00", 1);
});

```
<div class="frame"><pre class="frame">
            Index             |  Best of  |  WRank  |  LRank   |   WPts    |   LPts    |   W1   |   L1   |   W2   |   L2   |   W3   |   L3   |   W4   |   L4   |   W5   |   L5   |  Wsets  |  Lsets  |  B365W  |  B365L  |  EXW   |  EXL   |  LBW   |  LBL   |  PSW   |  PSL   |  SJW   |  SJL   |  MaxW  |  MaxL  |  AvgW  |  AvgL  |
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     (Indoor,Clay,1st Round)  |     3.00  |  88.50  |  112.83  |   630.58  |   613.67  |  6.33  |  4.42  |  4.83  |  4.42  |  2.42  |  1.33  |  0.00  |  0.00  |  0.00  |  0.00  |   1.83  |   0.42  |   1.95  |   2.55  |  1.92  |  2.55  |  1.89  |  2.46  |  2.04  |  2.69  |  1.89  |  2.43  |  2.05  |  2.93  |  1.90  |  2.58  |
     (Indoor,Clay,2nd Round)  |     3.00  |  70.25  |   69.50  |  1458.88  |   966.75  |  6.38  |  4.25  |  5.75  |  3.62  |  1.63  |  1.00  |  0.00  |  0.00  |  0.00  |  0.00  |   2.00  |   0.25  |   2.09  |   4.22  |  2.05  |  3.48  |  2.04  |  3.38  |  2.33  |  4.38  |  1.98  |  3.05  |  2.38  |  4.69  |  2.09  |  3.76  |
 (Indoor,Clay,Quarterfinals)  |     3.00  |  72.25  |   68.25  |  1808.50  |  1109.25  |  5.75  |  5.25  |  5.50  |  5.00  |  3.25  |  2.50  |  0.00  |  0.00  |  0.00  |  0.00  |   2.00  |   0.50  |   2.22  |   3.48  |  2.13  |  3.23  |  2.15  |  3.50  |  2.33  |  3.26  |  2.12  |  3.49  |  2.37  |  3.70  |  2.17  |  3.24  |
    (Indoor,Clay,Semifinals)  |     3.00  |  49.00  |   95.50  |  3050.00  |   567.00  |  6.00  |  3.00  |  6.50  |  6.00  |  3.00  |  0.50  |  0.00  |  0.00  |  0.00  |  0.00  |   2.00  |   0.50  |   1.28  |   6.25  |  1.31  |  5.18  |  1.29  |  5.44  |  1.35  |  7.23  |  1.28  |  6.75  |  1.35  |  7.82  |  1.30  |  5.94  |
     (Indoor,Clay,The Final)  |     3.00  |   5.00  |   93.00  |  5550.00  |   550.00  |  6.00  |  2.00  |  6.00  |  3.00  |  0.00  |  0.00  |  0.00  |  0.00  |  0.00  |  0.00  |   2.00  |   0.00  |   1.44  |   2.75  |  1.38  |  2.90  |  1.40  |  2.75  |  1.52  |  2.78  |  1.44  |  2.75  |  1.57  |  2.90  |  1.44  |  2.73  |
     (Indoor,Hard,1st Round)  |     3.00  |  78.47  |  119.85  |   983.54  |   791.82  |  5.74  |  4.19  |  5.80  |  3.74  |  1.89  |  1.06  |  0.00  |  0.00  |  0.00  |  0.00  |   1.92  |   0.32  |   2.06  |   3.05  |  2.00  |  2.72  |  1.97  |  2.70  |  2.16  |  3.15  |  2.01  |  2.73  |  2.30  |  3.38  |  2.07  |  2.85  |
     (Indoor,Hard,2nd Round)  |     3.00  |  47.49  |   85.58  |  1933.18  |   876.24  |  5.86  |  3.90  |  5.66  |  4.09  |  2.14  |  1.20  |  0.00  |  0.00  |  0.00  |  0.00  |   1.96  |   0.35  |   1.76  |   3.51  |  1.75  |  3.18  |  1.75  |  3.16  |  1.83  |  3.58  |  1.76  |  3.26  |  1.91  |  4.02  |  1.76  |  3.31  |
     (Indoor,Hard,3rd Round)  |     3.00  |   5.25  |   16.88  |  6193.75  |  2036.25  |  5.88  |  4.38  |  6.00  |  2.88  |  1.50  |  0.75  |  0.00  |  0.00  |  0.00  |  0.00  |   2.00  |   0.25  |   1.37  |   3.57  |  1.41  |  3.32  |  1.42  |  3.38  |  1.44  |  3.85  |  1.42  |  3.28  |  1.47  |  4.00  |  1.41  |  3.37  |
 (Indoor,Hard,Quarterfinals)  |     3.00  |  32.70  |   66.11  |  2366.33  |  1487.16  |  5.87  |  4.25  |  5.44  |  3.89  |  2.41  |  1.28  |  0.00  |  0.00  |  0.00  |  0.00  |   1.92  |   0.39  |   1.89  |   3.23  |  1.81  |  2.99  |  1.84  |  2.95  |  1.98  |  3.49  |  1.87  |  3.01  |  2.09  |  3.67  |  1.87  |  3.05  |
   (Indoor,Hard,Round Robin)  |     3.00  |   4.17  |    6.08  |  7602.08  |  4375.42  |  6.00  |  4.75  |  5.33  |  5.00  |  4.17  |  2.08  |  0.00  |  0.00  |  0.00  |  0.00  |   2.00  |   0.67  |   1.44  |   4.03  |  1.44  |  3.75  |  1.44  |  3.98  |  1.49  |  4.08  |  1.45  |  3.87  |  1.53  |  4.30  |  1.45  |  3.81  |
</pre></div>


#### Grouping Columns

Grouping columns in a `DataFrame` is entirely analogous to grouping rows as the API is completely symmetrical in the
row and column dimension. Instead of operating on the `DataFrameAxis` returned by a call to `DataFrame.rows()`, you operate 
on the same interface returned from `DataFrame.cols()`. The ATP match results served as a good candidate for grouping rows, 
but is not appropriate for grouping columns as there are few if any repetitions within a given row. Rather than introduce 
a new dataset, we can simply transpose the ATP dataset and then group the columns.

<?prettify?>
```java
//Transpose the ATP match data, select 20 left columns, print first ten rows
loadTennisMatchData(2013).transpose().left(20).out().print(10);
```

<div class="frame"><pre class="frame">
   Index     |            0             |            1             |            2             |            3             |            4             |            5             |            6             |            7             |            8             |            9             |            10            |            11            |            12            |            13            |            14            |            15            |            16            |            17            |            18            |            19            |
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
   Location  |                Brisbane  |                Brisbane  |                Brisbane  |                Brisbane  |                Brisbane  |                Brisbane  |                Brisbane  |                Brisbane  |                Brisbane  |                Brisbane  |                Brisbane  |                Brisbane  |                Brisbane  |                Brisbane  |                Brisbane  |                Brisbane  |                Brisbane  |                Brisbane  |                Brisbane  |                Brisbane  |
 Tournament  |  Brisbane International  |  Brisbane International  |  Brisbane International  |  Brisbane International  |  Brisbane International  |  Brisbane International  |  Brisbane International  |  Brisbane International  |  Brisbane International  |  Brisbane International  |  Brisbane International  |  Brisbane International  |  Brisbane International  |  Brisbane International  |  Brisbane International  |  Brisbane International  |  Brisbane International  |  Brisbane International  |  Brisbane International  |  Brisbane International  |
       Date  |              2012-12-31  |              2012-12-31  |              2012-12-31  |              2012-12-31  |              2013-01-01  |              2013-01-01  |              2013-01-01  |              2013-01-01  |              2013-01-01  |              2013-01-01  |              2013-01-01  |              2013-01-01  |              2013-01-02  |              2013-01-02  |              2013-01-02  |              2013-01-02  |              2013-01-03  |              2013-01-03  |              2013-01-03  |              2013-01-03  |
     Series  |                  ATP250  |                  ATP250  |                  ATP250  |                  ATP250  |                  ATP250  |                  ATP250  |                  ATP250  |                  ATP250  |                  ATP250  |                  ATP250  |                  ATP250  |                  ATP250  |                  ATP250  |                  ATP250  |                  ATP250  |                  ATP250  |                  ATP250  |                  ATP250  |                  ATP250  |                  ATP250  |
      Court  |                 Outdoor  |                 Outdoor  |                 Outdoor  |                 Outdoor  |                 Outdoor  |                 Outdoor  |                 Outdoor  |                 Outdoor  |                 Outdoor  |                 Outdoor  |                 Outdoor  |                 Outdoor  |                 Outdoor  |                 Outdoor  |                 Outdoor  |                 Outdoor  |                 Outdoor  |                 Outdoor  |                 Outdoor  |                 Outdoor  |
    Surface  |                    Hard  |                    Hard  |                    Hard  |                    Hard  |                    Hard  |                    Hard  |                    Hard  |                    Hard  |                    Hard  |                    Hard  |                    Hard  |                    Hard  |                    Hard  |                    Hard  |                    Hard  |                    Hard  |                    Hard  |                    Hard  |                    Hard  |                    Hard  |
      Round  |               1st Round  |               1st Round  |               1st Round  |               1st Round  |               1st Round  |               1st Round  |               1st Round  |               1st Round  |               1st Round  |               1st Round  |               1st Round  |               1st Round  |               2nd Round  |               2nd Round  |               2nd Round  |               2nd Round  |               2nd Round  |               2nd Round  |               2nd Round  |               2nd Round  |
    Best of  |                       3  |                       3  |                       3  |                       3  |                       3  |                       3  |                       3  |                       3  |                       3  |                       3  |                       3  |                       3  |                       3  |                       3  |                       3  |                       3  |                       3  |                       3  |                       3  |                       3  |
     Winner  |                Mayer F.  |             Nieminen J.  |            Nishikori K.  |            Baghdatis M.  |              Istomin D.  |              Millman J.  |                Falla A.  |               Melzer J.  |              Robredo T.  |             Dimitrov G.  |               Goffin D.  |               Hewitt L.  |                Simon G.  |            Baghdatis M.  |            Nishikori K.  |           Dolgopolov O.  |             Dimitrov G.  |               Melzer J.  |              Istomin D.  |               Murray A.  |
      Loser  |              Giraldo S.  |            Benneteau J.  |            Matosevic M.  |             Mitchell B.  |               Klizan M.  |                  Ito T.  |               Levine J.  |                Kudla D.  |             Harrison R.  |                Baker B.  |                Ebden M.  |             Kunitsyn I.  |                Falla A.  |                Mayer F.  |              Robredo T.  |             Nieminen J.  |               Raonic M.  |               Goffin D.  |               Hewitt L.  |              Millman J.  |Groups for depth 0...
</pre></div>

With the transposed ATP match results `DataFrame`, we can now group the columns in much the same way as we grouped the 
rows. The code below performs a 2-dimensional column grouping based on data in the rows identified by `Court` and `Surface`,
and subsequently prints out the number of columns per group at level 0 and 1.

<?prettify?>
```java
DataFrame<String,Integer> frame = loadTennisMatchData(2013).transpose();
DataFrameGrouping.Cols<String,Integer> grouping = frame.cols().groupBy("Court", "Surface");
for (int depth=0; depth<grouping.getDepth(); ++depth) {
    System.out.printf("Groups for depth %s...\n", depth);
    grouping.getGroupKeys(depth).sorted().forEach(groupKey -> {
        DataFrame<String,Integer> group = grouping.getGroup(groupKey);
        System.out.printf("There are %s columns for group %s\n", group.colCount(), groupKey);
    });
}
```

    Groups for depth 0...
    There are 514 columns for group (Indoor)
    There are 2117 columns for group (Outdoor)
    Groups for depth 1...
    There are 27 columns for group (Indoor,Clay)
    There are 487 columns for group (Indoor,Hard)
    There are 826 columns for group (Outdoor,Clay)
    There are 298 columns for group (Outdoor,Grass)
    There are 993 columns for group (Outdoor,Hard)

To avoid repetition, further column grouping examples are omitted as they are identical to the row grouping scenarios 
illustrated in prior sections. The next section presents and discusses some data on grouping performance of large 
`DataFrames`.

#### Performance

The performance of the group-by functions on a `DataFrame` will vary depending on many factors, including 
but not limited to the number of resulting groups, the depth of the grouping and the cost of extracting the 
relevant data from rows or columns. As with many `DataFrame` operations, a **parallel** implementation of grouping 
can be used to speed up scenarios which involve a large number of records and/or ones in which the code to 
assemble a group is somewhat costly. This section provides examples of how sequential and parallel grouping 
performance might be expected to compare in the most common scenarios. The absolute times published below are 
obviously vary machine specific, and in this case relate to a Macbook Pro with Quad Core-i7 processor.

##### Example Data

The ATP match data only holds a few thousand records per year which is a little small to gather reasonable group-by 
performance statistics against (the times will be small and thus sensitive to measurement noise). We will therefore 
use the the UK house price transaction data made available from the UK Land Registry as introduced in the Morpeus 
Overview section. This dataset, which contains approximately 1.35 records in 2006, can be loaded using the code below. 
While the raw data is in CSV format, it does not include a header as the first row, so for convenience we map the 
default numbered columns to more meaningful names.

<?prettify?>
```java
/**
 * Returns a DataFrame of UK house prices from the Land Registry of the UK
 * @param year      the year to load prices for
 * @return          the house price DataFrame
 */
static DataFrame<Integer,String> loadHousePrices(int year) {
    return DataFrame.read().csv(options -> {
        options.setHeader(false);
        options.setParallel(true);
        options.setExcludeColumns("Column-0");
        options.setResource("/uk-house-prices-" + year + ".csv");
        options.getFormats().setDateFormat("Date", "yyyy-MM-dd HH:mm");
        options.setColumnNameMapping((colName, colOrdinal) -> {
            switch (colOrdinal) {
                case 0:  return "Price";
                case 1:  return "Date";
                case 2:  return "PostCode";
                case 3:  return "PropertyType";
                case 4:  return "Old/New";
                case 5:  return "Duration";
                case 6:  return "PAON";
                case 7:  return "SAON";
                case 8:  return "Street";
                case 9:  return "Locality";
                case 10: return "Town/City";
                case 11: return "District";
                case 12: return "County";
                case 13: return "PPDType";
                case 14: return "RecordStatus";
                default: return colName;
            }
        });
    });
}
```
<div class="frame"><pre class="frame">
 Index  |  Price   |         Date          |  PostCode  |  PropertyType  |  Old/New  |  Duration  |  PAON  |  SAON  |        Street        |   Locality   |  Town/City   |       District       |        County        |  PPDType  |  RecordStatus  |
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     0  |  180000  |  2006-06-30T00:00:00  |   PR2 3SP  |             S  |        N  |         F  |    30  |  null  |    CLEVELEYS AVENUE  |     FULWOOD  |     PRESTON  |             PRESTON  |          LANCASHIRE  |        A  |             A  |
     1  |   99750  |  2006-09-15T00:00:00  |  EX23 8TA  |             F  |        N  |         L  |    25  |  null  |         BULLEID WAY  |        BUDE  |        BUDE  |      NORTH CORNWALL  |            CORNWALL  |        A  |             A  |
     2  |   65000  |  2006-10-06T00:00:00  |  BB12 7ER  |             T  |        N  |         L  |    70  |  null  |  SHAKESPEARE STREET  |     PADIHAM  |     BURNLEY  |             BURNLEY  |          LANCASHIRE  |        A  |             A  |
     3  |  120000  |  2006-02-13T00:00:00  |   LE2 1PB  |             T  |        N  |         F  |    35  |  null  |       CHEPSTOW ROAD  |   LEICESTER  |   LEICESTER  |           LEICESTER  |           LEICESTER  |        A  |             A  |
     4  |  112000  |  2006-04-07T00:00:00  |  CF38 2JF  |             T  |        N  |         F  |   127  |  null  |         MANOR CHASE  |      BEDDAU  |  PONTYPRIDD  |  RHONDDA CYNON TAFF  |  RHONDDA CYNON TAFF  |        A  |             A  |
     5  |   84950  |  2006-02-24T00:00:00  |   CV6 4AS  |             T  |        N  |         F  |   381  |  null  |        BURNABY ROAD  |        null  |    COVENTRY  |            COVENTRY  |       WEST MIDLANDS  |        A  |             A  |
     6  |  124950  |  2006-10-06T00:00:00  |  ST20 0HZ  |             S  |        N  |         F  |    38  |  null  |     ST LAWRENCE WAY  |     GNOSALL  |    STAFFORD  |            STAFFORD  |       STAFFORDSHIRE  |        A  |             A  |
     7  |   30000  |  2006-11-03T00:00:00  |    M8 0QL  |             S  |        Y  |         F  |    32  |  null  |          CHIME BANK  |  MANCHESTER  |  MANCHESTER  |          MANCHESTER  |  GREATER MANCHESTER  |        A  |             A  |
     8  |  380000  |  2006-09-25T00:00:00  |    N7 9SY  |             F  |        N  |         F  |     8  |  null  |    HEDDINGTON GROVE  |      LONDON  |      LONDON  |           ISLINGTON  |      GREATER LONDON  |        A  |             A  |
     9  |  451000  |  2006-02-08T00:00:00  |   SW9 9AL  |             F  |        Y  |         L  |    29  |  null  |         STANE GROVE  |        null  |      LONDON  |             LAMBETH  |      GREATER LONDON  |        A  |             A  |
</pre></div>

##### Group Count

The first example attempts to demonstrate the performance impact of an increasing number of groups created by a the 
group-by operation. In this case we group the rows of the UK property transaction data for 2006 by 5 different columns 
independently, namely `PropertyType`, `County`, `Month`, `District`, `Town/City` and `Locality`. The group count for these 
columns is listed below. The expectation is that execution times will increase as the number of groups generated by a group-by 
function increases, which is exactly what we see in the subsequent plot.

* **Property Type**: 5 Groups
* **Month**: 12 Groups
* **County**: 115 Groups
* **District**: 387 Groups
* **Town/City**: 1161 Groups
* **Locality**: 16800 Groups

<p align="center">
    <img class="chart" src="../../images/frame/data-frame-group-by-0.png"/>
</p>
Running this same example with **parallel execution** yields a similar trend, but the absolute times are much smaller.

<p align="center">
    <img class="chart" src="../../images/frame/data-frame-group-by-1.png"/>
</p>
The following code is used to generate the above results, for the sequential execution.

<?prettify?>
```java
//Load UK house prices for 2006
DataFrame<Integer,String> frame = loadHousePrices(2006);

//Run 10 iterations of sequential and parallel group by Town/City
DataFrame<String,String> results = PerfStat.run(5, TimeUnit.MILLISECONDS, false, tasks -> {
    tasks.put("PropertyType", () -> frame.rows().groupBy("PropertyType"));
    tasks.put("Month", () -> frame.rows().groupBy(row -> {
        final LocalDateTime date = row.getValue("Date");
        return Tuple.of(date.getMonth());
    }));
    tasks.put("County", () -> frame.rows().groupBy("County"));
    tasks.put("District", () -> frame.rows().groupBy("District"));
    tasks.put("Town/City", () -> frame.rows().groupBy("Town/City"));
    tasks.put("Locality", () -> frame.rows().groupBy("Locality"));
});

//Plot the results of the combined DataFrame with timings
Chart.create().withBarPlot(results, false, chart -> {
    chart.plot().axes().domain().label().withText("Timing Statistic");
    chart.plot().axes().range(0).label().withText("Time In Milliseconds");
    chart.title().withText("1-Dimensional Grouping of 1.35 Million Rows (Sequential)");
    chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
    chart.subtitle().withText("Grouping of UK House Price Transactions in 2006 by various columns");
    chart.legend().on().bottom();
    chart.show();
});
```
##### Sequential vs Parallel

The following example plots the results of a standard 1-dimensional group-by operation on the same chart. The 1.35 
million rows in the UK house price dataset is grouped by `County`, which results in 115 distinct groups as indicated
in the previous section. The timing statistics below seem to indicate the parallel grouping in this scenario was a little
over twice as fast as the sequential algorithm. This result bodes well as this is the simplest possible group-by scenatio, 
and we would expect the parallel algorithm to perform even better as the grouping complexity increases.

<p align="center">
    <img class="chart" src="../../images/frame/data-frame-group-by-2.png"/>
</p>
The following code is used to generate the above results.

<?prettify?>
```java
//Load UK house prices for 2006
DataFrame<Integer,String> frame = loadHousePrices(2006);

//Run 10 iterations of sequential and parallel group by County
DataFrame<String,String> results = PerfStat.run(10, TimeUnit.MILLISECONDS, false, tasks -> {
    tasks.put("Sequential", () -> frame.rows().sequential().groupBy("County"));
    tasks.put("Parallel", () -> frame.rows().parallel().groupBy("County"));
});

//Plot the results of the combined DataFrame with timings
Chart.create().withBarPlot(results, false, chart -> {
    chart.plot().axes().domain().label().withText("Timing Statistic");
    chart.plot().axes().range(0).label().withText("Time In Milliseconds");
    chart.title().withText("1-Dimensional Grouping of 1.35 Million Rows");
    chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
    chart.subtitle().withText("Grouping of UK House Price Transactions in 2006 by County");
    chart.legend().on().bottom();
    chart.show();
});
```

##### Multi-Dimensional

The prior examples use 1-dimensional grouping only, so this section looks at the cost of a multi-dimensioal
group-by on the rows of the UK property transaction data. In order to remove group count as a factor in the second
dimension, we simply group by the same column twice. This is not particularly useful in reality other than to assess 
the relative cost of adding the second dimension. Naturally, the group count in the second dimension will always
be 1.

The plot below indicates adding another dimension is significant, although using parallel execution can mostly
discount the additional cost, at least in this scenario.

<p align="center">
    <img class="chart" src="../../images/frame/data-frame-group-by-3.png"/>
</p>
The following code is used to generate the above results.

<?prettify?>
```java
//Load UK house prices for 2006
DataFrame<Integer,String> frame = loadHousePrices(2006);

//Run 10 iterations of sequential and parallel group by County and Town/City
DataFrame<String,String> results = PerfStat.run(10, TimeUnit.MILLISECONDS, false, tasks -> {
    tasks.put("Sequential(1-D)", () -> frame.rows().sequential().groupBy("County"));
    tasks.put("Parallel(1-D)", () -> frame.rows().parallel().groupBy("County"));
    tasks.put("Sequential(2-D)", () -> frame.rows().sequential().groupBy("County", "County"));
    tasks.put("Parallel(2-D)", () -> frame.rows().parallel().groupBy("County", "County"));
});

//Plot the results of the combined DataFrame with timings
Chart.create().withBarPlot(results, false, chart -> {
    chart.plot().axes().domain().label().withText("Timing Statistic");
    chart.plot().axes().range(0).label().withText("Time In Milliseconds");
    chart.title().withText("1-Dimension vs 2-Dimensional Grouping of 1.35 Million Rows");
    chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
    chart.subtitle().withText("Grouping of UK House Price Transactions in 2006 by County x 2");
    chart.legend().on().bottom();
    chart.show();
});
```

##### Grouping Function

The Morpheus API provides two styles of grouping function, the first simply takes the column keys or row
keys depending if you are grouping rows or columns respectively. The second style uses a lambda expression
that consumes either the row or column, and returns a `Tuple` that represents the group for that entry. The
latter is more flexible in that it can produce a group that is based on some derived calculation on the
row or column vector it is presented with. This section assess whether there is any perfomance difference
to using either of these approaches.

In this example we perform a two-dimensional group by using the `County` first, and the `Town/City` second.
The results in the subsequent plot suggests there is a very small performance cost to using the second style,
however it is fairly neglible in this case.

<p align="center">
    <img class="chart" src="../../images/frame/data-frame-group-by-4.png"/>
</p>
The following code is used to generate the above results.

<?prettify?>
```java
//Load UK house prices for 2006
DataFrame<Integer,String> frame = loadHousePrices(2006);

//Run 10 iterations of sequential and parallel group by County and Town/City
DataFrame<String,String> results = PerfStat.run(5, TimeUnit.MILLISECONDS, false, tasks -> {
    tasks.put("Method-1", () -> frame.rows().groupBy("County", "Town/City"));
    tasks.put("Method-2", () -> frame.rows().groupBy(row -> Tuple.of(
        row.<String>getValue("County"),
        row.<String>getValue("Town/City"))
    ));
});

//Plot the results of the combined DataFrame with timings
Chart.create().withBarPlot(results, false, chart -> {
    chart.plot().axes().domain().label().withText("Timing Statistic");
    chart.plot().axes().range(0).label().withText("Time In Milliseconds");
    chart.title().withText("2-Dimensional Grouping of 1.35 Million Rows");
    chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
    chart.subtitle().withText("Grouping of UK House Price Transactions in 2006 by County & Town/City");
    chart.legend().on().bottom();
    chart.show();
});
```