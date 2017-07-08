### Filtering

#### Introduction

Filtering a `DataFrame` along the row and/or column dimensions is a common requirement in order to easily 
analyze or operate on a subset of the data.  Applying a filter is a fairly cheap operation because the resulting 
frame is merely a projection onto its parent, as opposed to being a deep copy. This implies that very little 
additional memory is required to create filters or slices, and it also means that any data modifications to the 
filter are in effect applied to the parent. Of course, if a detached copy of the data subset is required, simply 
calling `DataFrame.copy()` on the filter frame will yield the desired result. 

It should also be noted that while the contents of a filter frame can be modified, the structure of the filter is 
immutable in the row dimension, but can be expanded in the column dimension (see section below titled Immutability 
that explains these restrictions in more detail). It is of course permissible to further filter an already filtered 
frame.

#### Example Data

There is a large amount of professional tennis match data available [here](http://www.tennis-data.co.uk/) for both 
the men's (ATP) and women's (WTA) tours. The code examples in this section operate on a dataset containing all match 
results for the 2013 ATP tour, which can be loaded using the code below. The dataset contains 2631 rows and 41 
columns, including player statistics, match play statistics, as well as betting odds from various bookies in the UK. 
More information on the exact content of this file can be located [here](http://www.tennis-data.co.uk/notes.txt)

<?prettify?>
```java
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
```

<div class="frame"><pre class="frame">
Index  |  Location  |        Tournament        |     Date     |  Series  |   Court   |  Surface  |    Round    |  Best of  |     Winner     |     Loser      |  WRank  |  LRank  |  WPts  |  LPts  |  W1  |  L1  |  W2  |  L2  |  W3  |  L3  |  W4  |  L4  |  W5  |  L5  |  Wsets  |  Lsets  |   Comment   |  B365W  |  B365L  |  EXW   |  EXL   |  LBW   |  LBL   |  PSW   |  PSL   |  SJW   |  SJL   |  MaxW  |  MaxL  |  AvgW  |  AvgL  |
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    0  |  Brisbane  |  Brisbane International  |  2012-12-31  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |      Mayer F.  |    Giraldo S.  |     28  |     57  |  1215  |   778  |   6  |   4  |   6  |   4  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |   1.36  |    3.0  |  1.45  |  2.65  |  1.44  |  2.62  |  1.47  |  2.85  |  1.44  |  2.63  |  1.47  |   3.2  |  1.42  |  2.78  |
    1  |  Brisbane  |  Brisbane International  |  2012-12-31  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |   Nieminen J.  |  Benneteau J.  |     41  |     35  |   927  |  1075  |   6  |   3  |   2  |   6  |   6  |   1  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |   1.61  |    2.2  |  1.75  |   2.0  |   1.8  |  1.91  |   1.8  |   2.1  |  1.73  |   2.0  |   1.8  |  2.26  |  1.73  |  2.05  |
    2  |  Brisbane  |  Brisbane International  |  2012-12-31  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |  Nishikori K.  |  Matosevic M.  |     19  |     49  |  1830  |   845  |   7  |   5  |   6  |   2  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |   1.25  |   3.75  |  1.25  |  3.75  |  1.29  |   3.5  |   1.3  |  3.85  |   1.3  |   3.2  |   1.3  |   4.2  |  1.28  |  3.58  |
    3  |  Brisbane  |  Brisbane International  |  2012-12-31  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |  Baghdatis M.  |   Mitchell B.  |     36  |    326  |  1070  |   137  |   6  |   4  |   6  |   4  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |   1.07  |    9.0  |  1.06  |   8.0  |  1.08  |   7.0  |  1.08  |  9.43  |  1.07  |   7.0  |   1.1  |   9.5  |  1.08  |  7.76  |
    4  |  Brisbane  |  Brisbane International  |  2013-01-01  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |    Istomin D.  |     Klizan M.  |     43  |     30  |   897  |  1175  |   6  |   1  |   6  |   2  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |    1.9  |    1.8  |  1.87  |  1.87  |  1.91  |   1.8  |  1.88  |   2.0  |  1.91  |   1.8  |  2.05  |   2.0  |  1.88  |  1.85  |
    5  |  Brisbane  |  Brisbane International  |  2013-01-01  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |    Millman J.  |        Ito T.  |    199  |     79  |   239  |   655  |   6  |   4  |   6  |   1  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |   1.61  |    2.2  |  1.75  |   2.0  |  1.73  |   2.0  |   1.7  |  2.27  |   1.8  |  1.91  |  1.85  |  2.28  |  1.71  |  2.08  |
    6  |  Brisbane  |  Brisbane International  |  2013-01-01  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |      Falla A.  |     Levine J.  |     54  |    104  |   809  |   530  |   6  |   1  |   7  |   6  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |    2.2  |   1.61  |  2.08  |  1.67  |  1.91  |   1.8  |  2.26  |   1.7  |   2.0  |  1.73  |  2.32  |  1.83  |  2.08  |   1.7  |
    7  |  Brisbane  |  Brisbane International  |  2013-01-01  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |     Melzer J.  |      Kudla D.  |     29  |    137  |  1177  |   402  |   2  |   6  |   6  |   4  |   6  |   4  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |   1.44  |   2.62  |  1.55  |  2.35  |  1.44  |  2.62  |   1.6  |  2.47  |   1.5  |   2.5  |  1.63  |  2.82  |  1.52  |  2.46  |
    8  |  Brisbane  |  Brisbane International  |  2013-01-01  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |    Robredo T.  |   Harrison R.  |    114  |     69  |   495  |   710  |   6  |   4  |   7  |   6  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |    3.0  |   1.36  |   2.5  |   1.5  |  2.38  |  1.53  |  2.93  |  1.45  |   2.5  |   1.5  |  3.25  |  1.53  |  2.66  |  1.47  |
    9  |  Brisbane  |  Brisbane International  |  2013-01-01  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |   Dimitrov G.  |      Baker B.  |     48  |     61  |   866  |   756  |   6  |   3  |   7  |   6  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |   1.36  |    3.0  |   1.4  |   2.8  |  1.44  |  2.62  |  1.38  |   3.3  |   1.4  |  2.75  |  1.45  |  3.55  |  1.39  |  2.87  |
</pre></div>

Initializing this frame from CSV is fairly straightforward as most of the data can be coerced into the appropriate 
type without any intervention, except for the Date column which is expressed in dd/MM/yy format and therefore requires
a custom parser. We also exclude a number of columns that are of little interest.

#### Select Rows

Filtering along the row dimension of the ATP match results can be achieved by calling one of the over-loaded `select()` 
methods available on the row dimension operator, which is accessed with a call to `DataFrame.rows()`. The `select()` 
methods either take the set of row keys to include, or more commonly a lambda expression that applies some logic on 
the row entity. For very large frames, parallel processing is also supported with a call to `DataFrame.rows().parallel()`. 

The code below shows a simple way of filtering the ATP match results to only include the 5 set matches in 2013.

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);
DataFrame<Integer,String> filter = frame.rows().select(row -> {
    final int wonSets = row.getInt("Wsets");
    final int lostSets = row.getInt("Lsets");
    return  wonSets + lostSets == 5;
});
```
<div class="frame"><pre class="frame">
 Index  |  Location   |    Tournament     |     Date     |    Series    |   Court   |  Surface  |    Round    |  Best of  |       Winner        |     Loser      |  WRank  |  LRank  |  WPts  |  LPts  |  W1  |  L1  |  W2  |  L2  |  W3  |  L3  |  W4  |  L4  |  W5  |  L5  |  Wsets  |  Lsets  |   Comment   |  B365W   |  B365L   |   EXW    |   EXL    |   LBW    |   LBL    |   PSW    |   PSL    |   SJW    |   SJL    |   MaxW   |   MaxL   |   AvgW   |   AvgL   |
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
   139  |  Melbourne  |  Australian Open  |  2013-01-14  |  Grand Slam  |  Outdoor  |     Hard  |  1st Round  |        5  |         Almagro N.  |    Johnson S.  |     11  |    175  |  2515  |   302  |   7  |   5  |   6  |   7  |   6  |   2  |   6  |   7  |   6  |   2  |      3  |      2  |  Completed  |  1.1400  |  5.5000  |  1.1300  |  5.7500  |  1.1100  |  6.0000  |  1.1900  |  5.5400  |  1.1300  |  6.0000  |  1.1900  |  7.9000  |  1.1400  |  5.4900  |
   144  |  Melbourne  |  Australian Open  |  2013-01-14  |  Grand Slam  |  Outdoor  |     Hard  |  1st Round  |        5  |   Gimeno-Traver D.  |      Kubot L.  |     71  |     76  |   704  |   675  |   6  |   7  |   6  |   4  |   6  |   0  |   4  |   6  |   6  |   4  |      3  |      2  |  Completed  |  3.2500  |  1.3300  |  3.1500  |  1.3500  |  3.4000  |  1.3000  |  3.9700  |  1.2900  |  2.7500  |  1.4400  |  3.9700  |  1.4400  |  3.2500  |  1.3300  |
   145  |  Melbourne  |  Australian Open  |  2013-01-14  |  Grand Slam  |  Outdoor  |     Hard  |  1st Round  |        5  |  Roger-Vasselin E.  |  Bemelmans R.  |    101  |    114  |   539  |   486  |   6  |   3  |   6  |   7  |   2  |   6  |   7  |   5  |  11  |   9  |      3  |      2  |  Completed  |  2.2000  |  1.6100  |  2.0500  |  1.7500  |  2.0000  |  1.7300  |  2.1100  |  1.8100  |  2.0000  |  1.8000  |  2.3800  |  1.8100  |  2.0700  |  1.7200  |
   147  |  Melbourne  |  Australian Open  |  2013-01-14  |  Grand Slam  |  Outdoor  |     Hard  |  1st Round  |        5  |        Verdasco F.  |     Goffin D.  |     24  |     49  |  1445  |   843  |   6  |   3  |   3  |   6  |   4  |   6  |   6  |   3  |   6  |   4  |      3  |      2  |  Completed  |  1.6100  |  2.2000  |  1.6500  |  2.2000  |  1.6200  |  2.2500  |  1.7200  |  2.2500  |  1.5700  |  2.3800  |  1.8000  |  2.3800  |  1.6600  |  2.1900  |
   153  |  Melbourne  |  Australian Open  |  2013-01-14  |  Grand Slam  |  Outdoor  |     Hard  |  1st Round  |        5  |         Youzhny M.  |      Ebden M.  |     25  |    108  |  1335  |   509  |   4  |   6  |   6  |   7  |   6  |   2  |   7  |   6  |   6  |   3  |      3  |      2  |  Completed  |  1.1600  |  5.0000  |  1.2000  |  4.4000  |  1.1700  |  5.0000  |  1.1900  |  5.3900  |  1.1700  |  5.0000  |  1.2000  |  6.7000  |  1.1700  |  4.9300  |
   156  |  Melbourne  |  Australian Open  |  2013-01-14  |  Grand Slam  |  Outdoor  |     Hard  |  1st Round  |        5  |           Baker B.  |  Bogomolov A.  |     57  |    133  |   776  |   422  |   7  |   6  |   6  |   3  |   6  |   7  |   3  |   6  |   6  |   2  |      3  |      2  |  Completed  |  1.7200  |  2.0000  |  1.6500  |  2.2000  |  1.6700  |  2.1000  |  1.8500  |  2.0700  |  1.6200  |  2.2500  |  1.8500  |  2.2500  |  1.7100  |  2.0800  |
   161  |  Melbourne  |  Australian Open  |  2013-01-14  |  Grand Slam  |  Outdoor  |     Hard  |  1st Round  |        5  |             Ito T.  |    Millman J.  |     84  |    184  |   633  |   279  |   6  |   4  |   6  |   4  |   3  |   6  |   0  |   6  |   7  |   5  |      3  |      2  |  Completed  |  3.7500  |  1.2500  |  3.4000  |  1.3000  |  3.0000  |  1.3600  |  3.8900  |  1.3000  |  3.0000  |  1.3600  |  4.0500  |  1.3600  |  3.4800  |  1.3000  |
   165  |  Melbourne  |  Australian Open  |  2013-01-14  |  Grand Slam  |  Outdoor  |     Hard  |  1st Round  |        5  |        Stepanek R.  |    Troicki V.  |     34  |     37  |  1090  |  1030  |   5  |   7  |   4  |   6  |   6  |   3  |   6  |   3  |   7  |   5  |      3  |      2  |  Completed  |  2.5000  |  1.5000  |  2.5500  |  1.5000  |  2.6200  |  1.4400  |  2.6200  |  1.5600  |  2.2000  |  1.6700  |  2.8800  |  1.5600  |  2.5900  |  1.4900  |
   166  |  Melbourne  |  Australian Open  |  2013-01-14  |  Grand Slam  |  Outdoor  |     Hard  |  1st Round  |        5  |        Bautista R.  |    Fognini F.  |     56  |     47  |   786  |   880  |   6  |   0  |   2  |   6  |   6  |   4  |   3  |   6  |   6  |   1  |      3  |      2  |  Completed  |  2.0000  |  1.7200  |  1.9800  |  1.8000  |  2.0000  |  1.7300  |  2.0300  |  1.8700  |  2.0000  |  1.8000  |  2.1800  |  1.8900  |  2.0000  |  1.7700  |
   170  |  Melbourne  |  Australian Open  |  2013-01-14  |  Grand Slam  |  Outdoor  |     Hard  |  1st Round  |        5  |       Baghdatis M.  |      Ramos A.  |     35  |     51  |  1070  |   830  |   6  |   7  |   7  |   6  |   6  |   4  |   3  |   6  |   6  |   3  |      3  |      2  |  Completed  |  1.0800  |  8.0000  |  1.0700  |  8.0000  |  1.0700  |  8.0000  |  1.0900  |  8.9100  |  1.0800  |  7.5000  |  1.1000  |  9.4000  |  1.0800  |  7.5000  |
</pre></div>

#### Select Columns

The API is completely symmetrical in the row and column dimension, so filtering a frame along the latter axis 
simply involves a call to `select()` on the column dimension operator (`DataFrame.cols()`). The code below 
demonstrates column filtering where an explicit set of column names are provided rather than using a lambda expression 
as in the previous row selection example. In this scenario we wish to select all the betting odds that were 
available on the eventual winner of the games in 2013.

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);
DataFrame<Integer,String> filter = frame.cols().select("B365W", "EXW", "LBW", "PSW" ,"SJW");
filter.out().print(10);
```
<div class="frame"><pre class="frame">
 Index  |  B365W   |   EXW    |   LBW    |   PSW    |   SJW    |
----------------------------------------------------------------
     0  |  1.3600  |  1.4500  |  1.4400  |  1.4700  |  1.4400  |
     1  |  1.6100  |  1.7500  |  1.8000  |  1.8000  |  1.7300  |
     2  |  1.2500  |  1.2500  |  1.2900  |  1.3000  |  1.3000  |
     3  |  1.0700  |  1.0600  |  1.0800  |  1.0800  |  1.0700  |
     4  |  1.9000  |  1.8700  |  1.9100  |  1.8800  |  1.9100  |
     5  |  1.6100  |  1.7500  |  1.7300  |  1.7000  |  1.8000  |
     6  |  2.2000  |  2.0800  |  1.9100  |  2.2600  |  2.0000  |
     7  |  1.4400  |  1.5500  |  1.4400  |  1.6000  |  1.5000  |
     8  |  3.0000  |  2.5000  |  2.3800  |  2.9300  |  2.5000  |
     9  |  1.3600  |  1.4000  |  1.4400  |  1.3800  |  1.4000  |
</pre></div>

#### Select Rows & Columns

Filtering in the row and column dimension simultaneously is also possible via one of the overloaded `select()` methods 
available on the `DataFrame` class itself, which either takes the row and column keys to include, or lambda expressions
to operate on the row and column entities. Parallel select is also supported by calling the `parallel()`method on the 
frame prior to `select()`.

Let's investigate which if any of the bookmakers provided more attractive odds in the various finals in 2013. 
Before we do this, we need to standardize the betting odds so that we can not only make cross-sectional comparisons 
for a given match, but also across different matches. To do this, we transform all the odds to a [z-score](https://en.wikipedia.org/wiki/Standard_score) 
by first subtracting the mean across the bookmakers, and then dividing by the standard deviation. The code below performs 
this transformation.

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);

//Array of the various bookmaker keys
Array<String> bookies = Array.of("B365", "EX", "LB", "PS" ,"SJ");

//Compute z-scores for winner & loser odds independently
Stream.of("W", "L").forEach(x -> {
    Array<String> colNames = bookies.mapValues(v -> v.getValue() + x);
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

//Print last 14 columns and first 10 rows to std out
frame.right(14).out().print(10, options -> {
    options.withDecimalFormat(Double.class, "0.000;-0.000", 1);
});
```
<div class="frame"><pre class="frame">
 Index  |  B365W   |  B365L   |   EXW    |   EXL    |   LBW    |   LBL    |   PSW    |   PSL    |   SJW    |   SJL    |  MaxW   |  MaxL   |  AvgW   |  AvgL   |
---------------------------------------------------------------------------------------------------------------------------------------------------------------
     0  |  -1.711  |   1.482  |   0.428  |  -0.593  |   0.190  |  -0.771  |   0.903  |   0.593  |   0.190  |  -0.711  |  1.470  |  3.200  |  1.420  |  2.780  |
     1  |  -1.643  |   1.423  |   0.154  |  -0.378  |   0.796  |  -1.189  |   0.796  |   0.523  |  -0.103  |  -0.378  |  1.800  |  2.260  |  1.730  |  2.050  |
     2  |  -1.082  |   0.532  |  -1.082  |   0.532  |   0.464  |  -0.418  |   0.850  |   0.912  |   0.850  |  -1.558  |  1.300  |  4.200  |  1.280  |  3.580  |
     3  |  -0.239  |   0.817  |  -1.434  |  -0.077  |   0.956  |  -0.971  |   0.956  |   1.201  |  -0.239  |  -0.971  |  1.100  |  9.500  |  1.080  |  7.760  |
     4  |   0.330  |  -0.620  |  -1.321  |   0.184  |   0.881  |  -0.620  |  -0.771  |   1.677  |   0.881  |  -0.620  |  2.050  |  2.000  |  1.880  |  1.850  |
     5  |  -1.532  |   0.817  |   0.454  |  -0.501  |   0.170  |  -0.501  |  -0.255  |   1.278  |   1.163  |  -1.094  |  1.850  |  2.280  |  1.710  |  2.080  |
     6  |   0.770  |  -1.305  |  -0.070  |  -0.454  |  -1.260  |   1.390  |   1.190  |  -0.028  |  -0.630  |   0.397  |  2.320  |  1.830  |  2.080  |  1.700  |
     7  |  -0.945  |   0.952  |   0.630  |  -1.428  |  -0.945  |   0.952  |   1.346  |  -0.370  |  -0.086  |  -0.106  |  1.630  |  2.820  |  1.520  |  2.460  |
     8  |   1.199  |  -1.615  |  -0.574  |   0.479  |  -1.000  |   0.927  |   0.950  |  -0.269  |  -0.574  |   0.479  |  3.250  |  1.530  |  2.660  |  1.470  |
     9  |  -1.214  |   0.400  |   0.135  |  -0.355  |   1.483  |  -1.034  |  -0.539  |   1.533  |   0.135  |  -0.544  |  1.450  |  3.550  |  1.390  |  2.870  |
</pre></div>

Now that we have standardized all the odds, we can try and make an assessment of which bookmaker on average provided
the most attractive odds. It should be noted that this example is purely to demonstrate API usage, it is not intended to 
represent rigorous statistical analysis of this data, so take the results with a huge grain of salt.  This data could
be fairly noisy, so let's only consider the finals since there is likely to be a higher degree of consensus for those
matches. We therefore need to filter on a subset of rows (finals only) and also only include odds on the eventual winner 
for brevity.

<?prettify?>
```java
//Select z-score odds on eventual winner across all bookmakers
Set<String> colNames = Collect.asSet("B365W", "EXW", "LBW", "PSW" ,"SJW");
DataFrame<Integer,String> finalWinnerOdds = frame.select(
    row -> row.getValue("Round").equals("The Final"),
    col -> colNames.contains(col.key())
);
```
We can now compute the mean of these columns, which gives us the average z-score for each bookmaker across all 
the finals in 2013. The code below demonstrates this, and prints the results to std-out with formatting applied.

<?prettify?>
```java
//Compute the mean of these z-scores and print
finalWinnerOdds.cols().stats().mean().out().print(options -> {
    options.withDecimalFormat(Double.class, "0.000;-0.000", 1);
});
```
<div class="frame"><pre class="frame">
 Index  |  B365W   |   EXW    |   LBW    |   PSW   |   SJW    |
---------------------------------------------------------------
  MEAN  |  -0.259  |  -0.583  |  -0.033  |  0.953  |  -0.078  |
</pre></div>

This naive analysis suggests that using a sample that includes all the ATP finals in 2013, on average, Pinnacles 
Sports (PSW) provided the most attractive odds and Expekt (EX) provided the least attractive odds. These standardized
scores represent the number of standard deviations above / below the mean, and if there was a very high degree of 
consensus across the bookmakers, in that the absolute odds were very similar, these figures may not really tell
us much. Again, this example is to illustrate API usage, it is not intended to draw any far reaching conclusions 
on the skills of bookmakers.

#### Monadic

The Morpheus API enables data analysis procedures to be chained together in order to facilitate powerful transformations 
to be applied with fairly little code. To demonstrate, consider an example where we wish to compute the summary statistics 
(say min, max, mean and std. deviation) on the number of games played in 5 set matches in 2013 across all the tournaments. 
The first code example below demonstrates a longer form solution where the intermediate frames are explicitly captured as 
variables, which for a beginner would likely be easier to follow:

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);
//Select all 5 set matches, and the number of games won/lost in each set
DataFrame<Integer,String> filter = frame.select(
    row -> row.getDouble("Wsets") + row.getDouble("Lsets") == 5,
    col -> col.key().matches("(W|L)\\d+")
);
//Sum the rows which yields total games played per 5 set match
DataFrame<Integer,StatType> gameCounts = filter.rows().stats().sum();
//The game count frame is Nx1, so compute summary stats on game counts
DataFrame<StatType,StatType> gameStats = gameCounts.cols().describe(MIN, MAX, MEAN, STD_DEV);
//Print results to std-out
gameStats.out().print(formats -> {
    formats.withDecimalFormat(Double.class, "0.00;-0.00", 1);
});
```
<div class="frame"><pre class="frame">
 Index  |   MIN   |   MAX   |  MEAN   |  STD_DEV  |
---------------------------------------------------
   SUM  |  40.00  |  66.00  |  51.15  |     5.29  |
</pre></div>

By leveraging the monadic nature of the Morpheus API, it's possible to perform this analysis in fewer lines of code
as shown below. Whether this is preferable or not is another question, at least in the context of this example,
but suffice to say that the monadic nature of the API can facilitate brevity for trivial steps in more complicated
analyzes.

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);
frame.select(
    row -> row.getDouble("Wsets") + row.getDouble("Lsets") == 5,
    col -> col.key().matches("(W|L)\\d+")
).rows().stats().sum().cols().describe(
    MIN, MAX, MEAN, STD_DEV
).out().print(formats -> {
    formats.withDecimalFormat(Double.class, "0.00;-0.00", 1);
});
```
<div class="frame"><pre class="frame">
 Index  |   MIN   |   MAX   |  MEAN   |  STD_DEV  |
---------------------------------------------------
   SUM  |  40.00  |  66.00  |  51.15  |     5.29  |
</pre></div>

#### Head & Tail

The `head()` method on the `DataFrame` class provides an easy mechanism to select the first N rows as follows:

<?prettify?>
```java
//Filter on the first 5 rows
frame.head(5).out().print();
```
<div class="frame"><pre class="frame">
 Index  |  Location  |        Tournament        |     Date     |  Series  |   Court   |  Surface  |    Round    |  Best of  |     Winner     |     Loser      |  WRank  |  LRank  |  WPts  |  LPts  |  W1  |  L1  |  W2  |  L2  |  W3  |  L3  |  W4  |  L4  |  W5  |  L5  |  Wsets  |  Lsets  |   Comment   |  B365W   |  B365L   |   EXW    |   EXL    |   LBW    |   LBL    |   PSW    |   PSL    |   SJW    |   SJL    |   MaxW   |   MaxL   |   AvgW   |   AvgL   |
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     0  |  Brisbane  |  Brisbane International  |  2012-12-31  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |      Mayer F.  |    Giraldo S.  |     28  |     57  |  1215  |   778  |   6  |   4  |   6  |   4  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.3600  |  3.0000  |  1.4500  |  2.6500  |  1.4400  |  2.6200  |  1.4700  |  2.8500  |  1.4400  |  2.6300  |  1.4700  |  3.2000  |  1.4200  |  2.7800  |
     1  |  Brisbane  |  Brisbane International  |  2012-12-31  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |   Nieminen J.  |  Benneteau J.  |     41  |     35  |   927  |  1075  |   6  |   3  |   2  |   6  |   6  |   1  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |  1.6100  |  2.2000  |  1.7500  |  2.0000  |  1.8000  |  1.9100  |  1.8000  |  2.1000  |  1.7300  |  2.0000  |  1.8000  |  2.2600  |  1.7300  |  2.0500  |
     2  |  Brisbane  |  Brisbane International  |  2012-12-31  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |  Nishikori K.  |  Matosevic M.  |     19  |     49  |  1830  |   845  |   7  |   5  |   6  |   2  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.2500  |  3.7500  |  1.2500  |  3.7500  |  1.2900  |  3.5000  |  1.3000  |  3.8500  |  1.3000  |  3.2000  |  1.3000  |  4.2000  |  1.2800  |  3.5800  |
     3  |  Brisbane  |  Brisbane International  |  2012-12-31  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |  Baghdatis M.  |   Mitchell B.  |     36  |    326  |  1070  |   137  |   6  |   4  |   6  |   4  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.0700  |  9.0000  |  1.0600  |  8.0000  |  1.0800  |  7.0000  |  1.0800  |  9.4300  |  1.0700  |  7.0000  |  1.1000  |  9.5000  |  1.0800  |  7.7600  |
     4  |  Brisbane  |  Brisbane International  |  2013-01-01  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |    Istomin D.  |     Klizan M.  |     43  |     30  |   897  |  1175  |   6  |   1  |   6  |   2  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.9000  |  1.8000  |  1.8700  |  1.8700  |  1.9100  |  1.8000  |  1.8800  |  2.0000  |  1.9100  |  1.8000  |  2.0500  |  2.0000  |  1.8800  |  1.8500  |
</pre></div>

The `tail()` method on the `DataFrame` class provides an easy mechanism to select the last N rows as follows:

<?prettify?>
```java
//Filter on the last 5 rows
frame.tail(5).out().print();
```
<div class="frame"><pre class="frame">
 Index  |  Location  |  Tournament   |     Date     |    Series     |  Court   |  Surface  |     Round     |  Best of  |    Winner     |      Loser       |  WRank  |  LRank  |  WPts   |  LPts   |  W1  |  L1  |  W2  |  L2  |  W3  |  L3  |  W4  |  L4  |  W5  |  L5  |  Wsets  |  Lsets  |   Comment   |  B365W   |   B365L   |   EXW    |   EXL    |   LBW    |    LBL    |   PSW    |    PSL    |   SJW    |   SJL    |   MaxW   |   MaxL    |   AvgW   |   AvgL   |
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  2626  |    London  |  Masters Cup  |  2013-11-09  |  Masters Cup  |  Indoor  |     Hard  |  Round Robin  |        3  |   Federer R.  |  Del Potro J.M.  |      7  |      5  |   3805  |   5055  |   4  |   6  |   7  |   6  |   7  |   5  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |  2.0000  |   1.8000  |  2.0000  |  1.8000  |  2.0000  |   1.8000  |  2.0700  |   1.8500  |  2.0000  |  1.8000  |  2.1000  |   1.9500  |  1.9800  |  1.8100  |
  2627  |    London  |  Masters Cup  |  2013-11-09  |  Masters Cup  |  Indoor  |     Hard  |  Round Robin  |        3  |  Djokovic N.  |      Gasquet R.  |      2  |      9  |  10610  |   3300  |   7  |   6  |   4  |   6  |   6  |   3  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |  1.0600  |  10.0000  |  1.0500  |  9.0000  |  1.0500  |  10.0000  |  1.0700  |  10.7500  |  1.0500  |  9.0000  |  1.1000  |  10.7500  |  1.0600  |  9.2200  |
  2628  |    London  |  Masters Cup  |  2013-11-10  |  Masters Cup  |  Indoor  |     Hard  |   Semifinals  |        3  |     Nadal R.  |      Federer R.  |      1  |      7  |  12030  |   3805  |   7  |   5  |   6  |   3  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.3000  |   3.5000  |  1.3300  |  3.2000  |  1.3000  |   3.5000  |  1.3400  |   3.6700  |  1.3300  |  3.4000  |  1.3700  |   4.0500  |  1.3300  |  3.3400  |
  2629  |    London  |  Masters Cup  |  2013-11-10  |  Masters Cup  |  Indoor  |     Hard  |   Semifinals  |        3  |  Djokovic N.  |     Wawrinka S.  |      2  |      8  |  10610  |   3330  |   6  |   3  |   6  |   3  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.1400  |   5.5000  |  1.1300  |  5.5000  |  1.1400  |   5.5000  |  1.1900  |   5.6100  |  1.1500  |  5.5000  |  1.1900  |   6.3000  |  1.1600  |  5.2500  |
  2630  |    London  |  Masters Cup  |  2013-11-11  |  Masters Cup  |  Indoor  |     Hard  |    The Final  |        3  |  Djokovic N.  |        Nadal R.  |      2  |      1  |  10610  |  12030  |   6  |   3  |   6  |   4  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.5700  |   2.3700  |  1.5500  |  2.4000  |  1.5700  |   2.3800  |  1.5800  |   2.5800  |  1.5700  |  2.3800  |  1.6500  |   2.5800  |  1.5700  |  2.4300  |
</pre></div>

#### Right & Left

The `left()` method is an analog to the `head()` method but selects the first N columns as follows:

<?prettify?>
```java
//Filter on the first 5 columns
frame.left(5).out().print();
```

<div class="frame"><pre class="frame">
 Index  |  Location  |        Tournament        |     Date     |  Series  |   Court   |
---------------------------------------------------------------------------------------
     0  |  Brisbane  |  Brisbane International  |  2012-12-31  |  ATP250  |  Outdoor  |
     1  |  Brisbane  |  Brisbane International  |  2012-12-31  |  ATP250  |  Outdoor  |
     2  |  Brisbane  |  Brisbane International  |  2012-12-31  |  ATP250  |  Outdoor  |
     3  |  Brisbane  |  Brisbane International  |  2012-12-31  |  ATP250  |  Outdoor  |
     4  |  Brisbane  |  Brisbane International  |  2013-01-01  |  ATP250  |  Outdoor  |
     5  |  Brisbane  |  Brisbane International  |  2013-01-01  |  ATP250  |  Outdoor  |
     6  |  Brisbane  |  Brisbane International  |  2013-01-01  |  ATP250  |  Outdoor  |
     7  |  Brisbane  |  Brisbane International  |  2013-01-01  |  ATP250  |  Outdoor  |
     8  |  Brisbane  |  Brisbane International  |  2013-01-01  |  ATP250  |  Outdoor  |
     9  |  Brisbane  |  Brisbane International  |  2013-01-01  |  ATP250  |  Outdoor  |
</pre></div>

The `right()` method is an analog to the `tail()` method but selects the last N columns as follows:

<?prettify?>
```java
//Filter on the last 14 columns
frame.right(14).out().print();
```

<div class="frame"><pre class="frame">
 Index  |  B365W   |  B365L   |   EXW    |   EXL    |   LBW    |   LBL    |   PSW    |   PSL    |   SJW    |   SJL    |   MaxW   |   MaxL   |   AvgW   |   AvgL   |
-------------------------------------------------------------------------------------------------------------------------------------------------------------------
     0  |  1.3600  |  3.0000  |  1.4500  |  2.6500  |  1.4400  |  2.6200  |  1.4700  |  2.8500  |  1.4400  |  2.6300  |  1.4700  |  3.2000  |  1.4200  |  2.7800  |
     1  |  1.6100  |  2.2000  |  1.7500  |  2.0000  |  1.8000  |  1.9100  |  1.8000  |  2.1000  |  1.7300  |  2.0000  |  1.8000  |  2.2600  |  1.7300  |  2.0500  |
     2  |  1.2500  |  3.7500  |  1.2500  |  3.7500  |  1.2900  |  3.5000  |  1.3000  |  3.8500  |  1.3000  |  3.2000  |  1.3000  |  4.2000  |  1.2800  |  3.5800  |
     3  |  1.0700  |  9.0000  |  1.0600  |  8.0000  |  1.0800  |  7.0000  |  1.0800  |  9.4300  |  1.0700  |  7.0000  |  1.1000  |  9.5000  |  1.0800  |  7.7600  |
     4  |  1.9000  |  1.8000  |  1.8700  |  1.8700  |  1.9100  |  1.8000  |  1.8800  |  2.0000  |  1.9100  |  1.8000  |  2.0500  |  2.0000  |  1.8800  |  1.8500  |
     5  |  1.6100  |  2.2000  |  1.7500  |  2.0000  |  1.7300  |  2.0000  |  1.7000  |  2.2700  |  1.8000  |  1.9100  |  1.8500  |  2.2800  |  1.7100  |  2.0800  |
     6  |  2.2000  |  1.6100  |  2.0800  |  1.6700  |  1.9100  |  1.8000  |  2.2600  |  1.7000  |  2.0000  |  1.7300  |  2.3200  |  1.8300  |  2.0800  |  1.7000  |
     7  |  1.4400  |  2.6200  |  1.5500  |  2.3500  |  1.4400  |  2.6200  |  1.6000  |  2.4700  |  1.5000  |  2.5000  |  1.6300  |  2.8200  |  1.5200  |  2.4600  |
     8  |  3.0000  |  1.3600  |  2.5000  |  1.5000  |  2.3800  |  1.5300  |  2.9300  |  1.4500  |  2.5000  |  1.5000  |  3.2500  |  1.5300  |  2.6600  |  1.4700  |
     9  |  1.3600  |  3.0000  |  1.4000  |  2.8000  |  1.4400  |  2.6200  |  1.3800  |  3.3000  |  1.4000  |  2.7500  |  1.4500  |  3.5500  |  1.3900  |  2.8700  |
</pre></div>

It is sometimes useful to combine these functions to select parts of the frame. For example, consider a scenario
where we wish to select all the betting odds, but excluding the maximum and averages that are represented by the
last 4 columns of the dataset. This can be achieved with a combined `right()` and `left()` operation as follows:

<?prettify?>
```java
//Filter on all betting ods, excluding max & averages
frame.right(14).left(10).out().print();
```

<div class="frame"><pre class="frame">
 Index  |  B365W   |  B365L   |   EXW    |   EXL    |   LBW    |   LBL    |   PSW    |   PSL    |   SJW    |   SJL    |
-----------------------------------------------------------------------------------------------------------------------
     0  |  1.3600  |  3.0000  |  1.4500  |  2.6500  |  1.4400  |  2.6200  |  1.4700  |  2.8500  |  1.4400  |  2.6300  |
     1  |  1.6100  |  2.2000  |  1.7500  |  2.0000  |  1.8000  |  1.9100  |  1.8000  |  2.1000  |  1.7300  |  2.0000  |
     2  |  1.2500  |  3.7500  |  1.2500  |  3.7500  |  1.2900  |  3.5000  |  1.3000  |  3.8500  |  1.3000  |  3.2000  |
     3  |  1.0700  |  9.0000  |  1.0600  |  8.0000  |  1.0800  |  7.0000  |  1.0800  |  9.4300  |  1.0700  |  7.0000  |
     4  |  1.9000  |  1.8000  |  1.8700  |  1.8700  |  1.9100  |  1.8000  |  1.8800  |  2.0000  |  1.9100  |  1.8000  |
     5  |  1.6100  |  2.2000  |  1.7500  |  2.0000  |  1.7300  |  2.0000  |  1.7000  |  2.2700  |  1.8000  |  1.9100  |
     6  |  2.2000  |  1.6100  |  2.0800  |  1.6700  |  1.9100  |  1.8000  |  2.2600  |  1.7000  |  2.0000  |  1.7300  |
     7  |  1.4400  |  2.6200  |  1.5500  |  2.3500  |  1.4400  |  2.6200  |  1.6000  |  2.4700  |  1.5000  |  2.5000  |
     8  |  3.0000  |  1.3600  |  2.5000  |  1.5000  |  2.3800  |  1.5300  |  2.9300  |  1.4500  |  2.5000  |  1.5000  |
     9  |  1.3600  |  3.0000  |  1.4000  |  2.8000  |  1.4400  |  2.6200  |  1.3800  |  3.3000  |  1.4000  |  2.7500  |
</pre></div>

#### Filter Method

The `DataFrame.rows()` and `DataFrame.cols()` methods return a versatile interface to operate on the row
and column dimension respectively. The examples above demonstrate how to use the `select()` method to
create a `DataFrame` filter to operate on a subset of the data. A `filter()` method also exists which
provides a shortcut to more directly operate on a subset of rows or columns without creating the 
intermediate filter frame.

The example below demonstrates how to iterate over a subset of rows that represent an upset in a finals match 
in 2013 according to the average bookmaker odds. Higher odds represent a less likely outcome, so if the
average odds on the eventual winner of a game were greater than those for the eventual loser, that _classifies_
as an upset because the favourite did not prevail. Concurrent iteration is also supported with a call to 
`parallel()`, however then order is not guaranteed which may or may not be important.

<?prettify?>
```java
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
```

<div class="frame"><pre class="frame">
Finals upset when Gasquet R. beat Davydenko N. at Qatar Exxon Mobil Open on 2013-01-05
Finals upset when Zeballos H. beat Nadal R. at VTR Open on 2013-02-10
Finals upset when Tsonga J.W. beat Berdych T. at Open 13 on 2013-02-24
Finals upset when Robredo T. beat Anderson K. at Grand Prix Hassan II on 2013-04-14
Finals upset when Isner J. beat Almagro N. at U.S. Men's Clay Court Championships on 2013-04-14
Finals upset when Djokovic N. beat Nadal R. at Monte Carlo Masters on 2013-04-21
Finals upset when Wawrinka S. beat Ferrer D. at Portugal Open on 2013-05-05
Finals upset when Montanes A. beat Monfils G. at Open de Nice C�te d�Azur on 2013-05-25
Finals upset when Mahut N. beat Wawrinka S. at Topshelf Open on 2013-06-22
Finals upset when Murray A. beat Djokovic N. at Wimbledon on 2013-07-07
Finals upset when Berlocq C. beat Verdasco F. at SkiStar Swedish Open on 2013-07-14
Finals upset when Fognini F. beat Kohlschreiber P. at Mercedes Cup on 2013-07-14
Finals upset when Karlovic I. beat Falla A. at Claro Open Colombia on 2013-07-21
Finals upset when Robredo T. beat Fognini F. at ATP Vegeta Croatia Open on 2013-07-28
Finals upset when Granollers M. beat Monaco J. at Bet-At-Home Cup on 2013-08-03
Finals upset when Melzer J. beat Monfils G. at Winston-Salem Open at Wake Forest University on 2013-08-24
Finals upset when Simon G. beat Tsonga J.W. at Open de Moselle on 2013-09-22
Finals upset when Raonic M. beat Berdych T. at Thailand Open on 2013-09-29
Finals upset when Sousa J. beat Benneteau J. at Malaysian Open on 2013-09-29
Finals upset when Dimitrov G. beat Ferrer D. at Stockholm Open on 2013-10-20
Finals upset when Youzhny M. beat Ferrer D. at Valencia Open 500 on 2013-10-27
</pre></div>

Again, to re-iterate the symmetrical nature of the API, consider the following where we iterate over a
subset of the columns to discover what the minimum and maximum betting odds that were available on
the eventual winner of each match by the 5 different book makers.

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);
frame.cols().filter("B365W", "EXW", "LBW", "PSW" ,"SJW").forEach(column -> {
    Optional<Bounds<Double>> bounds = column.bounds();
    bounds.ifPresent(b -> {
        System.out.println(column.key() + " max odds=" + b.upper() + ", min odds=" + b.lower());
    });
});
```
<div class="frame"><pre class="frame">
B365W max odds=29.0, min odds=1.0
EXW max odds=20.0, min odds=1.01
LBW max odds=26.0, min odds=1.0
PSW max odds=46.0, min odds=1.01
SJW max odds=17.0, min odds=1.01
</pre></div>

#### Immutability

In the introduction, it was suggested that a `DataFrame` that represents a filter on another frame is structurally 
immutable in some cases, in that additional columns cannot always be added (in all cases, additional rows cannot be
added to a filter). Data mutability on the other hand is supported on a filter, as illustrated by the example where 
we transformed absolute betting odds to a Z-score.  The structural immutability caveats are in explained in this section.

##### Column Store

A `DataFrame` is by default a column store, where each column is represented by a Morpheus array. When filtering such 
a frame, it is not possible to add rows to the filter frame, but adding columns is supported. The reason for this 
restriction is because the filter frame shares its content with the parent it was created from, so adding rows would 
result in also modifying the column arrays of the parent frame, which would lead to all sorts of issues. The filter 
frame does have a completely independent column axis however, so it is possible to add additional columns without this 
having any impact on the parent.

The following example demonstrates how an additional column can be added to the filter frame after we have selected all 
matches in which Novak Djokovic was the victor. Here we add an additional column to store the the total number of games
that Novak won in each match.

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);
//Select rows where Djokovic was the victor
DataFrame<Integer,String> filter = frame.rows().select(row -> row.getValue("Winner").equals("Djokovic N."));
//Add a column to the filter and seed with number of games won by Djokovic
Array<String> colNames = Range.of(1, 6).map(i -> "W" + i).toArray();
//Add new column to capture total number of games won by novak
filter.cols().add("WonCount", Double.class, value -> {
    final DataFrameRow<Integer,String> row = value.row(); //Access row associated with this value
    return colNames.mapToDoubles(v -> row.getDouble(v.getValue())).stats().sum().doubleValue();
});

//Print 10x10 section of right most columns
filter.right(10).out().print(10);
```
The column on the far right is the newly added column initialized with the number of games won by Novak.

<div class="frame"><pre class="frame">
 Index  |    LBL    |   PSW    |    PSL    |   SJW    |    SJL    |   MaxW   |   MaxL    |   AvgW   |   AvgL    |  WonCount  |
------------------------------------------------------------------------------------------------------------------------------
   152  |  13.0000  |  1.0100  |  41.0000  |  1.0100  |  19.0000  |  1.0200  |  46.0000  |  1.0100  |  21.7000  |   19.0000  |
   218  |  21.0000  |  1.0100  |  31.0000  |  1.0100  |  19.0000  |  1.0200  |  51.0000  |  1.0100  |  23.3800  |   18.0000  |
   236  |  17.0000  |  1.0100  |  41.0000  |  1.0100  |  13.0000  |  1.0200  |  63.2500  |  1.0100  |  24.9400  |   19.0000  |
   254  |  13.0000  |  1.0200  |  21.5000  |  1.0200  |  13.0000  |  1.0300  |  29.0000  |  1.0200  |  15.9400  |   32.0000  |
   260  |   5.0000  |  1.2200  |   4.9200  |  1.1800  |   5.2500  |  1.2300  |   5.3000  |  1.1800  |   4.7300  |   22.0000  |
   263  |   6.5000  |  1.1200  |   7.9400  |  1.1000  |   8.0000  |  1.1400  |   9.4000  |  1.1000  |   6.9100  |   18.0000  |
   265  |   2.5000  |  1.5400  |   2.7400  |  1.5300  |   2.6300  |  1.5700  |   2.9000  |  1.5000  |   2.6100  |   25.0000  |
   597  |  17.0000  |  1.0200  |  21.9700  |  1.0300  |  12.0000  |  1.0300  |  31.0000  |  1.0200  |  16.5400  |   12.0000  |
   606  |  17.0000  |  1.0100  |  29.1600  |  1.0100  |  15.0000  |  1.0400  |  46.0000  |  1.0100  |  20.0900  |   13.0000  |
   609  |   9.0000  |  1.0300  |  19.0000  |  1.0400  |  10.0000  |  1.0500  |  24.0000  |  1.0300  |  13.4300  |   12.0000  |
</pre></div>

If we print the right most columns of the original frame, the `WonCount" column is not present.

<?prettify?>
```java
//Print 10x10 section of right most columns of original frame
frame.right(10).out().print(10);
```

<div class="frame"><pre class="frame">
 Index  |   LBW    |   LBL    |   PSW    |   PSL    |   SJW    |   SJL    |   MaxW   |   MaxL   |   AvgW   |   AvgL   |
-----------------------------------------------------------------------------------------------------------------------
     0  |  1.4400  |  2.6200  |  1.4700  |  2.8500  |  1.4400  |  2.6300  |  1.4700  |  3.2000  |  1.4200  |  2.7800  |
     1  |  1.8000  |  1.9100  |  1.8000  |  2.1000  |  1.7300  |  2.0000  |  1.8000  |  2.2600  |  1.7300  |  2.0500  |
     2  |  1.2900  |  3.5000  |  1.3000  |  3.8500  |  1.3000  |  3.2000  |  1.3000  |  4.2000  |  1.2800  |  3.5800  |
     3  |  1.0800  |  7.0000  |  1.0800  |  9.4300  |  1.0700  |  7.0000  |  1.1000  |  9.5000  |  1.0800  |  7.7600  |
     4  |  1.9100  |  1.8000  |  1.8800  |  2.0000  |  1.9100  |  1.8000  |  2.0500  |  2.0000  |  1.8800  |  1.8500  |
     5  |  1.7300  |  2.0000  |  1.7000  |  2.2700  |  1.8000  |  1.9100  |  1.8500  |  2.2800  |  1.7100  |  2.0800  |
     6  |  1.9100  |  1.8000  |  2.2600  |  1.7000  |  2.0000  |  1.7300  |  2.3200  |  1.8300  |  2.0800  |  1.7000  |
     7  |  1.4400  |  2.6200  |  1.6000  |  2.4700  |  1.5000  |  2.5000  |  1.6300  |  2.8200  |  1.5200  |  2.4600  |
     8  |  2.3800  |  1.5300  |  2.9300  |  1.4500  |  2.5000  |  1.5000  |  3.2500  |  1.5300  |  2.6600  |  1.4700  |
     9  |  1.4400  |  2.6200  |  1.3800  |  3.3000  |  1.4000  |  2.7500  |  1.4500  |  3.5500  |  1.3900  |  2.8700  |
</pre></div>

In contrast, if we attempt to add rows to the filter frame, we will get a `DataFrameException` because modifying
the shared column arrays would impact the parent from which the filter was created. Let's attempt to add 5 more 
rows as follows:

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);
//Select rows where Djokovic was the victor
DataFrame<Integer,String> filter = frame.rows().select(row -> row.getValue("Winner").equals("Djokovic N."));
//Define a range of 10 additional keys
Range<Integer> rowKeys = Range.of(frame.rowCount(), frame.rowCount()+5);
//Try add rows for new row keys
filter.rows().addAll(rowKeys);
```

<pre>
com.zavtech.morpheus.frame.DataFrameException: Cannot add keys to a sliced axis of a DataFrame
	at com.zavtech.morpheus.reference.XDataFrameContent.addRows(XDataFrameContent.java:300)
	at com.zavtech.morpheus.reference.XDataFrameRows.addAll(XDataFrameRows.java:96)
	at com.zavtech.morpheus.reference.XDataFrameRows.addAll(XDataFrameRows.java:89)
</pre>

If we create a deep copy of the filter frame, then we end up with a completely new data structure
containing just the rows of the filter, so we can now add rows as usual.

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);
//Select rows where Djokovic was the victor
DataFrame<Integer,String> filter = frame.rows().select(row -> row.getValue("Winner").equals("Djokovic N."));
//Create a deep copy of the filter frame
DataFrame<Integer,String> copy = filter.copy();
//Define a range of 5 additional keys
Range<Integer> rowKeys = Range.of(frame.rowCount(), frame.rowCount()+5);
//Add 5 new rows to the copy
copy.rows().addAll(rowKeys);
//Print last 10 rows
copy.tail(10).out().print();
```

<div class="frame"><pre class="frame">
 Index  |  Location  |  Tournament   |     Date     |    Series     |  Court   |  Surface  |     Round     |  Best of  |    Winner     |      Loser       |  WRank  |  LRank  |  WPts   |  LPts   |  W1  |  L1  |  W2  |  L2  |  W3  |  L3  |  W4  |  L4  |  W5  |  L5  |  Wsets  |  Lsets  |   Comment   |  B365W   |   B365L   |   EXW    |   EXL    |   LBW    |    LBL    |   PSW    |    PSL    |   SJW    |   SJL    |   MaxW   |   MaxL    |   AvgW   |   AvgL   |
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  2619  |    London  |  Masters Cup  |  2013-11-05  |  Masters Cup  |  Indoor  |     Hard  |  Round Robin  |        3  |  Djokovic N.  |      Federer R.  |      2  |      7  |  10610  |   3805  |   6  |   4  |   6  |   7  |   6  |   2  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |  1.2800  |   3.7500  |  1.2800  |  3.5000  |  1.2900  |   3.7500  |  1.3000  |   3.9300  |  1.2900  |  3.7500  |  1.3300  |   4.0000  |  1.2900  |  3.6000  |
  2623  |    London  |  Masters Cup  |  2013-11-07  |  Masters Cup  |  Indoor  |     Hard  |  Round Robin  |        3  |  Djokovic N.  |  Del Potro J.M.  |      2  |      5  |  10610  |   5055  |   6  |   3  |   3  |   6  |   6  |   3  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |  1.2800  |   3.7500  |  1.2800  |  3.5000  |  1.3000  |   3.5000  |  1.3000  |   3.8900  |  1.2900  |  3.7500  |  1.3300  |   4.0500  |  1.2800  |  3.6100  |
  2627  |    London  |  Masters Cup  |  2013-11-09  |  Masters Cup  |  Indoor  |     Hard  |  Round Robin  |        3  |  Djokovic N.  |      Gasquet R.  |      2  |      9  |  10610  |   3300  |   7  |   6  |   4  |   6  |   6  |   3  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |  1.0600  |  10.0000  |  1.0500  |  9.0000  |  1.0500  |  10.0000  |  1.0700  |  10.7500  |  1.0500  |  9.0000  |  1.1000  |  10.7500  |  1.0600  |  9.2200  |
  2629  |    London  |  Masters Cup  |  2013-11-10  |  Masters Cup  |  Indoor  |     Hard  |   Semifinals  |        3  |  Djokovic N.  |     Wawrinka S.  |      2  |      8  |  10610  |   3330  |   6  |   3  |   6  |   3  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.1400  |   5.5000  |  1.1300  |  5.5000  |  1.1400  |   5.5000  |  1.1900  |   5.6100  |  1.1500  |  5.5000  |  1.1900  |   6.3000  |  1.1600  |  5.2500  |
  2630  |    London  |  Masters Cup  |  2013-11-11  |  Masters Cup  |  Indoor  |     Hard  |    The Final  |        3  |  Djokovic N.  |        Nadal R.  |      2  |      1  |  10610  |  12030  |   6  |   3  |   6  |   4  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.5700  |   2.3700  |  1.5500  |  2.4000  |  1.5700  |   2.3800  |  1.5800  |   2.5800  |  1.5700  |  2.3800  |  1.6500  |   2.5800  |  1.5700  |  2.4300  |
  2631  |      null  |         null  |        null  |         null  |    null  |     null  |         null  |        0  |         null  |            null  |      0  |      0  |      0  |      0  |   0  |   0  |   0  |   0  |   0  |   0  |   0  |   0  |   0  |   0  |      0  |      0  |       null  |     NaN  |      NaN  |     NaN  |     NaN  |     NaN  |      NaN  |     NaN  |      NaN  |     NaN  |     NaN  |     NaN  |      NaN  |     NaN  |     NaN  |
  2632  |      null  |         null  |        null  |         null  |    null  |     null  |         null  |        0  |         null  |            null  |      0  |      0  |      0  |      0  |   0  |   0  |   0  |   0  |   0  |   0  |   0  |   0  |   0  |   0  |      0  |      0  |       null  |     NaN  |      NaN  |     NaN  |     NaN  |     NaN  |      NaN  |     NaN  |      NaN  |     NaN  |     NaN  |     NaN  |      NaN  |     NaN  |     NaN  |
  2633  |      null  |         null  |        null  |         null  |    null  |     null  |         null  |        0  |         null  |            null  |      0  |      0  |      0  |      0  |   0  |   0  |   0  |   0  |   0  |   0  |   0  |   0  |   0  |   0  |      0  |      0  |       null  |     NaN  |      NaN  |     NaN  |     NaN  |     NaN  |      NaN  |     NaN  |      NaN  |     NaN  |     NaN  |     NaN  |      NaN  |     NaN  |     NaN  |
  2634  |      null  |         null  |        null  |         null  |    null  |     null  |         null  |        0  |         null  |            null  |      0  |      0  |      0  |      0  |   0  |   0  |   0  |   0  |   0  |   0  |   0  |   0  |   0  |   0  |      0  |      0  |       null  |     NaN  |      NaN  |     NaN  |     NaN  |     NaN  |      NaN  |     NaN  |      NaN  |     NaN  |     NaN  |     NaN  |      NaN  |     NaN  |     NaN  |
  2635  |      null  |         null  |        null  |         null  |    null  |     null  |         null  |        0  |         null  |            null  |      0  |      0  |      0  |      0  |   0  |   0  |   0  |   0  |   0  |   0  |   0  |   0  |   0  |   0  |      0  |      0  |       null  |     NaN  |      NaN  |     NaN  |     NaN  |     NaN  |      NaN  |     NaN  |      NaN  |     NaN  |     NaN  |     NaN  |      NaN  |     NaN  |     NaN  |
</pre></div>

##### Transpose

There is one additional caveat with respect to structural immutability which involves the transpose
of a `DataFrame`. While a frame is by design a column store, when you call `transpose()` to switch
the row and column axis, it effectively becomes row major. This implementation choice was made
so that creating a transpose frame is extremely cheap, and requires almost no additional memory.

A transposed `DataFrame` is structurally immutable in both the row and column dimensions, which 
is a design choice that may be relaxed at some point in the future. If structural changes to a 
transpose is a requirement, a copy will need to be created, which will then yield a column store
data structure with the data still in its transposed state.

In the case of a frame with mixed column types, like the ATP match results, one should be aware that 
transposing it and then creating a copy will result in a frame that is much less memory efficient 
than the non-transposed original. The reason for this is that the copied transpose frame will have
column arrays of type `Object` in order to cater to the non-homogeneous types for each column, and
thereby introduce object header overhead as well as significantly higher garbage collection times.

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);
//Transpose the original DataFrame
DataFrame<String,Integer> transpose = frame.transpose();
//Print 10x5 section of the frame
transpose.left(5).out().print();
//Attempt to add a column, which we know will fail
transpose.cols().add(transpose.colCount()+1, Double.class);
```

<div class="frame"><pre class="frame">
   Index     |            0             |            1             |            2             |            3             |            4             |
-----------------------------------------------------------------------------------------------------------------------------------------------------
   Location  |                Brisbane  |                Brisbane  |                Brisbane  |                Brisbane  |                Brisbane  |
 Tournament  |  Brisbane International  |  Brisbane International  |  Brisbane International  |  Brisbane International  |  Brisbane International  |
       Date  |              2012-12-31  |              2012-12-31  |              2012-12-31  |              2012-12-31  |              2013-01-01  |
     Series  |                  ATP250  |                  ATP250  |                  ATP250  |                  ATP250  |                  ATP250  |
      Court  |                 Outdoor  |                 Outdoor  |                 Outdoor  |                 Outdoor  |                 Outdoor  |
    Surface  |                    Hard  |                    Hard  |                    Hard  |                    Hard  |                    Hard  |
      Round  |               1st Round  |               1st Round  |               1st Round  |               1st Round  |               1st Round  |
    Best of  |                       3  |                       3  |                       3  |                       3  |                       3  |
     Winner  |                Mayer F.  |             Nieminen J.  |            Nishikori K.  |            Baghdatis M.  |              Istomin D.  |
      Loser  |              Giraldo S.  |            Benneteau J.  |            Matosevic M.  |             Mitchell B.  |               Klizan M.  |
</pre></div>

<pre>
com.zavtech.morpheus.frame.DataFrameException: This DataFrame is configured as a row store, transpose() first
	at com.zavtech.morpheus.reference.XDataFrameContent.addColumn(XDataFrameContent.java:326)
	at com.zavtech.morpheus.reference.XDataFrameColumns.lambda$null$14(XDataFrameColumns.java:216)
	at java.util.LinkedHashMap$LinkedKeySet.forEach(LinkedHashMap.java:555)
	at com.zavtech.morpheus.reference.XDataFrameColumns.lambda$addColumns$15(XDataFrameColumns.java:213)
	at java.util.function.Function.lambda$andThen$6(Function.java:88)
	at java.util.function.Function.lambda$andThen$6(Function.java:88)
	at com.zavtech.morpheus.reference.XDataFrameColumns.add(XDataFrameColumns.java:88)
</pre>