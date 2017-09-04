### Introduction

The following sections provide some micro-benchmark timing and memory statistics for various 
styles of Morpheus `DataFrames`. These benchmarks where performed on a 2013 MacBook Pro with a 
Core i7 2.6Ghz Quad-core CPU, and 16GB of memory. As with all benchmarks, these figures should 
be taken with a pinch of salt as real-world performance could differ substantially for all sorts 
of reasons. They are however a reasonable set of observations to compare the relative cost of 
different operations, and also to get a sense of how the sequential versus parallel execution 
of these operations compares.

### DataFrame
#### Initialization

A `DataFrame` is a column store composed of Morpheus Arrays, so the performance statistics presented
[here](./arrays) should serve as a rough guide to extrapolate some crude expectations regarding 
`DataFrame` performance & memory. Nevertheless, there are many idiosyncratic operations on a `DataFrame`
for which the performance characteristics are worthwhile understanding, and this section attempts to
cover some of this ground.

The chart below presents the initialization times for `DataFrames` with 10 columns of double precision
values and row counts increasing from 1 million to 5 million rows. The series on this chart represent 
frames with differing types for the row axis, including `Integer`, `Date`, `Instant`, `LocalDateTime` 
and `ZonedDateTime` types. If you have read the [section](./arrays) on the comparative performance 
of Morpheus Arrays for these types, it is not surprising to see that the frame with a `ZonedDateTime` 
row axis is more expensive to create than one with an `Integer` axis. With that said, this chart
provides a comforting picture with regard to the fairly linear scalability of performance as the 
row count increases, and the absolute times involved are also re-assuring.

<p align="center">
    <img class="chart" src="../../images/frame/data-frame-init-times.png"/>
</p>
**Figure 1. Large DataFrame construction times can be affected by the complexity of the largest axis.**

#### Memory

When constructing `DataFrames` that are entirely composed of Morpheus arrays that are themselves
internally represented as primitives, it is fairly easy to estimate how much memory the corresponding 
frame will consume. Consider the `DataFrame` example in the previous section, which included 10 columns
of double precision values. For the case with 5 million rows, and therefore 50 million elements, one 
can reason that it would require at least `(((64 / 8) * 50,000,000) / 1024^2) = 381MB` for just the 
data alone. As it turns out, direct measurement of this frame using the Java `Instrumentation` API 
suggests the entire object requires more like 500MB of RAM. 

The additional 120MB in this case is likely to be almost entirely attributable to the row and column 
axis of the frame, and obviously much more the former than the latter. The row and column axis is 
each backed by a [Trove](http://trove.starlight-systems.com/) map of an appropriate type which maintains 
the index, as well as a Morpheus array that defines the order. A Trove `TIntIntHashMap` initialized with 
a capacity of 5 million elements consumes 62MB according to Java Instrumentation, so together this 
explains over 90% of the memory used by this frame. This implies very little object overhead by the 
supporting classes that make up the `DataFrame`, and also suggests that garbage collection times 
should be minimal, which we will discuss in a following section.

<p align="center">
    <img class="chart" src="../../images/frame/data-frame-memory.png"/>
</p>
**Figure 2. Memory usage for a DataFrame with 50 million 64-bit double precision values and various row key types.** 

As you would expect, a `DataFrame` is generally very friendly to the garbage collector, so even creating 
a large frame will not result in excessively long GC times. The chart below illustrates the times to 
deallocate the DataFrame in this test, and both the magnitude and the small sampling noise in these
results is comforting.

<p align="center">
    <img class="chart" src="../../images/frame/data-frame-gc-times.png"/>
</p>
**Figure 3. The GC times are small in magnitude and demonstrate low sampling noise in the results.** 

#### Row Iteration

Row iteration is generally very fast when using the `forEach()` function with a consumer to perform 
some relevant analysis on each row. For example, consider a DataFrame with 50 million rows and 4 
columns containing random double precision values. The chart below shows how performance compares 
between sequential and parallel execution for a routine that computes the arithmetic mean of each row. 

<p align="center">
    <img class="chart" src="../../images/frame/data-frame-row-iteration.png"/>
</p>
**Figure 4. The parallel version of the result shows up to 50 million rows processed per second.** 

The code to generate these results is as follows:

<?prettify?>
```java
//Sample size for timing statistics
int sample = 10;

//Create frame with 50 million rows of Random doubles
Range<Integer> rowKeys = Range.of(0, 10000000);
Array<String> colKeys = Array.of("A", "B", "C", "D", "E", "F", "H");
DataFrame<Integer,String> frame = DataFrame.ofDoubles(rowKeys, colKeys).applyDoubles(v -> Math.random());

//Time sequential and parallel computation of mean over all rows
DataFrame<String,String> timing = PerfStat.run(sample, TimeUnit.MILLISECONDS, false, tasks -> {
    tasks.put("Sequential", () -> {
        frame.sequential().rows().forEach(row -> row.stats().mean());
        return frame;
    });
    tasks.put("Parallel", () -> {
        frame.parallel().rows().forEach(row -> row.stats().mean());
        return frame;
    });
});

//Plot timing statistics as a bar chart
Chart.create().withBarPlot(timing, false, chart -> {
    chart.title().withText("Time to Compute Arithmetic Mean of 50 Million rows (Sample 10 times)");
    chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
    chart.plot().axes().domain().label().withText("Timing Statistic");
    chart.plot().axes().range(0).label().withText("Total Time in Milliseconds");
    chart.legend().on();
    chart.show();
});
```

#### Updating Elements

A common `DataFrame` operation is to apply a function to each element in order to update the value based on 
some condition. The example below illustrates a performance comparison of sequential versus parallel execution for
such a scenario. In this example, the `DataFrame` contains 200 million random double precision values which are
initialized with a random value between 0 and 1 using `Math.random()`. The test the times how long it takes
to iterate over all these values, and capping values at 0.5 if they exceed that value.The results show a large 
performance improvement through parallel execution, and one could expect even larger differences as the 
complexity of the apply function increases.

<p align="center">
    <img class="chart" src="../../images/frame/data-frame-apply-doubles.png"/>
</p>
**Figure 5. Inspecting 200 million DataFrame elements and capping values > 0.5 in a little over a second.** 

The code to generate these results is as follows:

<?prettify?>
```java
//Sample size for timing statistics
int count = 10;

//Create frame with 50 million rows of Random doubles
Range<Integer> rowKeys = Range.of(0, 50000000);
Array<String> colKeys = Array.of("A", "B", "C", "D");
DataFrame<Integer,String> frame = DataFrame.ofDoubles(rowKeys, colKeys).applyDoubles(v -> Math.random());

//Time sequential and parallel capping of all elements in the DataFrame
ToDoubleFunction<DataFrameValue<Integer,String>> cap = (v) -> v.getDouble() > 0.5 ? 0.5 : v.getDouble();
DataFrame<String,String> timing = PerfStat.run(count, TimeUnit.MILLISECONDS, true, tasks -> {
    tasks.beforeEach(() -> frame.applyDoubles(v -> Math.random()));
    tasks.put("Sequential", () -> frame.sequential().applyDoubles(cap));
    tasks.put("Parallel", () -> frame.parallel().applyDoubles(cap));
});

//Plot timing statistics as a bar chart
Chart.create().withBarPlot(timing, false, chart -> {
    chart.title().withText("Time to Cap 200 Million DataFrame Elements (Sample 10 times)");
    chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
    chart.plot().axes().domain().label().withText("Timing Statistic");
    chart.plot().axes().range(0).label().withText("Total Time in Milliseconds");
    chart.legend().on();
    chart.show();
});
```