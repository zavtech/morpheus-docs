### Finding Data

#### Introduction

A `DataFrame` represents tablular data framed by a row and column axis that define a coordindate space by which entries 
can be read or written. As previously discussed in the section on [access](./access), it is possible to read/write data items 
either by keys, ordinals or a combination of the two. Finding data in a `DataFrame` often means identifying the row and or 
column coordinates associated with the data being searched. The Morpheus API provides a versatile set of functions to locate 
data in various ways, often providing both sequential and parallel search algorithms. This section provides some useful 
examples of how to use these APIs.

#### Example Data

In this section we will use a dataset introduced earlier, namely population data made available by the Office for National 
Statistics (ONS) in the United Kingdom. This dataset provides population counts between 1999 and 2014 decomposed into male and 
female age buckets ranging from 0 to 90 years old, over a range of UK Boroughs including the UK as a whole. The code to load 
this CSV formatted dataset is shown below, as well as a rendering of the first 10 rows. Note that the raw dataset is expressed 
as counts, so we convert these to a percentage of the total population for that row in order to standardize the values.

<?prettify?>
```java
/**
 * Returns the ONS population dataset for UK boroughs, and convert counts to weights
 * @return  the ONS population dataset, expressed as population weights
 */
static DataFrame<Tuple,String> loadPopulationDatasetWeights() {
    return DataFrame.read().<Tuple>csv(options -> {
        options.setResource("http://tinyurl.com/ons-population-year");
        options.setRowKeyParser(Tuple.class, row -> Tuple.of(Integer.parseInt(row[1]), row[2]));
        options.setExcludeColumns("Code");
        options.getFormats().setNullValues("-");
        options.setColumnType("All Males", Double.class);
        options.setColumnType("All Females", Double.class);
        options.setColumnType("All Persons", Double.class);
        options.setColumnType("[MF]\\s+\\d+", Double.class);
    }).applyValues(v -> {
       if (v.colKey().matches("[MF]\\s+\\d+")) {
           final double total = v.row().getDouble("All Persons");
           final double count = v.getDouble();
           return count / total;
       } else {
           return v.getValue();
       }
    });
}
```

<div class="frame"><pre class="frame">
         Index          |  Year  |     Borough      |  All Persons  |  All Males  |   M 0   |   M 1   |   M 2   |   M 3   |   M 4   |   M 5   |   M 6   |   M 7   |   M 8   |   M 9   |  M 10   |  M 11   |  M 12   |  M 13   |  M 14   |  M 15   |  M 16   |  M 17   |  M 18   |  M 19   |  M 20   |  M 21   |  M 22   |  M 23   |  M 24   |  M 25   |  M 26   |  M 27   |  M 28   |  M 29   |  M 30   |  M 31   |  M 32   |  M 33   |  M 34   |  M 35   |  M 36   |  M 37   |  M 38   |  M 39   |  M 40   |  M 41   |  M 42   |  M 43   |  M 44   |  M 45   |  M 46   |  M 47   |  M 48   |  M 49   |  M 50   |  M 51   |  M 52   |  M 53   |  M 54   |  M 55   |  M 56   |  M 57   |  M 58   |  M 59   |  M 60   |  M 61   |  M 62   |  M 63   |  M 64   |  M 65   |  M 66   |  M 67   |  M 68   |  M 69   |  M 70   |  M 71   |  M 72   |  M 73   |  M 74   |  M 75   |  M 76   |  M 77   |  M 78   |  M 79   |  M 80   |  M 81   |  M 82   |  M 83   |  M 84   |  M 85   |  M 86   |  M 87   |  M 88   |  M 89   |  M 90+  |  All Females  |   F 0   |   F 1   |   F 2   |   F 3   |   F 4   |   F 5   |   F 6   |   F 7   |   F 8   |   F 9   |  F 10   |  F 11   |  F 12   |  F 13   |  F 14   |  F 15   |  F 16   |  F 17   |  F 18   |  F 19   |  F 20   |  F 21   |  F 22   |  F 23   |  F 24   |  F 25   |  F 26   |  F 27   |  F 28   |  F 29   |  F 30   |  F 31   |  F 32   |  F 33   |  F 34   |  F 35   |  F 36   |  F 37   |  F 38   |  F 39   |  F 40   |  F 41   |  F 42   |  F 43   |  F 44   |  F 45   |  F 46   |  F 47   |  F 48   |  F 49   |  F 50   |  F 51   |  F 52   |  F 53   |  F 54   |  F 55   |  F 56   |  F 57   |  F 58   |  F 59   |  F 60   |  F 61   |  F 62   |  F 63   |  F 64   |  F 65   |  F 66   |  F 67   |  F 68   |  F 69   |  F 70   |  F 71   |  F 72   |  F 73   |  F 74   |  F 75   |  F 76   |  F 77   |  F 78   |  F 79   |  F 80   |  F 81   |  F 82   |  F 83   |  F 84   |  F 85   |  F 86   |  F 87   |  F 88   |  F 89   |  F 90+  |
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 (1999,City of London)  |  1999  |  City of London  |         6581  |       3519  |  0.36%  |  0.35%  |  0.33%  |  0.32%  |  0.30%  |  0.32%  |  0.30%  |  0.32%  |  0.30%  |  0.33%  |  0.33%  |  0.32%  |  0.29%  |  0.27%  |  0.26%  |  0.26%  |  0.26%  |  0.32%  |  0.40%  |  0.49%  |  0.61%  |  0.71%  |  0.79%  |  0.91%  |  0.99%  |  1.06%  |  1.11%  |  1.11%  |  1.11%  |  1.19%  |  1.19%  |  1.17%  |  1.14%  |  1.09%  |  1.08%  |  1.08%  |  0.99%  |  0.91%  |  0.88%  |  0.88%  |  0.87%  |  0.84%  |  0.77%  |  0.81%  |  0.81%  |  0.90%  |  0.90%  |  0.91%  |  0.99%  |  1.03%  |  1.03%  |  1.06%  |  0.96%  |  0.94%  |  0.93%  |  0.82%  |  0.74%  |  0.71%  |  0.65%  |  0.58%  |  0.53%  |  0.52%  |  0.52%  |  0.50%  |  0.49%  |  0.47%  |  0.49%  |  0.47%  |  0.46%  |  0.46%  |  0.44%  |  0.41%  |  0.40%  |  0.36%  |  0.35%  |  0.30%  |  0.30%  |  0.27%  |  0.26%  |  0.23%  |  0.21%  |  0.18%  |  0.18%  |  0.17%  |  0.17%  |  0.65%  |    NaN  |    NaN  |    NaN  |    NaN  |      0  |         3062  |  0.36%  |  0.36%  |  0.33%  |  0.33%  |  0.32%  |  0.32%  |  0.29%  |  0.29%  |  0.27%  |  0.27%  |  0.27%  |  0.24%  |  0.26%  |  0.24%  |  0.24%  |  0.24%  |  0.30%  |  0.38%  |  0.40%  |  0.46%  |  0.59%  |  0.71%  |  0.76%  |  0.82%  |  0.93%  |  1.03%  |  1.08%  |  1.09%  |  1.08%  |  1.11%  |  1.08%  |  1.03%  |  1.00%  |  0.94%  |  0.84%  |  0.77%  |  0.71%  |  0.65%  |  0.64%  |  0.59%  |  0.59%  |  0.62%  |  0.64%  |  0.65%  |  0.67%  |  0.61%  |  0.67%  |  0.68%  |  0.71%  |  0.73%  |  0.74%  |  0.74%  |  0.76%  |  0.70%  |  0.67%  |  0.59%  |  0.55%  |  0.52%  |  0.50%  |  0.47%  |  0.46%  |  0.44%  |  0.44%  |  0.46%  |  0.47%  |  0.46%  |  0.46%  |  0.44%  |  0.44%  |  0.43%  |  0.40%  |  0.40%  |  0.38%  |  0.35%  |  0.32%  |  0.30%  |  0.30%  |  0.29%  |  0.26%  |  0.26%  |  0.26%  |  0.24%  |  0.23%  |  0.21%  |  0.20%  |  1.15%  |    NaN  |    NaN  |    NaN  |    NaN  |      0  |
 (2000,City of London)  |  2000  |  City of London  |         7014  |       3775  |  0.36%  |  0.36%  |  0.34%  |  0.33%  |  0.31%  |  0.30%  |  0.31%  |  0.30%  |  0.31%  |  0.30%  |  0.31%  |  0.29%  |  0.26%  |  0.23%  |  0.23%  |  0.21%  |  0.24%  |  0.27%  |  0.34%  |  0.43%  |  0.56%  |  0.68%  |  0.81%  |  0.91%  |  1.03%  |  1.15%  |  1.25%  |  1.28%  |  1.28%  |  1.30%  |  1.34%  |  1.34%  |  1.28%  |  1.23%  |  1.15%  |  1.14%  |  1.08%  |  1.00%  |  0.93%  |  0.88%  |  0.88%  |  0.88%  |  0.84%  |  0.80%  |  0.80%  |  0.80%  |  0.88%  |  0.88%  |  0.88%  |  0.96%  |  1.01%  |  1.01%  |  1.06%  |  0.94%  |  0.91%  |  0.90%  |  0.80%  |  0.71%  |  0.67%  |  0.61%  |  0.56%  |  0.48%  |  0.46%  |  0.44%  |  0.43%  |  0.41%  |  0.40%  |  0.40%  |  0.38%  |  0.40%  |  0.38%  |  0.37%  |  0.36%  |  0.33%  |  0.31%  |  0.31%  |  0.27%  |  0.27%  |  0.24%  |  0.23%  |  0.21%  |  0.20%  |  0.17%  |  0.17%  |  0.16%  |  0.56%  |    NaN  |    NaN  |    NaN  |    NaN  |      0  |         3239  |  0.37%  |  0.34%  |  0.34%  |  0.31%  |  0.30%  |  0.30%  |  0.29%  |  0.27%  |  0.27%  |  0.27%  |  0.27%  |  0.26%  |  0.24%  |  0.26%  |  0.24%  |  0.26%  |  0.29%  |  0.33%  |  0.40%  |  0.46%  |  0.54%  |  0.70%  |  0.83%  |  0.87%  |  0.98%  |  1.07%  |  1.17%  |  1.18%  |  1.20%  |  1.15%  |  1.14%  |  1.08%  |  1.04%  |  0.98%  |  0.91%  |  0.81%  |  0.76%  |  0.68%  |  0.63%  |  0.61%  |  0.58%  |  0.60%  |  0.58%  |  0.61%  |  0.61%  |  0.63%  |  0.58%  |  0.64%  |  0.64%  |  0.67%  |  0.70%  |  0.71%  |  0.73%  |  0.73%  |  0.67%  |  0.64%  |  0.58%  |  0.53%  |  0.50%  |  0.46%  |  0.43%  |  0.41%  |  0.40%  |  0.38%  |  0.40%  |  0.41%  |  0.40%  |  0.40%  |  0.40%  |  0.40%  |  0.38%  |  0.36%  |  0.36%  |  0.34%  |  0.31%  |  0.30%  |  0.27%  |  0.29%  |  0.27%  |  0.24%  |  0.23%  |  0.21%  |  0.21%  |  0.21%  |  0.19%  |  1.15%  |    NaN  |    NaN  |    NaN  |    NaN  |      0  |
 (2001,City of London)  |  2001  |  City of London  |         7359  |       3984  |  0.23%  |  0.41%  |  0.34%  |  0.26%  |  0.29%  |  0.34%  |  0.27%  |  0.22%  |  0.26%  |  0.37%  |  0.35%  |  0.22%  |  0.39%  |  0.26%  |  0.15%  |  0.19%  |  0.16%  |  0.18%  |  0.16%  |  0.37%  |  0.52%  |  0.53%  |  0.72%  |  0.86%  |  1.09%  |  1.49%  |  1.49%  |  1.36%  |  1.47%  |  1.70%  |  1.55%  |  1.29%  |  1.26%  |  1.43%  |  1.35%  |  1.14%  |  1.21%  |  0.96%  |  0.86%  |  1.02%  |  0.88%  |  0.71%  |  0.64%  |  1.03%  |  0.99%  |  0.73%  |  0.67%  |  0.61%  |  0.82%  |  0.67%  |  1.51%  |  0.99%  |  0.75%  |  1.10%  |  1.09%  |  0.83%  |  0.86%  |  0.76%  |  0.87%  |  0.65%  |  0.38%  |  0.46%  |  0.61%  |  0.49%  |  0.34%  |  0.38%  |  0.29%  |  0.38%  |  0.29%  |  0.37%  |  0.37%  |  0.29%  |  0.33%  |  0.39%  |  0.29%  |  0.26%  |  0.31%  |  0.27%  |  0.15%  |  0.26%  |  0.20%  |  0.19%  |  0.11%  |  0.20%  |  0.14%  |  0.08%  |  0.14%  |  0.07%  |  0.05%  |  0.04%  |     13  |         3375  |  0.43%  |  0.41%  |  0.31%  |  0.29%  |  0.35%  |  0.22%  |  0.29%  |  0.20%  |  0.24%  |  0.33%  |  0.27%  |  0.27%  |  0.14%  |  0.35%  |  0.18%  |  0.18%  |  0.19%  |  0.53%  |  0.38%  |  0.48%  |  0.64%  |  0.53%  |  0.67%  |  0.73%  |  1.01%  |  1.26%  |  1.21%  |  1.09%  |  1.14%  |  1.45%  |  1.16%  |  1.13%  |  1.37%  |  0.90%  |  0.82%  |  0.73%  |  0.90%  |  0.82%  |  0.65%  |  0.61%  |  0.50%  |  0.38%  |  0.42%  |  0.86%  |  0.58%  |  0.72%  |  0.68%  |  0.64%  |  0.48%  |  0.57%  |  0.50%  |  0.88%  |  0.65%  |  0.77%  |  0.77%  |  0.52%  |  0.61%  |  0.52%  |  0.52%  |  0.45%  |  0.43%  |  0.43%  |  0.35%  |  0.34%  |  0.33%  |  0.39%  |  0.38%  |  0.37%  |  0.46%  |  0.37%  |  0.27%  |  0.35%  |  0.33%  |  0.42%  |  0.24%  |  0.33%  |  0.37%  |  0.18%  |  0.18%  |  0.20%  |  0.24%  |  0.30%  |  0.24%  |  0.15%  |  0.14%  |  0.14%  |  0.19%  |  0.35%  |  0.22%  |  0.10%  |     15  |
 (2002,City of London)  |  2002  |  City of London  |         7280  |       3968  |  0.40%  |  0.25%  |  0.38%  |  0.37%  |  0.27%  |  0.27%  |  0.33%  |  0.27%  |  0.23%  |  0.27%  |  0.34%  |  0.34%  |  0.19%  |  0.37%  |  0.12%  |  0.07%  |  0.11%  |  0.14%  |  0.16%  |  0.14%  |  0.27%  |  0.54%  |  0.69%  |  0.84%  |  0.93%  |  1.36%  |  1.55%  |  1.68%  |  1.40%  |  1.57%  |  1.57%  |  1.47%  |  1.36%  |  1.22%  |  1.39%  |  1.28%  |  1.15%  |  1.06%  |  1.00%  |  0.84%  |  1.07%  |  0.89%  |  0.78%  |  0.69%  |  1.03%  |  0.99%  |  0.77%  |  0.69%  |  0.62%  |  0.84%  |  0.63%  |  1.55%  |  0.96%  |  0.67%  |  1.17%  |  1.04%  |  0.81%  |  0.84%  |  0.76%  |  0.82%  |  0.62%  |  0.38%  |  0.43%  |  0.63%  |  0.55%  |  0.32%  |  0.36%  |  0.27%  |  0.34%  |  0.29%  |  0.33%  |  0.41%  |  0.29%  |  0.34%  |  0.40%  |  0.27%  |  0.25%  |  0.29%  |  0.23%  |  0.15%  |  0.23%  |  0.19%  |  0.18%  |  0.11%  |  0.21%  |  0.12%  |  0.08%  |  0.11%  |  0.05%  |  0.03%  |     14  |         3312  |  0.38%  |  0.48%  |  0.41%  |  0.26%  |  0.29%  |  0.36%  |  0.21%  |  0.27%  |  0.18%  |  0.25%  |  0.23%  |  0.21%  |  0.23%  |  0.08%  |  0.26%  |  0.18%  |  0.07%  |  0.21%  |  0.48%  |  0.40%  |  0.48%  |  0.66%  |  0.71%  |  0.65%  |  0.92%  |  1.02%  |  1.15%  |  1.15%  |  1.14%  |  0.96%  |  1.58%  |  1.15%  |  1.29%  |  1.39%  |  0.87%  |  0.84%  |  0.78%  |  0.80%  |  0.85%  |  0.60%  |  0.65%  |  0.51%  |  0.38%  |  0.45%  |  0.82%  |  0.49%  |  0.65%  |  0.71%  |  0.62%  |  0.49%  |  0.52%  |  0.56%  |  0.87%  |  0.70%  |  0.74%  |  0.76%  |  0.51%  |  0.56%  |  0.55%  |  0.49%  |  0.41%  |  0.40%  |  0.41%  |  0.37%  |  0.32%  |  0.32%  |  0.38%  |  0.38%  |  0.34%  |  0.47%  |  0.34%  |  0.26%  |  0.33%  |  0.32%  |  0.43%  |  0.26%  |  0.34%  |  0.36%  |  0.14%  |  0.16%  |  0.19%  |  0.23%  |  0.30%  |  0.21%  |  0.12%  |  0.14%  |  0.10%  |  0.15%  |  0.36%  |  0.19%  |     20  |
 (2003,City of London)  |  2003  |  City of London  |         7115  |       3892  |  0.49%  |  0.34%  |  0.22%  |  0.31%  |  0.37%  |  0.24%  |  0.27%  |  0.34%  |  0.30%  |  0.30%  |  0.34%  |  0.35%  |  0.30%  |  0.14%  |  0.22%  |  0.14%  |  0.06%  |  0.07%  |  0.17%  |  0.17%  |  0.15%  |  0.27%  |  0.69%  |  0.84%  |  0.93%  |  1.12%  |  1.41%  |  1.66%  |  1.60%  |  1.48%  |  1.52%  |  1.48%  |  1.39%  |  1.32%  |  1.19%  |  1.36%  |  1.25%  |  1.15%  |  1.07%  |  0.97%  |  0.89%  |  1.04%  |  0.90%  |  0.80%  |  0.72%  |  1.05%  |  1.04%  |  0.79%  |  0.72%  |  0.55%  |  0.90%  |  0.62%  |  1.52%  |  0.91%  |  0.66%  |  1.12%  |  1.04%  |  0.79%  |  0.86%  |  0.77%  |  0.77%  |  0.62%  |  0.37%  |  0.42%  |  0.59%  |  0.51%  |  0.32%  |  0.34%  |  0.31%  |  0.31%  |  0.25%  |  0.32%  |  0.44%  |  0.25%  |  0.34%  |  0.38%  |  0.27%  |  0.24%  |  0.31%  |  0.21%  |  0.14%  |  0.22%  |  0.17%  |  0.14%  |  0.11%  |  0.20%  |  0.11%  |  0.08%  |  0.10%  |  0.03%  |     13  |         3223  |  0.25%  |  0.35%  |  0.44%  |  0.41%  |  0.25%  |  0.30%  |  0.37%  |  0.20%  |  0.27%  |  0.17%  |  0.27%  |  0.18%  |  0.15%  |  0.17%  |  0.03%  |  0.22%  |  0.10%  |  0.17%  |  0.31%  |  0.44%  |  0.48%  |  0.52%  |  0.73%  |  0.86%  |  0.82%  |  1.19%  |  1.03%  |  1.21%  |  1.15%  |  1.19%  |  1.05%  |  1.39%  |  1.19%  |  1.25%  |  1.29%  |  0.84%  |  0.82%  |  0.63%  |  0.72%  |  0.74%  |  0.59%  |  0.66%  |  0.62%  |  0.41%  |  0.49%  |  0.83%  |  0.53%  |  0.58%  |  0.69%  |  0.55%  |  0.49%  |  0.56%  |  0.52%  |  0.86%  |  0.69%  |  0.74%  |  0.70%  |  0.49%  |  0.56%  |  0.48%  |  0.51%  |  0.42%  |  0.41%  |  0.42%  |  0.38%  |  0.34%  |  0.32%  |  0.39%  |  0.42%  |  0.35%  |  0.45%  |  0.34%  |  0.24%  |  0.32%  |  0.30%  |  0.42%  |  0.22%  |  0.32%  |  0.34%  |  0.14%  |  0.15%  |  0.20%  |  0.21%  |  0.22%  |  0.22%  |  0.13%  |  0.03%  |  0.07%  |  0.14%  |  0.31%  |     26  |
 (2004,City of London)  |  2004  |  City of London  |         7118  |       3875  |  0.39%  |  0.38%  |  0.35%  |  0.22%  |  0.28%  |  0.38%  |  0.24%  |  0.28%  |  0.44%  |  0.25%  |  0.31%  |  0.30%  |  0.31%  |  0.22%  |  0.06%  |  0.18%  |  0.11%  |  0.00%  |  0.03%  |  0.18%  |  0.13%  |  0.35%  |  0.37%  |  0.89%  |  0.98%  |  0.97%  |  1.25%  |  1.57%  |  1.62%  |  1.63%  |  1.40%  |  1.31%  |  1.43%  |  1.31%  |  1.21%  |  1.11%  |  1.39%  |  1.29%  |  1.24%  |  1.14%  |  1.03%  |  0.83%  |  0.91%  |  0.94%  |  0.81%  |  0.69%  |  1.07%  |  0.98%  |  0.77%  |  0.70%  |  0.62%  |  0.86%  |  0.63%  |  1.39%  |  0.91%  |  0.65%  |  1.08%  |  1.07%  |  0.77%  |  0.80%  |  0.74%  |  0.80%  |  0.60%  |  0.39%  |  0.45%  |  0.55%  |  0.48%  |  0.31%  |  0.35%  |  0.30%  |  0.31%  |  0.25%  |  0.28%  |  0.42%  |  0.22%  |  0.34%  |  0.39%  |  0.27%  |  0.20%  |  0.28%  |  0.13%  |  0.17%  |  0.20%  |  0.14%  |  0.11%  |  0.11%  |  0.21%  |  0.11%  |  0.07%  |  0.07%  |     12  |         3243  |  0.44%  |  0.25%  |  0.28%  |  0.37%  |  0.35%  |  0.28%  |  0.35%  |  0.39%  |  0.20%  |  0.27%  |  0.10%  |  0.27%  |  0.14%  |  0.13%  |  0.13%  |  0.01%  |  0.18%  |  0.24%  |  0.13%  |  0.31%  |  0.53%  |  0.58%  |  0.66%  |  0.83%  |  1.11%  |  1.12%  |  1.28%  |  1.21%  |  1.11%  |  1.14%  |  1.11%  |  1.10%  |  1.28%  |  1.12%  |  1.22%  |  1.17%  |  0.79%  |  0.86%  |  0.66%  |  0.67%  |  0.77%  |  0.56%  |  0.63%  |  0.55%  |  0.42%  |  0.48%  |  0.83%  |  0.52%  |  0.55%  |  0.79%  |  0.53%  |  0.46%  |  0.53%  |  0.51%  |  0.84%  |  0.72%  |  0.73%  |  0.72%  |  0.48%  |  0.56%  |  0.49%  |  0.48%  |  0.39%  |  0.45%  |  0.39%  |  0.38%  |  0.35%  |  0.28%  |  0.41%  |  0.39%  |  0.34%  |  0.42%  |  0.34%  |  0.21%  |  0.30%  |  0.31%  |  0.41%  |  0.21%  |  0.32%  |  0.31%  |  0.17%  |  0.15%  |  0.17%  |  0.15%  |  0.17%  |  0.17%  |  0.10%  |  0.01%  |  0.04%  |  0.13%  |     42  |
 (2005,City of London)  |  2005  |  City of London  |         7131  |       3869  |  0.48%  |  0.35%  |  0.31%  |  0.32%  |  0.17%  |  0.27%  |  0.31%  |  0.27%  |  0.27%  |  0.43%  |  0.28%  |  0.29%  |  0.28%  |  0.27%  |  0.06%  |  0.04%  |  0.17%  |  0.14%  |  0.00%  |  0.10%  |  0.13%  |  0.22%  |  0.60%  |  0.56%  |  1.01%  |  1.30%  |  1.04%  |  1.25%  |  1.64%  |  1.58%  |  1.46%  |  1.23%  |  1.29%  |  1.53%  |  1.37%  |  1.08%  |  1.07%  |  1.44%  |  1.25%  |  1.09%  |  1.12%  |  0.95%  |  0.86%  |  0.97%  |  0.87%  |  0.81%  |  0.72%  |  1.15%  |  0.94%  |  0.81%  |  0.73%  |  0.59%  |  0.83%  |  0.70%  |  1.25%  |  0.94%  |  0.66%  |  1.02%  |  0.98%  |  0.77%  |  0.81%  |  0.70%  |  0.74%  |  0.59%  |  0.38%  |  0.42%  |  0.59%  |  0.43%  |  0.32%  |  0.34%  |  0.28%  |  0.24%  |  0.24%  |  0.27%  |  0.42%  |  0.21%  |  0.29%  |  0.38%  |  0.24%  |  0.20%  |  0.29%  |  0.11%  |  0.20%  |  0.17%  |  0.10%  |  0.10%  |  0.08%  |  0.20%  |  0.10%  |  0.03%  |     12  |         3262  |  0.41%  |  0.41%  |  0.21%  |  0.21%  |  0.32%  |  0.35%  |  0.24%  |  0.32%  |  0.35%  |  0.20%  |  0.25%  |  0.10%  |  0.28%  |  0.10%  |  0.10%  |  0.08%  |  0.03%  |  0.10%  |  0.27%  |  0.21%  |  0.43%  |  0.60%  |  0.69%  |  0.70%  |  1.15%  |  1.07%  |  1.25%  |  1.25%  |  1.19%  |  1.07%  |  1.26%  |  1.07%  |  1.19%  |  1.28%  |  1.04%  |  1.12%  |  1.04%  |  0.84%  |  0.77%  |  0.73%  |  0.63%  |  0.73%  |  0.60%  |  0.66%  |  0.53%  |  0.48%  |  0.48%  |  0.77%  |  0.46%  |  0.53%  |  0.80%  |  0.53%  |  0.50%  |  0.55%  |  0.52%  |  0.81%  |  0.73%  |  0.77%  |  0.70%  |  0.48%  |  0.52%  |  0.49%  |  0.52%  |  0.36%  |  0.46%  |  0.42%  |  0.35%  |  0.34%  |  0.25%  |  0.38%  |  0.36%  |  0.28%  |  0.39%  |  0.31%  |  0.25%  |  0.29%  |  0.34%  |  0.36%  |  0.22%  |  0.29%  |  0.31%  |  0.14%  |  0.17%  |  0.15%  |  0.14%  |  0.17%  |  0.13%  |  0.03%  |  0.03%  |  0.04%  |     50  |
 (2006,City of London)  |  2006  |  City of London  |         7254  |       3959  |  0.36%  |  0.34%  |  0.36%  |  0.28%  |  0.25%  |  0.18%  |  0.22%  |  0.32%  |  0.30%  |  0.25%  |  0.41%  |  0.25%  |  0.28%  |  0.23%  |  0.10%  |  0.07%  |  0.07%  |  0.21%  |  0.17%  |  0.00%  |  0.17%  |  0.26%  |  0.44%  |  0.87%  |  0.87%  |  1.13%  |  1.50%  |  1.14%  |  1.36%  |  1.71%  |  1.57%  |  1.42%  |  1.14%  |  1.17%  |  1.60%  |  1.35%  |  1.06%  |  1.02%  |  1.48%  |  1.20%  |  1.03%  |  1.13%  |  0.94%  |  0.87%  |  0.95%  |  0.81%  |  0.85%  |  0.73%  |  1.14%  |  0.91%  |  0.83%  |  0.72%  |  0.66%  |  0.87%  |  0.66%  |  1.17%  |  0.92%  |  0.65%  |  0.94%  |  0.85%  |  0.77%  |  0.77%  |  0.66%  |  0.74%  |  0.55%  |  0.39%  |  0.36%  |  0.57%  |  0.43%  |  0.30%  |  0.32%  |  0.28%  |  0.19%  |  0.22%  |  0.26%  |  0.40%  |  0.23%  |  0.28%  |  0.39%  |  0.23%  |  0.19%  |  0.26%  |  0.08%  |  0.14%  |  0.17%  |  0.07%  |  0.06%  |  0.08%  |  0.17%  |  0.10%  |     13  |         3295  |  0.39%  |  0.36%  |  0.43%  |  0.17%  |  0.23%  |  0.34%  |  0.32%  |  0.28%  |  0.30%  |  0.34%  |  0.19%  |  0.25%  |  0.07%  |  0.26%  |  0.06%  |  0.10%  |  0.06%  |  0.17%  |  0.10%  |  0.37%  |  0.33%  |  0.45%  |  0.70%  |  0.80%  |  0.98%  |  1.21%  |  1.10%  |  1.19%  |  1.19%  |  1.27%  |  1.16%  |  1.19%  |  1.08%  |  1.16%  |  1.10%  |  1.06%  |  1.02%  |  0.94%  |  0.85%  |  0.62%  |  0.73%  |  0.62%  |  0.70%  |  0.62%  |  0.63%  |  0.54%  |  0.52%  |  0.45%  |  0.73%  |  0.43%  |  0.51%  |  0.79%  |  0.54%  |  0.50%  |  0.52%  |  0.50%  |  0.77%  |  0.69%  |  0.73%  |  0.65%  |  0.41%  |  0.57%  |  0.43%  |  0.47%  |  0.33%  |  0.41%  |  0.40%  |  0.32%  |  0.36%  |  0.23%  |  0.37%  |  0.36%  |  0.28%  |  0.36%  |  0.33%  |  0.25%  |  0.28%  |  0.30%  |  0.37%  |  0.23%  |  0.28%  |  0.26%  |  0.15%  |  0.15%  |  0.14%  |  0.12%  |  0.15%  |  0.12%  |  0.03%  |  0.00%  |     45  |
 (2007,City of London)  |  2007  |  City of London  |         7607  |       4197  |  0.41%  |  0.33%  |  0.32%  |  0.29%  |  0.30%  |  0.21%  |  0.17%  |  0.18%  |  0.35%  |  0.30%  |  0.20%  |  0.43%  |  0.21%  |  0.25%  |  0.09%  |  0.09%  |  0.04%  |  0.12%  |  0.21%  |  0.35%  |  0.00%  |  0.39%  |  0.46%  |  0.84%  |  1.20%  |  1.18%  |  1.49%  |  1.62%  |  1.30%  |  1.41%  |  1.62%  |  1.34%  |  1.39%  |  1.10%  |  1.14%  |  1.50%  |  1.37%  |  1.13%  |  1.09%  |  1.54%  |  1.16%  |  1.04%  |  0.97%  |  0.92%  |  0.99%  |  0.97%  |  0.83%  |  0.84%  |  0.75%  |  1.05%  |  0.85%  |  0.82%  |  0.72%  |  0.67%  |  0.84%  |  0.70%  |  1.03%  |  0.84%  |  0.63%  |  0.83%  |  0.78%  |  0.71%  |  0.76%  |  0.60%  |  0.71%  |  0.47%  |  0.34%  |  0.39%  |  0.51%  |  0.39%  |  0.29%  |  0.30%  |  0.22%  |  0.21%  |  0.21%  |  0.25%  |  0.35%  |  0.20%  |  0.20%  |  0.38%  |  0.18%  |  0.18%  |  0.24%  |  0.08%  |  0.08%  |  0.13%  |  0.05%  |  0.05%  |  0.08%  |  0.12%  |     20  |         3410  |  0.32%  |  0.32%  |  0.39%  |  0.33%  |  0.22%  |  0.22%  |  0.22%  |  0.28%  |  0.21%  |  0.28%  |  0.33%  |  0.20%  |  0.24%  |  0.05%  |  0.25%  |  0.04%  |  0.14%  |  0.32%  |  0.20%  |  0.32%  |  0.51%  |  0.45%  |  0.74%  |  0.92%  |  0.99%  |  1.20%  |  1.31%  |  1.12%  |  1.33%  |  1.09%  |  1.05%  |  1.00%  |  1.05%  |  0.95%  |  0.95%  |  0.95%  |  1.00%  |  0.96%  |  0.82%  |  0.85%  |  0.62%  |  0.74%  |  0.63%  |  0.72%  |  0.66%  |  0.64%  |  0.57%  |  0.47%  |  0.39%  |  0.63%  |  0.38%  |  0.50%  |  0.71%  |  0.54%  |  0.51%  |  0.51%  |  0.51%  |  0.74%  |  0.68%  |  0.70%  |  0.60%  |  0.35%  |  0.54%  |  0.46%  |  0.42%  |  0.35%  |  0.39%  |  0.42%  |  0.29%  |  0.32%  |  0.21%  |  0.35%  |  0.34%  |  0.24%  |  0.33%  |  0.30%  |  0.22%  |  0.24%  |  0.29%  |  0.30%  |  0.22%  |  0.22%  |  0.25%  |  0.14%  |  0.13%  |  0.13%  |  0.09%  |  0.12%  |  0.11%  |  0.01%  |     36  |
 (2008,City of London)  |  2008  |  City of London  |         7429  |       4131  |  0.39%  |  0.35%  |  0.30%  |  0.32%  |  0.24%  |  0.30%  |  0.17%  |  0.16%  |  0.23%  |  0.40%  |  0.27%  |  0.20%  |  0.40%  |  0.23%  |  0.13%  |  0.11%  |  0.08%  |  0.09%  |  0.15%  |  0.24%  |  0.38%  |  0.15%  |  0.58%  |  0.52%  |  1.01%  |  1.35%  |  1.14%  |  1.66%  |  1.64%  |  1.37%  |  1.25%  |  1.49%  |  1.29%  |  1.36%  |  1.01%  |  1.08%  |  1.39%  |  1.24%  |  1.12%  |  1.06%  |  1.63%  |  1.16%  |  1.06%  |  0.93%  |  0.94%  |  0.89%  |  1.04%  |  0.77%  |  0.90%  |  0.90%  |  1.09%  |  0.86%  |  0.82%  |  0.69%  |  0.70%  |  0.82%  |  0.74%  |  0.98%  |  0.82%  |  0.62%  |  0.81%  |  0.82%  |  0.71%  |  0.74%  |  0.57%  |  0.71%  |  0.46%  |  0.35%  |  0.39%  |  0.52%  |  0.39%  |  0.32%  |  0.30%  |  0.23%  |  0.20%  |  0.19%  |  0.26%  |  0.32%  |  0.19%  |  0.22%  |  0.36%  |  0.17%  |  0.16%  |  0.23%  |  0.08%  |  0.08%  |  0.09%  |  0.05%  |  0.04%  |  0.07%  |     25  |         3298  |  0.16%  |  0.23%  |  0.31%  |  0.31%  |  0.32%  |  0.22%  |  0.23%  |  0.22%  |  0.26%  |  0.20%  |  0.28%  |  0.32%  |  0.20%  |  0.26%  |  0.01%  |  0.26%  |  0.00%  |  0.30%  |  0.35%  |  0.34%  |  0.47%  |  0.58%  |  0.47%  |  0.77%  |  0.98%  |  1.10%  |  1.20%  |  1.28%  |  1.18%  |  1.27%  |  1.02%  |  1.04%  |  0.92%  |  0.93%  |  0.77%  |  0.81%  |  0.82%  |  0.86%  |  0.92%  |  0.77%  |  0.85%  |  0.55%  |  0.70%  |  0.62%  |  0.70%  |  0.67%  |  0.67%  |  0.50%  |  0.47%  |  0.43%  |  0.57%  |  0.39%  |  0.51%  |  0.71%  |  0.54%  |  0.57%  |  0.57%  |  0.51%  |  0.70%  |  0.69%  |  0.69%  |  0.65%  |  0.36%  |  0.55%  |  0.46%  |  0.39%  |  0.40%  |  0.42%  |  0.39%  |  0.31%  |  0.30%  |  0.22%  |  0.38%  |  0.36%  |  0.27%  |  0.32%  |  0.30%  |  0.22%  |  0.20%  |  0.30%  |  0.32%  |  0.20%  |  0.23%  |  0.24%  |  0.13%  |  0.12%  |  0.12%  |  0.09%  |  0.12%  |  0.11%  |     30  |Male weight: 55.76%, Female weight: 44.24% for 2014 in City of London
</pre></div>

#### First & Last

##### Row / Column

It is often useful to locate the first or last row / column in a `DataFrame` that satisfies some condition. The
`DataFrameAxis` interface includes overloaded `first()` and `last()` methods, one which takes no arguments and 
another which takes a predicate. In the example below, we use this API to find the first row in the ONS population 
data where the male / female population weights differ by more than 10%, which is a very significant difference. 

The results indicate that in 2007, males made up **55.17%** and females made up **44.83%** of the population in 
the **City of London**, also know as the financial district. Perhaps no surprise there.

<?prettify?>
```java
DataFrame<Tuple,String> frame = DemoData.loadPopulationDatasetWeights();
frame.rows().first(row -> {
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
```

    Male weight: 55.17%, Female weight: 44.83% for 2007 in City of London

We can also search the frame from bottom to top by calling `DataFrame.rows().last()` which yields another hit
for the **City of London**, but in this case in 2014. The weights are **55.76%** and **44.24%** for males and
females respectively. So much for progress in gender equality in the financial district!

    Male weight: 55.76%, Female weight: 44.24% for 2014 in City of London

##### Row / Column Value

The above example demonstrates the use of the `first()` and `last()` methods on `DataFrameAxis` to locate the 
first and last row or column that satisfies some predicate. The same functionality is provided on the `DataFrameVector`
interface to locate the first and last entries in a specific row or column.

In this example, we iterate over all rows in the `DataFrame` that correspond to the Borough of **Kensington and
Chelsea**, and for each row we attempt to find the first age group which has a population weight that exceeds
1% of the total population for that year. We then simply print the results to standard out.

<?prettify?>
```java
DataFrame<Tuple,String> frame = DemoData.loadPopulationDatasetWeights();
frame.rows().filter(r -> r.getValue("Borough").equals("Kensington and Chelsea")).forEach(row -> {
    row.first(v -> v.colKey().matches("[MF]\\s+\\d+") && v.getDouble() > 0.01).ifPresent(v -> {
        Tuple rowKey = v.rowKey();
        String group = v.colKey();
        double weight = v.getDouble() * 100d;
        IO.printf("Age group %s has a population of %.2f%% for %s\n", group, weight, rowKey);
    });
});
```
    Age group M 28 has a population of 1.05% for (1999,Kensington and Chelsea)
    Age group M 27 has a population of 1.02% for (2000,Kensington and Chelsea)
    Age group M 26 has a population of 1.04% for (2001,Kensington and Chelsea)
    Age group M 26 has a population of 1.13% for (2002,Kensington and Chelsea)
    Age group M 26 has a population of 1.09% for (2003,Kensington and Chelsea)
    Age group M 26 has a population of 1.02% for (2004,Kensington and Chelsea)
    Age group M 25 has a population of 1.07% for (2005,Kensington and Chelsea)
    Age group M 25 has a population of 1.07% for (2006,Kensington and Chelsea)
    Age group M 26 has a population of 1.14% for (2007,Kensington and Chelsea)
    Age group M 25 has a population of 1.02% for (2008,Kensington and Chelsea)
    Age group M 26 has a population of 1.07% for (2009,Kensington and Chelsea)
    Age group M 27 has a population of 1.10% for (2010,Kensington and Chelsea)
    Age group M 27 has a population of 1.02% for (2011,Kensington and Chelsea)
    Age group M 28 has a population of 1.00% for (2012,Kensington and Chelsea)
    Age group M 29 has a population of 1.04% for (2013,Kensington and Chelsea)
    Age group M 30 has a population of 1.00% for (2014,Kensington and Chelsea)

Performing similar analysis but this time searching for the last entry in each row which exceeds a population 
weight of 1.0% we get the following results.  The code for this is identical to above, except we call
`row.last()` passing in the same predicate.

    Age group F 85 has a population of 1.13% for (1999,Kensington and Chelsea)
    Age group F 85 has a population of 1.10% for (2000,Kensington and Chelsea)
    Age group F 35 has a population of 1.01% for (2001,Kensington and Chelsea)
    Age group F 35 has a population of 1.01% for (2002,Kensington and Chelsea)
    Age group F 35 has a population of 1.01% for (2003,Kensington and Chelsea)
    Age group F 35 has a population of 1.03% for (2004,Kensington and Chelsea)
    Age group F 34 has a population of 1.03% for (2005,Kensington and Chelsea)
    Age group F 34 has a population of 1.04% for (2006,Kensington and Chelsea)
    Age group F 35 has a population of 1.01% for (2007,Kensington and Chelsea)
    Age group F 34 has a population of 1.01% for (2008,Kensington and Chelsea)
    Age group F 34 has a population of 1.01% for (2009,Kensington and Chelsea)
    Age group F 34 has a population of 1.02% for (2010,Kensington and Chelsea)
    Age group F 34 has a population of 1.02% for (2011,Kensington and Chelsea)
    Age group F 33 has a population of 1.12% for (2012,Kensington and Chelsea)
    Age group F 34 has a population of 1.07% for (2013,Kensington and Chelsea)
    Age group F 35 has a population of 1.03% for (2014,Kensington and Chelsea)

#### Min & Max

##### Frame Min/Max

Finding the **mininum** or **maximum** value across the entire frame or within a given row or column subject
to some condition is trivial. Consider the example below whereby we wish to find the **gender**, **age group** 
and **year** that contains the largest population weight, but only in the Borough of **Islington**. The code 
below achieves this by calling the `max()` function on the ONS dataset with an appropriate predicate. The 
predicate defines the condition that we only want to consider values associated with the Islington Borough, 
and represent a male or female population weight, and which has a value > 0. This example also demonstrates 
the useful accessor methods on the `DataFrameValue` interface, such as efficiently accessing the row or column 
object associated  with the value.

<?prettify?>
```java
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
```

    Max population is 1.57% for age group F 27 in Islington, 2012

The analog of the above example to find the minimim value would simply involve a call to the `DataFrame.min()` method.

##### Row/Column Vector Min/Max

Finding the row or column that represents some minimum or maximum given some user specified `Comparator` is also
supported by the `DataFrameAxis` API. While this capability requires a linear search, it is much faster than sorting
the entire `DataFrame` according to the `Comparator`. In addition, this method supports **parallel** execution which
can boost performance on very large frames, or when the `Comparator` is expensive to execute.

The example below uses the ONS population dataset to find the row that has the highest ratio of female to male
births, and prints the results to standard out. The `max()` method returns an `Optional<DataFrameRow>` or an
`Optional<DataFrameColumn>` when operating on rows or columns respectively, which provides access to the
coordinates and data associated with the result. 

<?prettify?>
```java
final DataFrame<Tuple,String> frame = DemoData.loadPopulationDatasetWeights();
frame.rows().max((row1, row2) -> {
    double ratio1 = row1.getDouble("F 0") / row1.getDouble("M 0");
    double ratio2 = row2.getDouble("F 0") / row2.getDouble("M 0");
    return Double.compare(ratio1, ratio2);
}).ifPresent(row -> {
    Tuple rowKey = row.key();
    double males = row.getDouble("M 0") * 100d;
    double females = row.getDouble("F 0") * 100d;
    IO.printf("Largest female / male births = %.2f%%/%.2f%% for %s", females, males, rowKey);
});
```

    Largest female / male births = 0.46%/0.19% for (2009,City of London)

The smallest ratio between female / male births can be found simply by calling `DataFrame.rows().min()` with
the same `Comparator` as above. Furthermore, this API is completely symmetrical in the row and column dimension,
so finding the minimum or maximum column follows the same approach by call `DataFrame.cols()`.

##### Row/Column Value Min/Max

It is often useful to be able to locate the minimum or maximum value in a specific row or column. While you
could use the frame level `min()` and `max()` methods with a predicate that restricts the search space to a 
specific row or column, this would not be particuarly efficient as you would still need to iterate over all 
values in the frame.

The following examples show how one could select a specific row or column using the `rowAt()` or `colAt()` 
functions, and then use the `min()` or `max()` functions to search just within the desired vector. Overloaded 
versions of these methods are provided, one which takes no arguments, and the other takes a predicate. The code 
below demonstrates how to find the maximum population bucket for the Borough of Islingtom in year 2000. 

<?prettify?>
```java
Tuple rowKey = Tuple.of(2000, "Islington");
DataFrame<Tuple,String> frame = DemoData.loadPopulationDatasetWeights();
frame.rowAt(rowKey).max(v -> v.colKey().matches("[MF]\\s+\\d+") && v.getDouble() > 0).ifPresent(max -> {
    String group = max.colKey();
    int year = max.rowKey().item(0);
    String borough = max.rowKey().item(1);
    double weight = max.getDouble() * 100d;
    System.out.printf("Max population weight for %s in %s is %.2f%% for %s", borough, year, weight, group);
});
```

    Max population weight for Islington in 2000 is 1.30% for F 26

Similarly, the following code illustrates how to find which Borough out of **Islington**, **Wandsworth** and 
**Kensington and Chelsea** has the largest female population aged 30 and in what year this occurred. In this
case we select the column named **F 30** and find the max value subject to the condition that the Borough is 
part of our constrained universe.

<?prettify?>
```java
Set<String> boroughs = Collect.asSet("Islington", "Wandsworth", "Kensington and Chelsea");
DataFrame<Tuple,String> frame = DemoData.loadPopulationDatasetWeights();
frame.colAt("F 30").max(v -> boroughs.contains((String)v.row().getValue("Borough"))).ifPresent(max -> {
    int year = max.rowKey().item(0);
    double weight = max.getDouble() * 100d;
    String borough = max.row().getValue("Borough");
    System.out.printf("Max female population weight aged 30 is %.2f%% in %s, %s", weight, borough, year);
});
```

    Max female population weight aged 30 is 1.69% in Wandsworth, 2012

#### Binary Search

The `DataFrameVector` interface exposes 3 overloaded `binarySearch()` methods useful in locating an entry that
matches a specific value. The search can be optionally restricted to a subset of the vector by providing an offset 
ordinal and length, and an optional `Comparator` if the data is sorted according to some custom logic. 

Naturally the data in the vector must be sorted in ascending order, otherwise the behaviour of these methods is undefined. 
The `binarySearch()` methods return an `Optional<DataFrameValue<R,C>>` containing the matched value, or an empty
optional if no match. On a successful match, the `DataFrameValue<R,C>` interace can be used to access the coordinates
of the matched value.

In the example below, we perform a binary search for a pre-selected value in the **F 30** column, and then assert
that to located value and row key matches what we expect. Note that we sort the `DataFrame` rows so that data in the
**F 30** column is sorted in ascending order first.

<?prettify?>
```java
final Tuple expected = Tuple.of(2000, "Islington");
final DataFrame<Tuple,String> frame = DemoData.loadPopulationDatasetWeights();
final double weight = frame.data().getDouble(expected, "F 30");
frame.rows().sort(true, "F 30"); //Ensure data is sorted
frame.colAt("F 30").binarySearch(weight).ifPresent(value -> {
    assert(value.rowKey().equals(expected));
    assert(value.getDouble() == weight);
});
```

The same functionality is available in the row dimension as these methods are exposed on the `DataFrameVector` interface
which is the super interface of `DataFrameRow` and `DataFrameColumn`.

#### Lower & Higher

Morpheus also provides a useful API for locating the nearest row or column **key** before or after some specified value, 
even if that value does not exist in the axis in question. This functionality is contingent on the data in the axis being 
sorted in ascending order as it is implemented internally using a binary search, but is therefore also fast even for a 
very large axis. Consider a `DataFrame` of random values constructed with a row axis representing month end `LocalDate`
values, which is built as follows:

<?prettify?>
```java
LocalDate start = LocalDate.of(2014, 1, 1);
Range<String> columns = Range.of(0, 10).map(i -> "Column-" + i);
Range<LocalDate> monthEnds = Range.of(0, 12).map(i -> start.plusMonths(i+1).minusDays(1));
DataFrame<LocalDate,String> frame = DataFrame.ofDoubles(monthEnds, columns, v -> Math.random() * 100d);
frame.out().print(formats -> {
    formats.withDecimalFormat(Double.class, "0.00;-0.00", 1);
});
```
<div class="frame"><pre class="frame">
   Index     |  Column-0  |  Column-1  |  Column-2  |  Column-3  |  Column-4  |  Column-5  |  Column-6  |  Column-7  |  Column-8  |  Column-9  |
------------------------------------------------------------------------------------------------------------------------------------------------
 2014-01-31  |     19.77  |     79.79  |     54.33  |     72.15  |     32.47  |     48.82  |     40.26  |     85.37  |     33.05  |     38.18  |
 2014-02-28  |     37.88  |     62.38  |      5.54  |     11.75  |     19.53  |     71.03  |      6.66  |     33.03  |     74.29  |     11.47  |
 2014-03-31  |      3.33  |     12.06  |     67.68  |     22.46  |     30.11  |     11.07  |     73.94  |     43.57  |     44.48  |     33.34  |
 2014-04-30  |     57.28  |     99.35  |     51.99  |     53.83  |     74.58  |     14.18  |     85.94  |     91.67  |     52.23  |     34.07  |
 2014-05-31  |     81.53  |     82.24  |     71.24  |     89.30  |      7.24  |     75.58  |     74.63  |     18.16  |      7.31  |     71.79  |
 2014-06-30  |     98.89  |     34.17  |     92.35  |     44.58  |      6.92  |     53.81  |     30.58  |     30.46  |     14.15  |     34.93  |
 2014-07-31  |     23.99  |     75.76  |     93.12  |     65.59  |     23.22  |     83.26  |     10.02  |     99.18  |     11.73  |     43.27  |
 2014-08-31  |     43.93  |      9.30  |      6.24  |     18.98  |     28.80  |     42.37  |     37.73  |     74.29  |      8.99  |     78.51  |
 2014-09-30  |     98.85  |     45.26  |     34.00  |     35.86  |     78.51  |     49.17  |     56.03  |     61.06  |      5.50  |     55.80  |
 2014-10-31  |      5.48  |     10.46  |     65.42  |     20.80  |     49.96  |     21.22  |     76.53  |     56.08  |     11.83  |     28.27  |
 2014-11-30  |     84.84  |     37.78  |     95.47  |     42.30  |     20.87  |     89.41  |     12.20  |     27.29  |     64.71  |     65.12  |
 2014-12-31  |     22.34  |     93.66  |     97.76  |     45.17  |     39.16  |     71.14  |     31.19  |     65.24  |     46.66  |     88.43  |
</pre></div>

This kind of dataset may be typical of an experiment that captures monthly values and reports them at month end. Some 
hypothetical analysis involving this dataset may assume that for any date in February 2014 for example, the prior row applies,
and therefore we need to access the entry for 2014-01-31 (to avoid peak ahead). In this scenario, we know the dataset is 
reported at month end in all cases, so we could code up a rule such that when requesting a value for any given date, we adjust 
that date to the prior month-end. What if the data is reported at some random dates within the month rather than always at month 
end? In that case we would have to perform a linear search in the worst case to find an appropriate row.

The `DataFrameAxis` interface exposes a `lowerKey()` and `higherKey()` method that provides a fast mechanism to solve
this problem. On the assumption that the keys are sorted, calling `lowerKey()` with some date will yield an `Optional` 
match to the previous key. So in the context of the frame above, requesting the lower key of **2014-02-15** would yield
**2014-01-31**. Similarly, calling `higherKey()` with the same argument would yield **2014-02-28**.

The example below iterates over the first 35 days of the `DataFrame` row axis at a **daily** frequency, and attempts
to locate the previous row key. Since we know what to expect in this example, we can make some assertions on the resulting
keys returned from a call to `lowerKey()`, which serves as a usuful unit test.

<?prettify?>
```java
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
```
<div class="frame"><pre class="frame">
Exact match for: 2014-01-31
Lower match for 2014-02-01 is 2014-01-31
Lower match for 2014-02-02 is 2014-01-31
Lower match for 2014-02-03 is 2014-01-31
Lower match for 2014-02-04 is 2014-01-31
Lower match for 2014-02-05 is 2014-01-31
Lower match for 2014-02-06 is 2014-01-31
Lower match for 2014-02-07 is 2014-01-31
Lower match for 2014-02-08 is 2014-01-31
Lower match for 2014-02-09 is 2014-01-31
Lower match for 2014-02-10 is 2014-01-31
Lower match for 2014-02-11 is 2014-01-31
Lower match for 2014-02-12 is 2014-01-31
Lower match for 2014-02-13 is 2014-01-31
Lower match for 2014-02-14 is 2014-01-31
Lower match for 2014-02-15 is 2014-01-31
Lower match for 2014-02-16 is 2014-01-31
Lower match for 2014-02-17 is 2014-01-31
Lower match for 2014-02-18 is 2014-01-31
Lower match for 2014-02-19 is 2014-01-31
Lower match for 2014-02-20 is 2014-01-31
Lower match for 2014-02-21 is 2014-01-31
Lower match for 2014-02-22 is 2014-01-31
Lower match for 2014-02-23 is 2014-01-31
Lower match for 2014-02-24 is 2014-01-31
Lower match for 2014-02-25 is 2014-01-31
Lower match for 2014-02-26 is 2014-01-31
Lower match for 2014-02-27 is 2014-01-31
Exact match for: 2014-02-28
Lower match for 2014-03-01 is 2014-02-28
Lower match for 2014-03-02 is 2014-02-28
Lower match for 2014-03-03 is 2014-02-28
Lower match for 2014-03-04 is 2014-02-28
Lower match for 2014-03-05 is 2014-02-28
Lower match for 2014-03-06 is 2014-02-28
</pre></div>
