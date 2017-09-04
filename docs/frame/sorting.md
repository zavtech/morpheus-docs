### Sorting

#### Introduction

The Morpheus `DataFrame` can be sorted in both the row and column dimension, either by the axis keys, or by 
values in one or more columns or rows respectively. In addition, custom sort logic is supported via a user provided
`Comparator` that consumes either `DataFrameRow` or `DataFrameColumn` objects. Finally, all sort functions are 
capable of executing in **parallel** which can lead to dramatic performance improvements on large frames, and some
results in this regard are presented in the section on performance below. In all cases, an optimized single pivot 
quick sort is used, with an implementation made available in the excellent [FastUtil](http://fastutil.di.unimi.it/) 
library, so a thank you goes out to [Sebastiano Vigna](http://vigna.di.unimi.it/).

#### Example Data

In this section, we will continue to use the ATP 2013 dataset that was introduced earlier in the filtering discussion 
[here](./filtering/#example-data). In some examples a custom frame is created of random double precision values in order to
demonstrate how to sort columns by row values. The ATP match `DataFrame` does not have homogeneous types along the rows, and 
therefore a column sort based on these values is nonsensical. In addition, the section on performance creates much larger frames
with millions of rows in order to get more measurable timing statistics.

The unsorted 2103 ATP match `DataFrame` looks as follows (at least the first 10 rows).

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
     9  |  Brisbane  |  Brisbane International  |  2013-01-01  |  ATP250  |  Outdoor  |     Hard  |  1st Round  |        3  |   Dimitrov G.  |      Baker B.  |     48  |     61  |   866  |   756  |   6  |   3  |   7  |   6  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.3600  |  3.0000  |  1.4000  |  2.8000  |  1.4400  |  2.6200  |  1.3800  |  3.3000  |  1.4000  |  2.7500  |  1.4500  |  3.5500  |  1.3900  |  2.8700  |
</pre></div>

#### Row Sorting

##### Key Sort

There are several overloaded `sort()` methods on the `DataFrameAxis` interface, the most basic of which takes a 
`boolean` to signal ascending (`true`) or descending (`false`) order. The example below illustrates how to sort the ATP
2013 match frame by descending row key order. In the unsorted frame, the row keys are monotonically increasing 
integers so the sort effectively reverses the order of the frame revealing the last matches at the top of the 
frame.

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);
//Sort rows by row keys in descending order
frame.rows().sort(false);
//Print first ten rows
frame.out().print(10);
```
<div class="frame"><pre class="frame">
 Index  |  Location  |  Tournament   |     Date     |    Series     |  Court   |  Surface  |     Round     |  Best of  |    Winner     |      Loser       |  WRank  |  LRank  |  WPts   |  LPts   |  W1  |  L1  |  W2  |  L2  |  W3  |  L3  |  W4  |  L4  |  W5  |  L5  |  Wsets  |  Lsets  |   Comment   |  B365W   |   B365L   |   EXW    |   EXL    |   LBW    |    LBL    |   PSW    |    PSL    |   SJW    |   SJL    |   MaxW   |   MaxL    |   AvgW   |   AvgL   |
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  2630  |    London  |  Masters Cup  |  2013-11-11  |  Masters Cup  |  Indoor  |     Hard  |    The Final  |        3  |  Djokovic N.  |        Nadal R.  |      2  |      1  |  10610  |  12030  |   6  |   3  |   6  |   4  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.5700  |   2.3700  |  1.5500  |  2.4000  |  1.5700  |   2.3800  |  1.5800  |   2.5800  |  1.5700  |  2.3800  |  1.6500  |   2.5800  |  1.5700  |  2.4300  |
  2629  |    London  |  Masters Cup  |  2013-11-10  |  Masters Cup  |  Indoor  |     Hard  |   Semifinals  |        3  |  Djokovic N.  |     Wawrinka S.  |      2  |      8  |  10610  |   3330  |   6  |   3  |   6  |   3  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.1400  |   5.5000  |  1.1300  |  5.5000  |  1.1400  |   5.5000  |  1.1900  |   5.6100  |  1.1500  |  5.5000  |  1.1900  |   6.3000  |  1.1600  |  5.2500  |
  2628  |    London  |  Masters Cup  |  2013-11-10  |  Masters Cup  |  Indoor  |     Hard  |   Semifinals  |        3  |     Nadal R.  |      Federer R.  |      1  |      7  |  12030  |   3805  |   7  |   5  |   6  |   3  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.3000  |   3.5000  |  1.3300  |  3.2000  |  1.3000  |   3.5000  |  1.3400  |   3.6700  |  1.3300  |  3.4000  |  1.3700  |   4.0500  |  1.3300  |  3.3400  |
  2627  |    London  |  Masters Cup  |  2013-11-09  |  Masters Cup  |  Indoor  |     Hard  |  Round Robin  |        3  |  Djokovic N.  |      Gasquet R.  |      2  |      9  |  10610  |   3300  |   7  |   6  |   4  |   6  |   6  |   3  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |  1.0600  |  10.0000  |  1.0500  |  9.0000  |  1.0500  |  10.0000  |  1.0700  |  10.7500  |  1.0500  |  9.0000  |  1.1000  |  10.7500  |  1.0600  |  9.2200  |
  2626  |    London  |  Masters Cup  |  2013-11-09  |  Masters Cup  |  Indoor  |     Hard  |  Round Robin  |        3  |   Federer R.  |  Del Potro J.M.  |      7  |      5  |   3805  |   5055  |   4  |   6  |   7  |   6  |   7  |   5  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |  2.0000  |   1.8000  |  2.0000  |  1.8000  |  2.0000  |   1.8000  |  2.0700  |   1.8500  |  2.0000  |  1.8000  |  2.1000  |   1.9500  |  1.9800  |  1.8100  |
  2625  |    London  |  Masters Cup  |  2013-11-08  |  Masters Cup  |  Indoor  |     Hard  |  Round Robin  |        3  |     Nadal R.  |      Berdych T.  |      1  |      6  |  12030  |   3980  |   6  |   4  |   1  |   6  |   6  |   3  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |  1.2000  |   4.5000  |  1.2200  |  4.0000  |  1.2000  |   4.5000  |  1.2700  |   4.2600  |  1.2200  |  4.0000  |  1.3000  |   4.6000  |  1.2300  |  4.1200  |
  2624  |    London  |  Masters Cup  |  2013-11-08  |  Masters Cup  |  Indoor  |     Hard  |  Round Robin  |        3  |  Wawrinka S.  |       Ferrer D.  |      8  |      3  |   3330  |   5800  |   6  |   7  |   6  |   4  |   6  |   1  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |  1.4400  |   2.7500  |  1.4500  |  2.7000  |  1.4400  |   2.7500  |  1.5600  |   2.6200  |  1.4400  |  2.7500  |  1.5700  |   2.8000  |  1.4800  |  2.6300  |
  2623  |    London  |  Masters Cup  |  2013-11-07  |  Masters Cup  |  Indoor  |     Hard  |  Round Robin  |        3  |  Djokovic N.  |  Del Potro J.M.  |      2  |      5  |  10610  |   5055  |   6  |   3  |   3  |   6  |   6  |   3  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |  1.2800  |   3.7500  |  1.2800  |  3.5000  |  1.3000  |   3.5000  |  1.3000  |   3.8900  |  1.2900  |  3.7500  |  1.3300  |   4.0500  |  1.2800  |  3.6100  |
  2622  |    London  |  Masters Cup  |  2013-11-07  |  Masters Cup  |  Indoor  |     Hard  |  Round Robin  |        3  |   Federer R.  |      Gasquet R.  |      7  |      9  |   3805  |   3300  |   6  |   4  |   6  |   3  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.2800  |   3.7500  |  1.3000  |  3.4000  |  1.2900  |   3.7500  |  1.3400  |   3.6000  |  1.3000  |  3.5000  |  1.3500  |   4.2000  |  1.2900  |  3.5900  |
  2621  |    London  |  Masters Cup  |  2013-11-06  |  Masters Cup  |  Indoor  |     Hard  |  Round Robin  |        3  |   Berdych T.  |       Ferrer D.  |      6  |      3  |   3980  |   5800  |   6  |   4  |   6  |   4  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  2.1000  |   1.7200  |  2.0000  |  1.8000  |  2.0000  |   1.8000  |  2.0600  |   1.8500  |  2.0000  |  1.8000  |  2.2000  |   1.8500  |  2.0300  |  1.7800  |
</pre></div>

##### Value Sort

A more common scenario may be to sort rows according to values in a specific column. Any data type that implements the
`Comparable` interface can be sorted in this way (the same applies to sorting by keys as described in the previous section). 
If the `DataFrame` happens to have a column of data whose type does not implement `Comparable` and sorting according to
this data is required, a user provided `Comparator` will have to be applied, which is discussed later.

The example below demonstrates how to sort the 2013 ATP match results by the rank of the eventual winner of the match (`WRank`). 
Oddly, there appears to be a player ranked zero (namely Khachanov K.), and thus he appears first. Beyond this anomaly, you find 
the expected names in the Winner column, namely Djokovic and Nadal.

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);
//Sort rows by the WRank (winner rank) column values
frame.rows().sort(true, "WRank");
//Print first ten rows
frame.out().print(10);
```
<div class="frame"><pre class="frame">
 Index  |     Location     |          Tournament           |     Date     |     Series     |   Court   |  Surface  |      Round      |  Best of  |     Winner     |      Loser       |  WRank  |  LRank  |  WPts   |  LPts  |  W1  |  L1  |  W2  |  L2  |  W3  |  L3  |  W4  |  L4  |  W5  |  L5  |  Wsets  |  Lsets  |   Comment   |  B365W   |   B365L   |   EXW    |    EXL    |   LBW    |    LBL    |   PSW    |    PSL    |   SJW    |    SJL    |   MaxW   |   MaxL    |   AvgW   |   AvgL    |
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  2224  |  St. Petersburg  |          St. Petersburg Open  |  2013-09-16  |        ATP250  |   Indoor  |     Hard  |      1st Round  |        3  |  Khachanov K.  |      Hanescu V.  |      0  |     63  |      0  |   771  |   7  |   6  |   7  |   6  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  7.0000  |   1.1000  |  5.2500  |   1.1200  |  5.5000  |   1.1200  |  7.7400  |   1.1100  |  5.5000  |   1.1400  |  8.2000  |   1.1500  |  6.1400  |   1.1200  |
   152  |       Melbourne  |              Australian Open  |  2013-01-14  |    Grand Slam  |  Outdoor  |     Hard  |      1st Round  |        5  |   Djokovic N.  |    Mathieu P.H.  |      1  |     60  |  12920  |   763  |   6  |   2  |   6  |   4  |   7  |   5  |   0  |   0  |   0  |   0  |      3  |      0  |  Completed  |  1.0020  |  29.0000  |  1.0200  |  15.0000  |  1.0200  |  13.0000  |  1.0100  |  41.0000  |  1.0100  |  19.0000  |  1.0200  |  46.0000  |  1.0100  |  21.7000  |
  1121  |            Rome  |  Internazionali BNL d'Italia  |  2013-05-14  |  Masters 1000  |  Outdoor  |     Clay  |      2nd Round  |        3  |   Djokovic N.  |     Montanes A.  |      1  |     89  |  12730  |   573  |   6  |   2  |   6  |   3  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.0100  |  21.0000  |  1.0200  |  12.0000  |  1.0200  |  13.0000  |  1.0500  |  13.0000  |  1.0200  |  13.0000  |  1.0500  |  24.5000  |  1.0200  |  14.5500  |
  1137  |            Rome  |  Internazionali BNL d'Italia  |  2013-05-16  |  Masters 1000  |  Outdoor  |     Clay  |      3rd Round  |        3  |   Djokovic N.  |   Dolgopolov O.  |      1  |     23  |  12730  |  1420  |   6  |   1  |   6  |   4  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.0600  |  10.0000  |  1.0800  |   7.0000  |  1.0600  |   9.0000  |  1.0800  |  10.7500  |  1.0800  |   7.5000  |  1.1000  |  11.5000  |  1.0700  |   8.5500  |
  1592  |          London  |                    Wimbledon  |  2013-07-03  |    Grand Slam  |  Outdoor  |    Grass  |  Quarterfinals  |        5  |   Djokovic N.  |      Berdych T.  |      1  |      6  |  11830  |  4515  |   7  |   6  |   6  |   4  |   6  |   3  |   0  |   0  |   0  |   0  |      3  |      0  |  Completed  |  1.1000  |   7.0000  |  1.1200  |   6.0000  |  1.1200  |   6.0000  |  1.1300  |   7.9400  |     NaN  |      NaN  |  1.1500  |   7.9400  |  1.1200  |   6.4100  |
   704  |    Indian Wells  |             BNP Paribas Open  |  2013-03-15  |  Masters 1000  |  Outdoor  |     Hard  |  Quarterfinals  |        3  |   Djokovic N.  |     Tsonga J.W.  |      1  |      8  |  13280  |  3660  |   6  |   3  |   6  |   1  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.0800  |   8.0000  |  8.0000  |   6.7100  |  1.1100  |   6.5000  |  1.1100  |   8.5000  |  1.1300  |   6.5000  |  1.1400  |   8.7000  |  1.1000  |   6.8800  |
  1596  |          London  |                    Wimbledon  |  2013-07-05  |    Grand Slam  |  Outdoor  |    Grass  |     Semifinals  |        5  |   Djokovic N.  |  Del Potro J.M.  |      1  |      8  |  11830  |  3960  |   7  |   5  |   4  |   6  |   7  |   6  |   6  |   7  |   6  |   3  |      3  |      2  |  Completed  |  1.1600  |   5.0000  |  1.1200  |   6.0000  |  1.1400  |   5.5000  |  1.1600  |   6.6000  |  1.1400  |   6.0000  |  1.2000  |   6.6000  |  1.1400  |   5.6200  |
   674  |    Indian Wells  |             BNP Paribas Open  |  2013-03-11  |  Masters 1000  |  Outdoor  |     Hard  |      2nd Round  |        3  |   Djokovic N.  |      Fognini F.  |      1  |     36  |  13280  |  1065  |   6  |   0  |   5  |   7  |   6  |   2  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |  1.0020  |  29.0000  |  1.0100  |  15.0000  |  1.0100  |  17.0000  |  1.0100  |  33.8900  |  1.0100  |  15.0000  |  1.0400  |  41.0000  |  1.0100  |  20.1700  |
   690  |    Indian Wells  |             BNP Paribas Open  |  2013-03-12  |  Masters 1000  |  Outdoor  |     Hard  |      3rd Round  |        3  |   Djokovic N.  |     Dimitrov G.  |      1  |     31  |  13280  |  1137  |   7  |   6  |   6  |   1  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.0200  |  17.0000  |  1.0300  |  10.0000  |  1.0400  |   9.0000  |  1.0200  |  23.0000  |  1.0500  |   9.0000  |  1.0500  |  23.0000  |  1.0300  |  12.7200  |
  2611  |           Paris  |          BNP Paribas Masters  |  2013-11-01  |  Masters 1000  |   Indoor  |     Hard  |  Quarterfinals  |        3  |      Nadal R.  |      Gasquet R.  |      1  |     10  |  11670  |  3130  |   6  |   4  |   6  |   1  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.1400  |   5.5000  |  1.1300  |   5.5000  |  1.1400  |   5.5000  |  1.2000  |   5.3700  |  1.1400  |   5.5000  |  1.2000  |   6.3000  |  1.1500  |   5.2400  |
</pre></div>

##### Multidimensional Sort

The `DataFrameAxis` interface also supports a **multidimensional** sort where the row order is first sorted by the values in one column 
and then by a second, third and so on. The example below again uses the 2013 ATP match results to sort first by the match `Date` column 
(because we expect to find many matches occurring on the same day), and then subsequently by the rank of the eventual winner (`WRank`). 
The output below shows the first 10 rows of the sorted frame, with the earliest matches occurring on 2012-10-01 (it appears the ATP tour 
does not follow a calendar year), followed by a WRank sort which is also ascending.

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);
//Multidimensional row sort (ascending) first by Date, then WRank
frame.rows().sort(true, Collect.asList("Date", "WRank"));
//Print first ten rows
frame.out().print(10);
```
<div class="frame"><pre class="frame">
 Index  |  Location  |                Tournament                 |     Date     |  Series  |   Court   |  Surface  |    Round    |  Best of  |       Winner       |     Loser     |  WRank  |  LRank  |  WPts  |  LPts  |  W1  |  L1  |  W2  |  L2  |  W3  |  L3  |  W4  |  L4  |  W5  |  L5  |  Wsets  |  Lsets  |   Comment   |  B365W   |  B365L   |   EXW    |   EXL    |   LBW    |   LBL    |   PSW    |   PSL    |   SJW    |   SJL    |   MaxW   |   MaxL   |   AvgW   |   AvgL   |
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  2342  |     Tokyo  |  Rakuten Japan Open Tennis Championships  |  2012-10-01  |  ATP500  |  Outdoor  |     Hard  |  1st Round  |        3  |       Tsonga J.W.  |   Monfils G.  |      8  |     42  |  3325  |  1030  |   6  |   3  |   7  |   6  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.4000  |  2.7500  |  1.4200  |  2.6500  |  1.4400  |  2.6200  |  1.5300  |  2.6600  |  1.4400  |  2.7500  |  1.5500  |  2.8500  |  1.4700  |  2.6300  |
  2315  |   Beijing  |                               China Open  |  2012-10-01  |  ATP500  |  Outdoor  |     Hard  |  1st Round  |        3  |        Gasquet R.  |     Mayer F.  |     10  |     41  |  3005  |  1030  |   6  |   3  |   7  |   6  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.2500  |  3.7500  |  1.2800  |  3.3000  |  1.3000  |  3.4000  |  1.3600  |  3.4300  |  1.2900  |  3.5000  |  1.3600  |  3.8000  |  1.2900  |  3.4600  |
  2340  |     Tokyo  |  Rakuten Japan Open Tennis Championships  |  2012-10-01  |  ATP500  |  Outdoor  |     Hard  |  1st Round  |        3  |        Almagro N.  |    Becker B.  |     17  |     73  |  1940  |   672  |   7  |   6  |   7  |   6  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.6100  |  2.2000  |  1.6200  |  2.1500  |  1.6700  |  2.1000  |  1.5700  |  2.5400  |  1.6200  |  2.2500  |  1.7100  |  2.5500  |  1.6000  |  2.2800  |
  2310  |   Beijing  |                               China Open  |  2012-10-01  |  ATP500  |  Outdoor  |     Hard  |  1st Round  |        3  |        Fognini F.  |   Robredo T.  |     19  |     18  |  1840  |  1855  |   7  |   5  |   4  |   6  |   6  |   3  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |  3.0000  |  1.3600  |  2.6500  |  1.4200  |  2.6200  |  1.4400  |  2.9300  |  1.4500  |  2.7500  |  1.4400  |  3.6500  |  1.4600  |  2.7900  |  1.4200  |
  2312  |   Beijing  |                               China Open  |  2012-10-01  |  ATP500  |  Outdoor  |     Hard  |  1st Round  |        3  |  Kohlschreiber P.  |  Montanes A.  |     25  |     58  |  1445  |   836  |   7  |   5  |   1  |   6  |   7  |   6  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |  1.1600  |  5.0000  |  1.1500  |  4.7500  |  1.1700  |  4.5000  |  1.1700  |  5.6500  |  1.1800  |  4.5000  |  1.2200  |  5.6500  |  1.1700  |  4.7500  |
  2314  |   Beijing  |                               China Open  |  2012-10-01  |  ATP500  |  Outdoor  |     Hard  |  1st Round  |        3  |        Querrey S.  |   Youzhny M.  |     30  |     20  |  1265  |  1780  |   7  |   6  |   6  |   3  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  2.3700  |  1.5300  |  2.3000  |  1.5500  |  2.2500  |  1.5700  |  2.7000  |  1.5200  |  2.3800  |  1.5700  |  2.7500  |  1.6200  |  2.4900  |  1.5100  |
  2341  |     Tokyo  |  Rakuten Japan Open Tennis Championships  |  2012-10-01  |  ATP500  |  Outdoor  |     Hard  |  1st Round  |        3  |     Dolgopolov O.  |    Brands D.  |     39  |     61  |  1080  |   813  |   6  |   3  |   6  |   1  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.5000  |  2.5000  |  1.5000  |  2.4000  |  1.5000  |  2.5000  |  1.5300  |  2.6600  |  1.5700  |  2.3800  |  1.6000  |  2.6600  |  1.5200  |  2.4700  |
  2343  |     Tokyo  |  Rakuten Japan Open Tennis Championships  |  2012-10-01  |  ATP500  |  Outdoor  |     Hard  |  1st Round  |        3  |       Nieminen J.  |    Monaco J.  |     44  |     36  |   980  |  1115  |   7  |   6  |   6  |   1  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.6100  |  2.2000  |  1.6000  |  2.2000  |  1.6200  |  2.2000  |  1.7000  |  2.2700  |  1.6200  |  2.2500  |  1.7300  |  2.3800  |  1.6400  |  2.2000  |
  2309  |   Beijing  |                               China Open  |  2012-10-01  |  ATP500  |  Outdoor  |     Hard  |  1st Round  |        3  |          Tomic B.  |     Zhang Z.  |     55  |    193  |   855  |   251  |   7  |   6  |   6  |   4  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.1400  |  5.0000  |  1.1500  |  4.7500  |  1.1700  |  4.5000  |  1.1800  |  5.4700  |  1.1400  |  5.5000  |  1.2200  |  5.5000  |  1.1600  |  4.9500  |
  2313  |   Beijing  |                               China Open  |  2012-10-01  |  ATP500  |  Outdoor  |     Hard  |  1st Round  |        3  |         Hewitt L.  |      Haas T.  |     59  |     12  |   825  |  2265  |   7  |   6  |   6  |   3  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  2.3700  |  1.5300  |  2.3000  |  1.5500  |  2.2500  |  1.5700  |  2.4100  |  1.6200  |  2.3800  |  1.5700  |  2.5000  |  1.6200  |  2.3500  |  1.5700  |
</pre></div>

##### Comparator Sort

If the two row sort mechanisms discussed above are functionally insufficient for your needs, a final `sort()` method 
on the `DataFrameAxis` interface is provided which accepts a user implemented `Comparator`, allowing for sorting logic 
of arbitrary complexity. 

Depending on the implementation of the `Comparator`, performance may not be quite as good as the two prior use cases as 
those mechanisms can leverage private access to the internal data structures of the frame. If that proves to be the case, 
using parallel sort on a multi-core machine is likely to boost performance significantly (see section below which provides
some hard numbers on this topic).

The example below shows how to sort the 2013 ATP match results by a derived value (i.e. one where a column representing
the sort quantity does not exist on the frame). In this setup we apply a row sort such that rows that have the smallest 
absolute difference between the betting odds on the eventual winner (`AvgW`) and eventual loser (`AvgL`) appear first.
This helps identity matches that as far as the bookmakers are concerned, are simply too close to call.

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);
//Sort rows so that matches smallest difference in betting odds between winner and looser.
frame.rows().sort((row1, row2) -> {
    double diff1 = Math.abs(row1.getDouble("AvgW") - row1.getDouble("AvgL"));
    double diff2 = Math.abs(row2.getDouble("AvgW") - row2.getDouble("AvgL"));
    return Double.compare(diff1, diff2);
});
//Print first ten rows
frame.out().print(10);
```
<div class="frame"><pre class="frame">
 Index  |    Location     |                   Tournament                   |     Date     |     Series     |   Court   |  Surface  |      Round      |  Best of  |       Winner       |      Loser      |  WRank  |  LRank  |  WPts  |  LPts  |  W1  |  L1  |  W2  |  L2  |  W3  |  L3  |  W4  |  L4  |  W5  |  L5  |  Wsets  |  Lsets  |   Comment   |  B365W   |  B365L   |   EXW    |   EXL    |   LBW    |   LBL    |   PSW    |   PSL    |   SJW    |   SJL    |   MaxW   |   MaxL   |   AvgW   |   AvgL   |
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
   640  |   Indian Wells  |                              BNP Paribas Open  |  2013-03-09  |  Masters 1000  |  Outdoor  |     Hard  |      1st Round  |        3  |          Blake J.  |       Haase R.  |     99  |     47  |   538  |   845  |   6  |   3  |   6  |   4  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.8300  |  1.8300  |  1.9000  |  1.8500  |  1.8300  |  1.8300  |  1.9300  |  1.9700  |  1.8300  |  1.9100  |  2.0800  |  1.9700  |  1.8800  |  1.8700  |
  1981  |     Cincinnati  |    Western & Southern Financial Group Masters  |  2013-08-13  |  Masters 1000  |  Outdoor  |     Hard  |      1st Round  |        3  |           Haas T.  |    Anderson K.  |     13  |     19  |  2140  |  1740  |   6  |   4  |   6  |   4  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.9000  |  1.8000  |  1.8500  |  1.8500  |  1.9100  |  1.8000  |  1.9200  |  1.9900  |  1.9100  |  1.9100  |  1.9700  |  2.0400  |  1.8700  |  1.8800  |
  2050  |  Winston-Salem  |  Winston-Salem Open at Wake Forest University  |  2013-08-20  |        ATP250  |  Outdoor  |     Hard  |      2nd Round  |        3  |         Melzer J.  |   De Bakker T.  |     32  |    101  |  1220  |   547  |   7  |   5  |   6  |   7  |   4  |   2  |   0  |   0  |   0  |   0  |      1  |      1  |    Retired  |  1.8300  |  1.8300  |  1.8500  |  1.8500  |  1.8300  |  1.8300  |  1.9300  |  1.9800  |  1.8300  |  1.9100  |  1.9300  |  1.9800  |  1.8700  |  1.8800  |
   341  |         Zagreb  |                            PBZ Zagreb Indoors  |  2013-02-08  |        ATP250  |   Indoor  |     Hard  |  Quarterfinals  |        3  |          Haase R.  |  Petzschner P.  |     58  |    122  |   755  |   436  |   6  |   4  |   3  |   6  |   6  |   0  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |  1.8300  |  1.8300  |  1.8500  |  1.9000  |  1.8300  |  1.8300  |  1.9500  |  1.9700  |  1.8000  |  2.0000  |  2.0000  |  2.1000  |  1.8700  |  1.8800  |
   937  |      Barcelona  |                           Open Banco Sabadell  |  2013-04-24  |        ATP500  |  Outdoor  |     Clay  |      2nd Round  |        3  |         Klizan M.  |    Montanes A.  |     31  |     80  |  1205  |   630  |   6  |   3  |   6  |   4  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.8300  |  1.8300  |  1.8500  |  1.9000  |  1.7300  |  2.0000  |  2.0400  |  1.8700  |  1.8000  |  2.0000  |  2.0400  |  2.0000  |  1.8800  |  1.8700  |
  2545  |       Valencia  |                             Valencia Open 500  |  2013-10-22  |        ATP500  |   Indoor  |     Hard  |      1st Round  |        3  |      Benneteau J.  |       Lopez F.  |     33  |     30  |  1195  |  1275  |   6  |   3  |   6  |   1  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.9000  |  1.8000  |  1.9000  |  1.8500  |  1.9100  |  1.8000  |  1.9900  |  1.9000  |  1.9000  |  1.9000  |  2.0400  |  1.9500  |  1.8800  |  1.8700  |
   729  |          Miami  |                            Sony Ericsson Open  |  2013-03-21  |  Masters 1000  |  Outdoor  |     Hard  |      1st Round  |        3  |          Falla A.  |       Soeda G.  |     60  |     83  |   776  |   600  |   7  |   5  |   6  |   1  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.8000  |  1.9000  |  1.8500  |  1.9000  |  1.8300  |  1.8300  |  1.9100  |  2.0000  |  2.0000  |  1.8000  |  2.0000  |  2.0500  |  1.8800  |  1.8700  |
   858  |    Monte Carlo  |                           Monte Carlo Masters  |  2013-04-14  |  Masters 1000  |  Outdoor  |     Clay  |      1st Round  |        3  |  Kohlschreiber P.  |    Bellucci T.  |     21  |     39  |  1670  |   987  |   6  |   4  |   6  |   2  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.8300  |  1.8300  |  1.9000  |  1.8500  |  1.9100  |  1.8000  |  1.9300  |  1.9700  |  1.9000  |  1.9000  |  2.0000  |  2.0000  |  1.8700  |  1.8800  |
  2000  |     Cincinnati  |    Western & Southern Financial Group Masters  |  2013-08-14  |  Masters 1000  |  Outdoor  |     Hard  |      2nd Round  |        3  |          Isner J.  |     Gasquet R.  |     22  |     11  |  1585  |  2625  |   7  |   6  |   6  |   2  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.8000  |  1.9000  |  1.8500  |  1.8500  |  1.7300  |  2.0000  |  2.0200  |  1.8700  |  1.9100  |  1.8300  |  2.0200  |  2.0800  |  1.8700  |  1.8800  |
   632  |   Indian Wells  |                              BNP Paribas Open  |  2013-03-08  |  Masters 1000  |  Outdoor  |     Hard  |      1st Round  |        3  |      Matosevic M.  |     Robredo T.  |     53  |     69  |   813  |   680  |   7  |   5  |   6  |   2  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.8300  |  1.8300  |  1.8500  |  1.9000  |  1.8300  |  1.8300  |  1.9300  |  1.9800  |  1.8300  |  1.9100  |  1.9500  |  2.1000  |  1.8800  |  1.8700  |
</pre></div>

#### Column Sorting

##### Key Sort

The Morpheus API is almost entirely symmetrical in the row and column dimension, so the sorting mechanisms presented 
in the previous sections can also be used to sort columns. The first code example below sorts the columns of the 2013 
ATP match results in ascending order by calling the `sort(ascending=true)` method on the `DataFrameAxis` returned 
from `DataFrame.cols()`.

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);
//Sort columns by column keys in ascending order
frame.cols().sort(true);
//Print first ten rows
frame.out().print(10);
```

<div class="frame"><pre class="frame">
 Index  |   AvgL   |   AvgW   |  B365L   |  B365W   |  Best of  |   Comment   |   Court   |     Date     |   EXL    |   EXW    |  L1  |  L2  |  L3  |  L4  |  L5  |   LBL    |   LBW    |  LPts  |  LRank  |  Location  |     Loser      |  Lsets  |   MaxL   |   MaxW   |   PSL    |   PSW    |    Round    |   SJL    |   SJW    |  Series  |  Surface  |        Tournament        |  W1  |  W2  |  W3  |  W4  |  W5  |  WPts  |  WRank  |     Winner     |  Wsets  |
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     0  |  2.7800  |  1.4200  |  3.0000  |  1.3600  |        3  |  Completed  |  Outdoor  |  2012-12-31  |  2.6500  |  1.4500  |   4  |   4  |   0  |   0  |   0  |  2.6200  |  1.4400  |   778  |     57  |  Brisbane  |    Giraldo S.  |      0  |  3.2000  |  1.4700  |  2.8500  |  1.4700  |  1st Round  |  2.6300  |  1.4400  |  ATP250  |     Hard  |  Brisbane International  |   6  |   6  |   0  |   0  |   0  |  1215  |     28  |      Mayer F.  |      2  |
     1  |  2.0500  |  1.7300  |  2.2000  |  1.6100  |        3  |  Completed  |  Outdoor  |  2012-12-31  |  2.0000  |  1.7500  |   3  |   6  |   1  |   0  |   0  |  1.9100  |  1.8000  |  1075  |     35  |  Brisbane  |  Benneteau J.  |      1  |  2.2600  |  1.8000  |  2.1000  |  1.8000  |  1st Round  |  2.0000  |  1.7300  |  ATP250  |     Hard  |  Brisbane International  |   6  |   2  |   6  |   0  |   0  |   927  |     41  |   Nieminen J.  |      2  |
     2  |  3.5800  |  1.2800  |  3.7500  |  1.2500  |        3  |  Completed  |  Outdoor  |  2012-12-31  |  3.7500  |  1.2500  |   5  |   2  |   0  |   0  |   0  |  3.5000  |  1.2900  |   845  |     49  |  Brisbane  |  Matosevic M.  |      0  |  4.2000  |  1.3000  |  3.8500  |  1.3000  |  1st Round  |  3.2000  |  1.3000  |  ATP250  |     Hard  |  Brisbane International  |   7  |   6  |   0  |   0  |   0  |  1830  |     19  |  Nishikori K.  |      2  |
     3  |  7.7600  |  1.0800  |  9.0000  |  1.0700  |        3  |  Completed  |  Outdoor  |  2012-12-31  |  8.0000  |  1.0600  |   4  |   4  |   0  |   0  |   0  |  7.0000  |  1.0800  |   137  |    326  |  Brisbane  |   Mitchell B.  |      0  |  9.5000  |  1.1000  |  9.4300  |  1.0800  |  1st Round  |  7.0000  |  1.0700  |  ATP250  |     Hard  |  Brisbane International  |   6  |   6  |   0  |   0  |   0  |  1070  |     36  |  Baghdatis M.  |      2  |
     4  |  1.8500  |  1.8800  |  1.8000  |  1.9000  |        3  |  Completed  |  Outdoor  |  2013-01-01  |  1.8700  |  1.8700  |   1  |   2  |   0  |   0  |   0  |  1.8000  |  1.9100  |  1175  |     30  |  Brisbane  |     Klizan M.  |      0  |  2.0000  |  2.0500  |  2.0000  |  1.8800  |  1st Round  |  1.8000  |  1.9100  |  ATP250  |     Hard  |  Brisbane International  |   6  |   6  |   0  |   0  |   0  |   897  |     43  |    Istomin D.  |      2  |
     5  |  2.0800  |  1.7100  |  2.2000  |  1.6100  |        3  |  Completed  |  Outdoor  |  2013-01-01  |  2.0000  |  1.7500  |   4  |   1  |   0  |   0  |   0  |  2.0000  |  1.7300  |   655  |     79  |  Brisbane  |        Ito T.  |      0  |  2.2800  |  1.8500  |  2.2700  |  1.7000  |  1st Round  |  1.9100  |  1.8000  |  ATP250  |     Hard  |  Brisbane International  |   6  |   6  |   0  |   0  |   0  |   239  |    199  |    Millman J.  |      2  |
     6  |  1.7000  |  2.0800  |  1.6100  |  2.2000  |        3  |  Completed  |  Outdoor  |  2013-01-01  |  1.6700  |  2.0800  |   1  |   6  |   0  |   0  |   0  |  1.8000  |  1.9100  |   530  |    104  |  Brisbane  |     Levine J.  |      0  |  1.8300  |  2.3200  |  1.7000  |  2.2600  |  1st Round  |  1.7300  |  2.0000  |  ATP250  |     Hard  |  Brisbane International  |   6  |   7  |   0  |   0  |   0  |   809  |     54  |      Falla A.  |      2  |
     7  |  2.4600  |  1.5200  |  2.6200  |  1.4400  |        3  |  Completed  |  Outdoor  |  2013-01-01  |  2.3500  |  1.5500  |   6  |   4  |   4  |   0  |   0  |  2.6200  |  1.4400  |   402  |    137  |  Brisbane  |      Kudla D.  |      1  |  2.8200  |  1.6300  |  2.4700  |  1.6000  |  1st Round  |  2.5000  |  1.5000  |  ATP250  |     Hard  |  Brisbane International  |   2  |   6  |   6  |   0  |   0  |  1177  |     29  |     Melzer J.  |      2  |
     8  |  1.4700  |  2.6600  |  1.3600  |  3.0000  |        3  |  Completed  |  Outdoor  |  2013-01-01  |  1.5000  |  2.5000  |   4  |   6  |   0  |   0  |   0  |  1.5300  |  2.3800  |   710  |     69  |  Brisbane  |   Harrison R.  |      0  |  1.5300  |  3.2500  |  1.4500  |  2.9300  |  1st Round  |  1.5000  |  2.5000  |  ATP250  |     Hard  |  Brisbane International  |   6  |   7  |   0  |   0  |   0  |   495  |    114  |    Robredo T.  |      2  |
     9  |  2.8700  |  1.3900  |  3.0000  |  1.3600  |        3  |  Completed  |  Outdoor  |  2013-01-01  |  2.8000  |  1.4000  |   3  |   6  |   0  |   0  |   0  |  2.6200  |  1.4400  |   756  |     61  |  Brisbane  |      Baker B.  |      0  |  3.5500  |  1.4500  |  3.3000  |  1.3800  |  1st Round  |  2.7500  |  1.4000  |  ATP250  |     Hard  |  Brisbane International  |   6  |   7  |   0  |   0  |   0  |   866  |     48  |   Dimitrov G.  |      2  |
</pre></div>

##### Value Sort

To sort columns according to values in one or more rows, the values need to be of a homogeneous type. It doesn't make
much sense to sort the 2013 ATP match results in this way, and in fact you will get a `ClassCastException` when a `String`
is compared to a `Double` value by calling its `compareTo()` method. You could of course implement a custom `Comparator`
and sort the columns based on the `toString()` of all the values, but it's hard to see what the value is in that.

To demonstrate a column sort based on row data, the following example creates a `10x10` frame initialized with random double 
precision values. Given the homogeneity of the types along the rows in this frame, a column sort is a reasonable proposition. 
The column keys in the unsorted frame are `C0`, `C1`, `C2` to `C9`, and the new order post sort is shown below. 

<?prettify?>
```java
//Create a 10x10 frame initialized with random doubles
DataFrame<String,String> frame = DataFrame.ofDoubles(
    Range.of(0, 10).map(i -> "R" + i),
    Range.of(0, 10).map(i -> "C" + i), 
    value -> Math.random() * 100d
);
//Sort columns by the data in first row in ascending order
frame.cols().sort(true, "R0");
//Print first ten rows
frame.out().print(10);
```
<div class="frame"><pre class="frame">
 Index  |      C3       |      C1       |      C8       |      C7       |      C4       |      C6       |      C9       |      C2       |      C0       |      C5       |
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    R0  |  11.70306561  |  29.51025702  |  44.56451853  |  49.14276083  |  49.30009688  |  49.66602735  |  59.72386052  |  64.69244099  |  83.13030538  |  98.60728081  |
    R1  |  96.31980655  |  85.87996651  |  79.06930692  |  48.08819333  |  84.33796355  |   8.83992696  |  73.19731538  |  65.38285662  |  71.82216915  |   80.3000506  |
    R2  |  49.53677506  |   9.45207021  |   1.54748873  |  57.99794713  |  20.02291114  |   8.96502162  |   46.1696664  |  77.33926623  |  34.21577299  |  94.85120102  |
    R3  |   46.3516546  |  74.97630129  |  73.49083701  |  27.68089003  |  33.72130957  |  52.23839606  |  65.37188146  |  48.36073597  |  51.23686989  |  95.92785717  |
    R4  |  72.01951644  |  39.75814373  |  69.23633177  |  46.96902638  |  70.82327915  |  91.53783289  |  57.33616312  |  38.94393019  |   1.68280501  |   87.2170405  |
    R5  |  62.49904384  |   7.52041005  |  34.26879199  |  47.23321809  |   99.3061582  |  98.79472512  |    4.5477916  |  65.23264192  |   3.96089302  |  24.99717992  |
    R6  |  42.63425518  |  26.61492898  |  16.50642183  |  15.04284983  |   67.1590594  |  44.72721238  |  77.61992004  |  53.96632198  |  60.65017717  |  31.01324133  |
    R7  |   76.7178214  |   11.1839268  |  93.51531804  |  66.05863202  |  46.75806412  |   7.82498305  |  17.40931894  |  94.65796849  |  55.14862369  |  28.87315085  |
    R8  |   48.7670682  |   1.44288709  |  51.16369446  |   32.3675847  |  93.95447717  |  10.09004133  |    6.6242657  |  69.63114684  |  37.92679795  |    2.1543318  |
    R9  |  42.25961018  |  55.62679387  |   1.01423147  |  22.75711435  |  85.05398313  |  92.11731992  |  79.68760819  |  29.04198229  |  97.24438511  |  17.33341345  |
</pre></div>

We can of course sort by data in both the row and column dimension as follows:

<?prettify?>
```java
//Create a 10x10 frame initialized with random doubles
DataFrame<String,String> frame = DataFrame.ofDoubles(
    Range.of(0, 10).map(i -> "R" + i),
    Range.of(0, 10).map(i -> "C" + i),
    value -> Math.random() * 100d
);
//Sort columns by the data in first row
frame.cols().sort(true, "R0");
//Sort rows by the data that is now in the first column
frame.rows().sort(true, frame.cols().key(0));
//Print first ten rows
frame.out().print(10);
```

<div class="frame"><pre class="frame">
 Index  |      C8       |      C2       |      C1       |      C6       |      C7       |      C9       |      C4       |      C3       |      C0       |      C5       |
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    R6  |  10.79109008  |   98.6524288  |  84.86273268  |  94.70606475  |  53.38375256  |  61.10043302  |  15.12273981  |  69.89835361  |  57.66658584  |  57.27005464  |
    R0  |  11.43931147  |  20.54256763  |  23.79181572  |  27.47288193  |  29.95263252  |  75.42157524  |  76.42720119  |  77.22820182  |  77.67104703  |  95.18532196  |
    R5  |  36.90300534  |  37.55942619  |  73.84924663  |  96.27509348  |  89.51587605  |  56.83564479  |   8.43705948  |  65.56679789  |  26.18719945  |  19.35039888  |
    R8  |  48.92084538  |  74.04830396  |  15.13429492  |  79.55867974  |  69.78011977  |  92.60505236  |   0.90437748  |  66.04955254  |   4.24454634  |  16.08194424  |
    R1  |  49.72933364  |  23.54721145  |  13.76578354  |  36.82903111  |  89.45754038  |  88.36190839  |  88.35626403  |   57.3339813  |   5.07104083  |   64.8812482  |
    R3  |  50.05896919  |  20.99271329  |   19.3462031  |  84.85646161  |  18.70819105  |  36.02397058  |  39.60181063  |  97.98311929  |  23.15751737  |  11.19755199  |
    R2  |  70.91560241  |  48.22161882  |  36.04857549  |  17.43486886  |  53.59280303  |  24.17841927  |   7.32760917  |    93.077888  |  95.69590224  |   70.1948771  |
    R7  |  71.66128613  |  25.04195311  |  25.24586372  |  45.55143352  |   1.95783573  |  56.47302595  |  84.33080973  |  47.08666346  |  21.68500396  |  63.38464116  |
    R9  |   72.5569825  |   3.69441849  |  40.53699445  |  58.65763148  |   3.58546753  |  73.53537208  |   7.97752157  |  15.53034669  |    0.8829291  |  44.36481597  |
    R4  |  75.16120178  |  42.44021922  |  72.00614468  |  79.09004161  |   4.43077647  |  43.89845937  |  44.84293408  |  62.85029955  |  77.98911497  |  48.24598168  |
</pre></div>

#### Filters

Slicing or filtering a `DataFrame` is a common operation and is discussed in some detail [here](./filtering). There are 
structural immutability constraints on filtered frames, however sorting in both the row and column dimensions is 
supported.

The example below first filters the 2013 ATP match results so as to capture all rows in which Novak Djokovic was the victor, 
and then sorts these rows in ascending order according to the rank of the match loser (`LRank`). In that way, the highest
rank players he beat appear first, so it is no surprise to see names like Nadal, Murray, Federer and so on.

<?prettify?>
```java
DataFrame<Integer,String> frame = loadTennisMatchData(2013);
//First filter frame to include only rows where Novak Djokovic was the victor
DataFrame<Integer,String> filter = frame.rows().select(row -> row.getValue("Winner").equals("Djokovic N."));
//Sort rows so that the highest rank players he beat come first
filter.rows().sort(true, "LRank");
//Print first ten rows
filter.out().print(10);
```
<div class="frame"><pre class="frame">
 Index  |   Location    |          Tournament          |     Date     |     Series     |   Court   |  Surface  |     Round     |  Best of  |    Winner     |      Loser       |  WRank  |  LRank  |  WPts   |  LPts   |  W1  |  L1  |  W2  |  L2  |  W3  |  L3  |  W4  |  L4  |  W5  |  L5  |  Wsets  |  Lsets  |   Comment   |  B365W   |  B365L   |   EXW    |   EXL    |   LBW    |   LBL    |   PSW    |   PSL    |   SJW    |   SJL    |   MaxW   |   MaxL   |   AvgW   |   AvgL   |
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  2630  |       London  |                 Masters Cup  |  2013-11-11  |   Masters Cup  |   Indoor  |     Hard  |    The Final  |        3  |  Djokovic N.  |        Nadal R.  |      2  |      1  |  10610  |  12030  |   6  |   3  |   6  |   4  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.5700  |  2.3700  |  1.5500  |  2.4000  |  1.5700  |  2.3800  |  1.5800  |  2.5800  |  1.5700  |  2.3800  |  1.6500  |  2.5800  |  1.5700  |  2.4300  |
  2339  |      Beijing  |                  China Open  |  2012-10-07  |        ATP500  |  Outdoor  |     Hard  |    The Final  |        3  |  Djokovic N.  |        Nadal R.  |      1  |      2  |  11120  |  10860  |   6  |   3  |   6  |   4  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.7200  |  2.1000  |  1.7000  |  2.0500  |  1.7300  |  2.1000  |  1.8000  |  2.1500  |  1.6700  |  2.2000  |  1.8300  |  2.2000  |  1.7400  |  2.0600  |
   265  |    Melbourne  |             Australian Open  |  2013-01-27  |    Grand Slam  |  Outdoor  |     Hard  |    The Final  |        5  |  Djokovic N.  |       Murray A.  |      1  |      3  |  12920  |   8000  |   6  |   7  |   7  |   6  |   6  |   3  |   6  |   2  |   0  |   0  |      3  |      1  |  Completed  |  1.5300  |  2.6200  |  1.5000  |  2.5500  |  1.5300  |  2.5000  |  1.5400  |  2.7400  |  1.5300  |  2.6300  |  1.5700  |  2.9000  |  1.5000  |  2.6100  |
  2615  |        Paris  |         BNP Paribas Masters  |  2013-11-03  |  Masters 1000  |   Indoor  |     Hard  |    The Final  |        3  |  Djokovic N.  |       Ferrer D.  |      2  |      3  |  11120  |   6600  |   7  |   5  |   7  |   5  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.1200  |  6.0000  |  1.1500  |  5.2500  |  1.1400  |  5.5000  |  1.1700  |  6.0000  |  1.1500  |  5.5000  |  1.1500  |  5.4200  |  1.1800  |  6.5000  |
  2623  |       London  |                 Masters Cup  |  2013-11-07  |   Masters Cup  |   Indoor  |     Hard  |  Round Robin  |        3  |  Djokovic N.  |  Del Potro J.M.  |      2  |      5  |  10610  |   5055  |   6  |   3  |   3  |   6  |   6  |   3  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |  1.2800  |  3.7500  |  1.2800  |  3.5000  |  1.3000  |  3.5000  |  1.3000  |  3.8900  |  1.2900  |  3.7500  |  1.3300  |  4.0500  |  1.2800  |  3.6100  |
   263  |    Melbourne  |             Australian Open  |  2013-01-24  |    Grand Slam  |  Outdoor  |     Hard  |   Semifinals  |        5  |  Djokovic N.  |       Ferrer D.  |      1  |      5  |  12920  |   6505  |   6  |   2  |   6  |   2  |   6  |   1  |   0  |   0  |   0  |   0  |      3  |      0  |  Completed  |  1.0800  |  8.0000  |  1.1000  |  6.5000  |  1.1100  |  6.5000  |  1.1200  |  7.9400  |  1.1000  |  8.0000  |  1.1400  |  9.4000  |  1.1000  |  6.9100  |
  2425  |     Shanghai  |            Shanghai Masters  |  2013-10-13  |  Masters 1000  |  Outdoor  |     Hard  |    The Final  |        3  |  Djokovic N.  |  Del Potro J.M.  |      2  |      5  |  11120  |   4925  |   6  |   1  |   3  |   6  |   7  |   6  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |  1.3300  |  3.4000  |  1.3500  |  2.9000  |  1.3600  |  3.2500  |  1.3800  |  3.4100  |  1.3600  |  3.2500  |  1.4000  |  3.5000  |  1.3600  |  3.1500  |
   912  |  Monte Carlo  |         Monte Carlo Masters  |  2013-04-21  |  Masters 1000  |  Outdoor  |     Clay  |    The Final  |        3  |  Djokovic N.  |        Nadal R.  |      1  |      5  |  12500  |   6385  |   6  |   2  |   7  |   6  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  2.8700  |  1.4000  |  2.6500  |  1.4500  |  2.7500  |  1.4400  |  2.9700  |  1.4600  |  2.7500  |  1.4400  |  3.0000  |  1.5000  |  2.7600  |  1.4400  |
  2613  |        Paris  |         BNP Paribas Masters  |  2013-11-02  |  Masters 1000  |   Indoor  |     Hard  |   Semifinals  |        3  |  Djokovic N.  |      Federer R.  |      2  |      6  |  11120  |   4245  |   4  |   6  |   6  |   3  |   6  |   2  |   0  |   0  |   0  |   0  |      2  |      1  |  Completed  |  1.2800  |  3.7500  |  1.2500  |  3.7500  |  1.2900  |  3.7500  |  1.3100  |  3.9400  |  1.2900  |  3.7500  |  1.3200  |  4.4000  |  1.2800  |  3.6200  |
   613  |        Dubai  |  Dubai Tennis Championships  |  2013-03-02  |        ATP500  |  Outdoor  |     Hard  |    The Final  |        3  |  Djokovic N.  |      Berdych T.  |      1  |      6  |  12960  |   4545  |   7  |   5  |   6  |   3  |   0  |   0  |   0  |   0  |   0  |   0  |      2  |      0  |  Completed  |  1.1100  |  6.5000  |  1.1400  |  5.2000  |  1.1100  |  6.0000  |  1.1500  |  6.5400  |  1.1300  |  6.5000  |  1.1500  |  8.7000  |  1.1200  |  6.1700  |
</pre></div>

#### Performance

Morpheus has been designed specifically to deal with very large data, and the section on general performance
which is discussed [here]() and [here]() describes some of the implementation choices and their rationale. 
Morpheus is not a traditional Big Data solution in that it is focused on addressing a class of problem that 
can fit in the memory space of a single operating system process. These days it is not uncommon to find machines 
with 250GB of physical memory, which can certainly be used to analyze substantial data. A distributed solution
using Morpheus while possible, is not addressed as part of the framework, at least not at the time of writing.
 
Great care has been spent designing efficient data structures that scale well on the Java Virtual Machine, and
many of the common `DataFrame` operations can execute in **parallel** based on internal algorithms that leverage
the fork and join framework introduced in Java 7. Sorting is one such operation.

##### Sequential Sorting

By default, `DataFrame` functions implement sequential routines unless a concurrent implementation is requested
by calling the `parallel()` method on the interface in question. This design has been influenced by the 
Java 8 Stream API, which is not only fluid to work with, but should also serve to reduce the learning curve
somewhat (assuming you are familiar with some of these new APIs introduced in JDK 8).

The test results presented below were performed on a late 2013 Macbook Pro with a 2.6Ghz Core i7 Quad Core 
processor. Like all benchmarks, they should be taken with a pinch of salt, but the key point to convey here is 
the substantial gain in performance of the **parallel** sort over the **sequential** while adding no implementation 
complexity to the end user.

In all cases, these benchmarks execute 10 runs of each scenario, and compute summary statistics on the timimg results,
namely a *min*, *max*, *mean*, *median* and *standard deviation*. This should give adequate time for the Hotspot 
to perform its magic, and should also give us a sense on the level of dispersion we see across the runs. Obviously 
the lower the dispersion, the more confidence we can have in the results.

The code below is the full benchmark. Before each test run, we call `DataFrame.rows().sort(null)` which is effectively
passing a null comparator, and thereby resetting the frame to its original unsorted state. If we didn't do this
the first test would be much slower as subsequent tests would be asked to sort an already sorted frame. We would
see a very big difference between the *min* and *max* sort times if that was the case.

<?prettify?>
```java
//Define range of row counts we want to test, from 1M to 5M inclusive
Range<Integer> rowCounts = Range.of(1, 6).map(i -> i * 1000000);

//Time DataFrame sort operations on frame of random doubles with row counts ranging from 1M to 6M
DataFrame<String,String> results = DataFrame.combineFirst(rowCounts.map(rowCount -> {
    Range<Integer> rowKeys = Range.of(0, rowCount.intValue());
    Range<String> colKeys = Range.of(0, 5).map(i -> "C" + i);
    //Create frame initialized with random double values
    DataFrame<Integer,String> frame = DataFrame.ofDoubles(rowKeys, colKeys, v -> Math.random() * 100d);
    String label = "Rows(" + (rowCount / 1000000) + "M)";
    //Run each test 10 times, clear the sort before running the test with sort(null)
    return PerfStat.run(10, TimeUnit.MILLISECONDS, false, tasks -> {
        tasks.beforeEach(() -> frame.rows().sort(null));
        tasks.put(label, () -> frame.rows().sort(true, "C1"));
    });
}));

//Plot the results of the combined DataFrame with timings
Chart.create().withBarPlot(results, false, chart -> {
    chart.plot().axes().domain().label().withText("Timing Statistic");
    chart.plot().axes().range(0).label().withText("Time In Milliseconds");
    chart.title().withText("DataFrame Sorting Performance (Sequential)");
    chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
    chart.subtitle().withText("Row Sort with counts from 1M to 5M rows");
    chart.legend().on().bottom();
    chart.show();
});
```

The results in the chart below show a fairly linear increase in sort times as we progress from a frame with 1 million 
to 5 million rows. The dispersion in the 10 iterations as measured by the standard deviation is fairly low in all cases, 
at least as a percentage of the time taken.

<p align="center">
    <img class="chart" src="../../images/frame/data-frame-row-sort-sequential.png"/>
</p>


##### Parallel Sorting

The sequential benchmark code presented in the previous section can be ever so slightly modified to leverage a parallel 
sort by ensuring that we call `DataFrame.rows().parallel().sort()`. The modified code is as follows:
 
<?prettify?>
```java
//Define range of row counts we want to test, from 1M to 5M inclusive
Range<Integer> rowCounts = Range.of(1, 6).map(i -> i * 1000000);

//Time DataFrame sort operations on frame of random doubles with row counts ranging from 1M to 6M
DataFrame<String,String> results = DataFrame.combineFirst(rowCounts.map(rowCount -> {
    Range<Integer> rowKeys = Range.of(0, rowCount.intValue());
    Range<String> colKeys = Range.of(0, 5).map(i -> "C" + i);
    //Create frame initialized with random double values
    DataFrame<Integer,String> frame = DataFrame.ofDoubles(rowKeys, colKeys, v -> Math.random() * 100d);
    String label = "Rows(" + (rowCount / 1000000) + "M)";
    //Run each test 10 times, clear the sort before running the test with sort(null)
    return PerfStat.run(10, TimeUnit.MILLISECONDS, false, tasks -> {
        tasks.beforeEach(() -> frame.rows().sort(null));
        tasks.put(label, () -> frame.rows().parallel().sort(true, "C1"));
    });
}));

//Plot the results of the combined DataFrame with timings
Chart.create().withBarPlot(results, false, chart -> {
    chart.plot().axes().domain().label().withText("Timing Statistic");
    chart.plot().axes().range(0).label().withText("Time In Milliseconds");
    chart.title().withText("DataFrame Sorting Performance (Parallel)");
    chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
    chart.subtitle().withText("Row Sort with counts from 1M to 5M rows");
    chart.legend().on().bottom();
    chart.show();
});
```

The plot with the parallel execution results below shows a very significant improvement, and also has lower 
dispersion in the results. For the largest frame with 5 million rows the *median* sort time went from around 
1400 milliseconds in the sequential case to just over 500 milliseconds with the parallel execution. As processor 
cache sizes increase and ever more cores become available, we can only expect this spread to widen as Moore's 
Law begins to plateau.

<p align="center">
    <img class="chart" src="../../images/frame/data-frame-row-sort-parallel.png"/>
</p>

##### Comparator Performance

A final performance comparison that may be of interest is to consider the relative cost of sorting
with a user provided `Comparator` as opposed to using the `sort()` functions that just take a row
or column key. The example below addresses this question by sorting a frame with 1 million rows
and using identical logic where in one case we simply sort the rows based on a column key, and
in the second we provide a `Comparator` implementation that effectively does the same thing. We 
also run a sequential and parallel setup for these two cases, so 4 test scenarios in all. For each
scenario, we run 10 iterations so we can collect some summary statistics. The benchmark code is as
follows:

<?prettify?>
```java
//Create frame initialized with random double values
Range<Integer> rowKeys = Range.of(0, 1000000);
Range<String> colKeys = Range.of(0, 5).map(i -> "C" + i);
DataFrame<Integer,String> frame = DataFrame.ofDoubles(rowKeys, colKeys, v -> Math.random() * 100d);
//Define comparator to sort rows by column C1, which is ordinal 1
Comparator<DataFrameRow<Integer,String>> comparator = (row1, row2) -> {
    double v1 = row1.getDouble(1);
    double v2 = row2.getDouble(1);
    return Double.compare(v1, v2);
};

//Time sorting in various modes (with & without comparator in both sequential & parallel mode)
DataFrame<String,String> results = PerfStat.run(10, TimeUnit.MILLISECONDS, false, tasks -> {
    tasks.beforeEach(() -> frame.rows().sort(null));
    tasks.put("W/O Comparator (seq)", () -> frame.rows().sort(true, "C1"));
    tasks.put("W/O Comparator (par)", () -> frame.rows().parallel().sort(true, "C1"));
    tasks.put("W/ Comparator (seq)", () -> frame.rows().sort(comparator));
    tasks.put("W/ Comparator (par)", () -> frame.rows().parallel().sort(comparator));
});

//Plot the results of the combined DataFrame with timings
Chart.create().withBarPlot(results, false, chart -> {
    chart.plot().axes().domain().label().withText("Timing Statistic");
    chart.plot().axes().range(0).label().withText("Time In Milliseconds");
    chart.title().withText("DataFrame Sorting Performance With & Without Comparator");
    chart.subtitle().withText("1 Million rows of random double precision values");
    chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
    chart.legend().on().bottom();
    chart.show();
});
```

The test results presented below are not entirely surprising, and seem to suggest that the user `Comparator`
based sort is more than twice as slow as the built in sort. As might be expected, the built in sort
has private access to the internal structures of the `DataFrame`, and therefore has to make fewer calls
to get to the data in question. An interesting outcome in the sequential versus parallel battle
is that the parallel sort with the `Comparator` is still faster than the sequential sort without
the `Comparator`. 

<p align="center">
    <img class="chart" src="../../images/frame/data-frame-row-sort-comparator.png"/>
</p>

These performance benchmarks are obviously very controlled test cases, and real world performance is likely
to differ for all kinds of reasons. In addition, at the time of writing the Morpheus Library is at version 1.0, 
so performance improvements are likely to come in follow up releases.
