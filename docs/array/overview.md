### Introduction

In order to scale large datasets on the [Java Virtual Machine](https://en.wikipedia.org/wiki/Java_virtual_machine) 
(JVM) it is necessary to limit large arrays to primitive types, as these are far more efficient from a 
memory allocation & de-allocation perspective. The reason for this is that primitive arrays are represented 
as a single Object and a **contiguous block of memory**. Object arrays on the other hand not only incur the 
memory overhead of each object header in the array, but also impose a significant burden on the Garbage 
Collector. The section on [performance](performance) provides some hard numbers to demonstrate the comparative 
costs of primitive arrays and their boxed counterparts. Future versions of Java which will likely 
introduce support for value types as described [here](http://cr.openjdk.java.net/~jrose/values/shady-values.html),
and improved array support as described [here](http://cr.openjdk.java.net/~jrose/pres/201207-Arrays-2.pdf), 
may mitigate some of these concerns.

In order to address this circumstance, one of the fundamental building blocks of the Morpheus library 
is the `Array` interface, of which there exist many different implementations optimized to store various 
data types. Where possible, Morpheus `Arrays` map object types such as `LocalDate` to an appropriate
primitive value, namely a long in this case. In addition to this, support for dense, sparse and memory 
mapped (off heap) backing stores is included to enable flexible memory allocations for different 
circumstances. Adding support for additional `Array` types is also fairly straightforward, and is 
described in a later section.

Primitive collections libraries such as [Trove](http://trove.starlight-systems.com/) and [Goldman Sachs Collections](https://github.com/goldmansachs/gs-collections) 
provide very effective high performance data structures, and the Morpheus `Array` interface is not intended 
to compete with these libraries. In fact, sparse Morpheus Array implementations leverage the Trove library 
under the covers. A feature of Trove however is that each typed collection is represented by its own interface, 
such as `TIntList` for primitive integers and `TDoubleList` for primitive doubles, and they do not share a 
common interface. This makes it inconvenient to build generic APIs that can operate on multiple types of 
primitive collections, without creating lots of overloaded methods.

The Morpheus Array class is somewhat similar in design to a `java.sql.ResultSet` in that there are type 
specific read/write methods supporting primitives, as well as a generic object version that will box a value 
if its internal value is a primitive.  It is easy to interrogate an `Array` instance for its data type, and 
therefore use the appropriate accessors or mutators that will avoid boxing of primitive types where possible.

The following sections describe how to use Morpheus `Arrays`, and demonstrate some of the less obvious
features of the API.

### Construction

There are a number of ways of creating Morpheus `Array` instances, but the most general way is using
the `of` method as illustrated below. In this example, we create a dense, sparse and memory mapped array
by specifying the type, length and default value. To create a sparse `Array`, we simply provide a **load
factor < 1f** to indicate the array is not likely to be fully populated. Below, we signal that we expect
the sparse array to be half populated by declaring a load factor of 0.5f.

<?prettify?>
```java
//Create a dense array of double precision values with default value of NaN
Array<Double> denseArray = Array.of(Double.class, 1000, Double.NaN);
//Create a sparse array which we expect to be only half populated, default value = 0
Array<Double> sparseArray = Array.of(Double.class, 1000, 0d, 0.5f);
//Created a memory mapped array of double values using an anonymous file
Array<Double> mappedArray1 = Array.mmap(Double.class, 1000, Double.NaN);
//Created a memory mapped array of double values using a user specified file
Array<Double> mappedArray2 = Array.mmap(Double.class, 1000, Double.NaN, "test.dat");

//Assert that each array is of the type we expect
Assert.assertTrue(denseArray.type() == Double.class);
Assert.assertTrue(sparseArray.type() == Double.class);
Assert.assertTrue(mappedArray1.type() == Double.class);
Assert.assertTrue(mappedArray2.type() == Double.class);

//Assert that each array is of the style we expect
Assert.assertTrue(denseArray.style() == ArrayStyle.DENSE);
Assert.assertTrue(sparseArray.style() == ArrayStyle.SPARSE);
Assert.assertTrue(mappedArray1.style() == ArrayStyle.MAPPED);
Assert.assertTrue(mappedArray2.style() == ArrayStyle.MAPPED);

//Confirm all elements are initialized as expected for each Array
IntStream.range(0, 1000).forEach(i -> {
    Assert.assertTrue(Double.isNaN(denseArray.getDouble(i)));
    Assert.assertTrue(sparseArray.getDouble(i) == 0d);
    Assert.assertTrue(Double.isNaN(mappedArray1.getDouble(i)));
    Assert.assertTrue(Double.isNaN(mappedArray2.getDouble(i)));
});
```

There are also convenient methods for creating a **dense** `Array` given the values directly.
 
<?prettify?>
```java
Array<Boolean> booleans = Array.of(true, false, true, false, true, true);
Array<Integer> integers = Array.of(0, 1, 2, 3, 4, 5);
Array<Long> longs = Array.of(0L, 1L, 2L, 3L, 4L);
Array<Double> doubles = Array.of(0d, 1d, 2d, 3d, 4d, 5d);
Array<LocalDate> dates = Array.of(LocalDate.now(), LocalDate.now().plusDays(1));
```

To create a half populated sparse `Array` where even indices are non-zero, one could do something as follows:

<?prettify?>
```java
Array<Double> sparseArray = Array.of(Double.class, 1000, 0d, 0.5f).applyDoubles(v -> {
    return v.index() % 2 == 0 ? Math.random() : 0d;
});
```

### Access

Given that a Morpheus `Array` is represented by an interface, the elements of the array need to be accessed 
via methods, not via a [] operator. There exist getter and setter methods for `boolean`, `int`, `long`, 
`double` and a generic `object` type. Up casting is allowed in the sense that you can call `getDouble()` on 
`Array<Integer>` and the internal `int` value will be cast to a double, however the reverse will result in an 
`ArrayException`. That is, automatic down casting which can result in the loss of precision is now allowed.

<?prettify?>
```java
//Create a dense array of doubles
Array<Double> array = Array.of(0d, 1d, 2d, 3d, 4d, 5d);
//Set first element using a primitive
array.setDouble(0, 22d);
//Set second element using a boxed value
array.setValue(1, 33d);
//Read first element as primitive
Assert.assertEquals(array.getDouble(0), 22d);
//Read second element as generic boxed value
Assert.assertEquals(array.getValue(1), new Double(33d));
```

### Iteration

The Morpheus `Array` interface extends `Iterable` and therefore iteration can be done via the `forEach()` 
method. For very large arrays that are backed by primitive values, this may not always be optimal as it 
results in boxing each value. To that end, the `Array` interface exposes type specific `forEachXXX()` 
methods to allow fast iteration without any boxing cost. Consider the example below

<?prettify?>
```java
//Create dense array of 20K random doubles
Array<Double> array = Array.of(Double.class, 20000, Double.NaN).applyDoubles(v -> Math.random());
//Iterate values by boxing doubles
array.forEach(value -> Assert.assertTrue(value > 0d));
//Iterate values and avoid boxing
array.forEachDouble(v -> Assert.assertTrue(v > 0d));
```

This can also be performed using **parallel processing** which can provide a significant performance boost on 
multi-core processor architectures that are mostly the norm these days. In fact, many functions on a Morpheus 
`Array` are parallel aware, and can result in significant boosts in performance as illustrated in the section 
on [performance](performance). The Fork & Join framework is used internally as a divide and conquer algorithm.

<?prettify?>
```java
//Parallel iterate values by boxing doubles
array.parallel().forEach(value -> Assert.assertTrue(value > 0d));
//Parallel iterate values and avoid boxing
array.parallel().forEachDouble(v -> Assert.assertTrue(v > 0d));
```

When iterating over an `Array`, it is sometimes not only useful to have access to the **value** but also the 
**index** it is associated with. To facilitate this, the `Array` interface exposes a `forEachValue()` method
which takes a consumer of `ArrayValue<?>` objects, which itself declares various type specific primitive 
accessors, and also an `index()` method that yields the current ordinal for the iteration. Consider
the example below where we iterate over all values printing only those values at index 0, 1000, 2000 and
so on.

<?prettify?>
```java
//Print values at index 0, 1000, 2000 etc...
array.forEachValue(v -> {
    if (v.index() % 1000 == 0) {
        System.out.printf("\nValue = %s at index %s", v.getDouble(), v.index());
    }
});
```

An important feature of the above iteration method is that the `Consumer<ArrayValue<?>` always **receives the 
same instance** of the `ArrayValue` object, which between iterations is simply pointing at a different element 
in the underlying `Array`. For that reason, one must always treat `ArrayValue<?>` objects as **ephemeral** and 
only valid for the life of the method they are passed to. That is, do not attempt to collect `ArrayValue<?>` 
instances in a collection, as they will in fact all be the same instance! Parallel iteration is also supported 
by the `forEachValue()` method, but in this case there will be one instance of an `ArrayValue<?>` per thread to 
avoid any collisions.

<?prettify?>
```java
//Parallel Print values at index 0, 1000, 2000 etc...
array.parallel().forEachValue(v -> {
    if (v.index() % 1000 == 0) {
        System.out.printf("\nv = %s at %s", v.getDouble(), v.index());
    }
});
```

### Updating

Modifying individual elements of a Morpheus `Array` via type specific setters has already been discussed.
Often however it is useful to perform bulk updates on an `Array`, and to this end, various `applyXXX()` 
methods exist to enable this to be done while avoiding boxing once again. Consider the example below
where we create an `Array<Double>` of 1 million random doubles, and then proceed to cap the values at 50 using
`applyDoubles()`. All the `applyXXX()` methods take functions which accept `ArrayValue<?>` objects, 
which again should be treated as ephemeral and only valid for the life of the function they are passed
to.

<?prettify?>
```java
//Create dense array of 1 million doubles
Array<Double> array = Array.of(Double.class, 1000000, Double.NaN);
//Update with random values
array.applyDoubles(v -> Math.random() * 100d);
//Cap Values to be no larger than 50
array.applyDoubles(v -> Math.min(50d, v.getDouble()));
//Assert values are capped
array.forEachValue(v -> Assert.assertTrue(v.getDouble() <= 50d));
```

This can obviously be done in parallel since the order of operations in this case does not matter.

<?prettify?>
```java
//Parallel update with random values
array.parallel().applyDoubles(v -> Math.random() * 100d);
//Parallel Cap Values to be no larger than 50
array.parallel().applyDoubles(v -> Math.min(50d, v.getDouble()));
//Assert values are capped
array.parallel().forEachValue(v -> Assert.assertTrue(v.getDouble() <= 50d));
```

### Mapping

While the `applyXXX()` methods discussed in the previous section are used to modify the contents of
an existing `Array`, it is also useful to be able to map an array to some other representation. This
is essentially the same as mapping with Java 8 Streams, and is a fundamental feature of functional
programming. As with the `applyXXX()` methods, type specific `mapToXXX()` methods exist to enable
mapping to various primitive types without any need for boxing.
 
<?prettify?>
```java
//Initial random generator
Random random = new Random();
//Create Array of LocalDates with random offsets from today
Array<LocalDate> dates = Array.of(LocalDate.class, 100, null).applyValues(v -> {
    return LocalDate.now().minusDays(random.nextInt(1000));
});
//Map dates to date times with time set to 12:15
Array<LocalDateTime> dateTimes = dates.map(v -> v.getValue().atTime(LocalTime.of(12, 15)));
//Map dates to day count offsets from today
Array<Long> dayCounts = dates.mapToLongs(v -> ChronoUnit.DAYS.between(v.getValue(), LocalDate.now()));
//Check day counts resolve back to original dates
dayCounts.forEachValue(v -> {
    long dayCount = v.getLong();
    LocalDate expected = dates.getValue(v.index());
    LocalDate actual = LocalDate.now().minusDays(dayCount);
    Assert.assertEquals(actual, expected);
});
``` 

The mapping functions are also **parallel** aware.

<?prettify?>
```java
//Parallel map dates to day count offsets from today
Array<Long> dayCounts = dates.parallel().mapToLongs(v -> {
    LocalDate now = LocalDate.now();
    LocalDate value = v.getValue();
    return ChronoUnit.DAYS.between(value, now);
});
```

### Statistics

The `Array` interface exposes a `stats()` method which makes it easy to compute summary statistics 
on arrays that contain numerical data. Attempting to compute stats on non-numerical arrays will
result in an `ArrayException`. The table below enumerates the supported statistics.

| Method                    | Description                                                                                   | Details
|---------------------------|-----------------------------------------------------------------------------------------------|---------------------------------------------------------------------|
| count()                   | The number of observations, ignoring nulls                                                    |                                                                     |   
| min()                     | The minimum value, ignoring nulls                                                             | [Details](https://en.wikipedia.org/wiki/Sample_maximum_and_minimum) |
| max()                     | The maximim value, ignorning nulls                                                            | [Details](https://en.wikipedia.org/wiki/Sample_maximum_and_minimum) |
| mean()                    | The first moment, or the arithmetic mean or average, ignoring nulls                           | [Details](https://en.wikipedia.org/wiki/Mean)                       |
| variance()                | The un-biased variance or second moment, a measure of dispersion                              | [Details](https://en.wikipedia.org/wiki/Variance)                   |
| stdDev()                  | The un-biased standard deviation, a measure of dispersion                                     | [Details](https://en.wikipedia.org/wiki/Standard_deviation)         |
| skew()                    | The third moment, or skewness, a measure of the asymmetry in the distribution                 | [Details](https://en.wikipedia.org/wiki/Skewness)                   | 
| kurtosis()                | The fourth moment, or Kurtosis, a measure of the "tailedness" of the probability distribution | [Details](https://en.wikipedia.org/wiki/Kurtosis)                   |         
| median()                  | The value separating the higher half of the data, or 50th percentile                          | [Details](https://en.wikipedia.org/wiki/Median)                     |
| mad()                     | The Mean Absolute Deviation from a central point, another measure of dispersion               | [Details](https://en.wikipedia.org/wiki/Average_absolute_deviation) |
| sem()                     | The standard error of the mean                                                                | [Details](https://en.wikipedia.org/wiki/Standard_error)             |
| geoMean()                 | The geometric mean, another measure of central tendency                                       | [Details](https://en.wikipedia.org/wiki/Geometric_mean)             |
| sum()                     | The summation of all values, ignoring nulls                                                   | [Details](https://en.wikipedia.org/wiki/Summation)                  |
| sumOfSquares()            | The sum, over non-null observations, of the squared differences from the mean                 | [Details](https://en.wikipedia.org/wiki/Total_sum_of_squares)       | 
| autocorr(int lag)         | The autocorrelation, which is the correlation of a signal with a delayed copy of itself       | [Details](https://en.wikipedia.org/wiki/Autocorrelation)            |
| percentile(double nth)    | The percentile value below which n% of values fall, ignoring nulls                            | [Details](https://en.wikipedia.org/wiki/Percentile)                 |

Below we initialise an `Array` of double precision values with elements equal to their index and the
proceed to compute summary statistics.

<?prettify?>
```java
//Create dense array of 1 million doubles
Array<Double> array = Array.of(Double.class, 1000, Double.NaN).applyDoubles(ArrayValue::index);

//Compute stats
Assert.assertEquals(array.stats().count(), 1000d);
Assert.assertEquals(array.stats().min(), 0d);
Assert.assertEquals(array.stats().max(), 999d);
Assert.assertEquals(array.stats().mean(), 499.5d);
Assert.assertEquals(array.stats().variance(), 83416.66666666667d);
Assert.assertEquals(array.stats().stdDev(), 288.8194360957494d);
Assert.assertEquals(array.stats().skew(), 0d);
Assert.assertEquals(array.stats().kurtosis(), -1.2000000000000004d);
Assert.assertEquals(array.stats().median(), 499.5d);
Assert.assertEquals(array.stats().mad(), 250.00000000000003d);
Assert.assertEquals(array.stats().sem(), 9.133272505880171d);
Assert.assertEquals(array.stats().geoMean(), 0d);
Assert.assertEquals(array.stats().sum(), 499500.0d);
Assert.assertEquals(array.stats().sumSquares(), 3.328335E8);
Assert.assertEquals(array.stats().autocorr(1), 1d);
Assert.assertEquals(array.stats().percentile(0.5d), 499.5d);
```

### Searching

The Morpheus `Array` interface provides some useful functions to search for values given a user provided 
predicate. In addition, there are methods to perform binary searches to find a matching value, or to find 
the next smallest / largest value given a user provided value that may not even exist in the array. 

The first example below demonstrates how to find the first and last values in the array given some predicate.
Note that the predicate again accepts an `ArrayValue<T>` instance, which allows the index to be accessed as
well as the value in a way that can avoid boxing.

<?prettify?>
```java
//Create random with seed
Random random = new Random(3);
//Create dense array double precision values
Array<Double> array = Array.of(Double.class, 1000, Double.NaN).applyDoubles(v -> random.nextDouble() * 55d);

//Find first value above 50
Assert.assertTrue(array.first(v -> v.getDouble() > 50d).isPresent());
array.first(v -> v.getDouble() > 50d).ifPresent(v -> {
    Assert.assertEquals(v.getDouble(), 51.997892373318116d, 0.000001);
    Assert.assertEquals(v.index(), 9);
});

//Find last value above 50
Assert.assertTrue(array.last(v -> v.getDouble() > 50d).isPresent());
array.last(v -> v.getDouble() > 50d).ifPresent(v -> {
    Assert.assertEquals(v.getDouble(), 51.864302849037315d, 0.000001);
    Assert.assertEquals(v.index(), 992);
});
```

The next two examples are predicated on binary search, and for that to work the `Array` needs to be sorted, 
which can be done by calling one of the sort methods as shown below.

<?prettify?>
```java
//Sort the array for binary search
Array<Double> sorted = array.sort(true);
```

Knowing that the array is sorted, we can perform a binary search on a subset of the `Array` or the entire
`Array` by providing the start and end index for the search space. The example below picks a number of
indexes, selects the value for those indexes, and then proceeds to search for those values and assert
that we get a match at the expected location.

<?prettify?>
```java
//Perform binary search over entire array for various chosen values
IntStream.of(27, 45, 145, 378, 945).forEach(index -> {
    double value = sorted.getDouble(index);
    int actual = sorted.binarySearch(0, 1000, value);
    Assert.assertEquals(actual, index);
});
```

Another useful search feature that leverages binary search and therefore performs well, is to find the
closest value before or after some value, even if that value does not exist in the `Array`. The examples
below illustrate how to find the next and prior value. The result of this search is present as an `ArrayValue`
wrapped in an `Optional`, so the value and index are easily accessible.

<?prettify?>
```java
//Find next value given a value that does not actual exist in the array
IntStream.of(27, 45, 145, 378, 945).forEach(index -> {
    double value1 = sorted.getDouble(index);
    double value2 = sorted.getDouble(index+1);
    double mean = (value1 + value2) / 2d;
    //Find next value given a value that does not exist in the array
    Optional<ArrayValue<Double>> nextValue = sorted.next(mean);
    Assert.assertTrue(nextValue.isPresent());
    nextValue.ifPresent(v -> {
        Assert.assertEquals(v.getDouble(), value2);
        Assert.assertEquals(v.index(), index + 1);
    });
});

//Find prior value given a value that does not actual exist in the array
IntStream.of(27, 45, 145, 378, 945).forEach(index -> {
    double value1 = sorted.getDouble(index);
    double value2 = sorted.getDouble(index+1);
    double mean = (value1 + value2) / 2d;
    //Find prior value given a value that does not exist in the array
    Optional<ArrayValue<Double>> priorValue = sorted.previous(mean);
    Assert.assertTrue(priorValue.isPresent());
    priorValue.ifPresent(v -> {
        Assert.assertEquals(v.getDouble(), value1);
        Assert.assertEquals(v.index(), index);
    });
});
```

### Sorting

The `Array` interface provides various convenience methods to sort values in either ascending or descending
order or in some bespoke order according to some user defined `Comparator`. To demonstrate, let us first
create an `Array` initialized with random double precision values, including both positive and negative values
as follows:

<?prettify?>
```java
//Create random generator with seed
Random random = new Random(22);
//Create dense array double precision values
Array<Double> array = Array.of(Double.class, 1000, Double.NaN);
//Initialise with random values
array.applyDoubles(v -> {
    final double sign = random.nextDouble() > 0.5d ? 1d : -1d;
    return random.nextDouble() * 10 * sign;
});
```

Sorting these values in ascending or descending order is as trivial:

<?prettify?>
```java
//Sort ascending
array.sort(true);
//Sort descending
array.sort(false);
```

It is also possible to only sort a subset of the array in ascending or descending order:

<?prettify?>
```java
//Sort values between indexes 100 and 200 in ascending order
array.sort(100, 200, true);
//Sort values between indexes 100 and 200 in ascending order
array.sort(100, 200, false);
```

The most general sort method allows the user to specify the range of values to operate on, and also
provide a `Comparator` that accepts `ArrayValue` instances. This enables the user to write a `Comparator`
implementation that avoids boxing values, and also one that has access to the data values as well as their
index in the array. The code below shows how to sort our example `Array` by the **absolute value** of the
elements in the array, but only including items between index 100 (inclusive) and 200 (exclusive). We
then run a check to see the values are sorted as expected. Like with other functions that consume `ArrayValue`
objects, they should be treated as ephemeral and only valid for the life of the method invocation.

<?prettify?>
```java
//Sort by absolute ascending value
array.sort(100, 200, (v1, v2) -> {
    final double d1 = Math.abs(v1.getDouble());
    final double d2 = Math.abs(v2.getDouble());
    return Double.compare(d1, d2);
});

//Check values in range are sorted as expected
IntStream.range(101, 200).forEach(index -> {
    double prior = Math.abs(array.getDouble(index-1));
    double current = Math.abs(array.getDouble(index));
    int compare = Double.compare(prior, current);
    Assert.assertTrue(compare <= 0);
});
```

All the sorting methods on a Morpheus `Array` support **parallel** execution, which can significantly 
improve performance.

<?prettify?>
```java
//Parallel sort by absolute ascending value
array.parallel().sort(100, 200, (v1, v2) -> {
    final double d1 = Math.abs(v1.getDouble());
    final double d2 = Math.abs(v2.getDouble());
    return Double.compare(d1, d2);
});
```

### Copying

Being able to efficiently create **deep copies** of Morpheus `Arrays` either in their entirety or only including
a subset of the elements is supported via three overloaded `copy()` methods. The code examples below illustrate
these three cases, the first case copies the entire `Array`, the second creates a copy including only a range
of values, and the third creates a copy given specific indexes.

<?prettify?>
```java
//Create random generator with seed
Random random = new Random(22);
//Create dense array double precision values
Array<Double> array = Array.of(Double.class, 1000, Double.NaN).applyDoubles(v -> random.nextDouble());

//Deep copy of entire Array
Array<Double> copy1 = array.copy();
//Deep copy of subset of Array, start inclusive, end exclusive
Array<Double> copy2 = array.copy(100, 200);
//Deep copy of specific indexes
Array<Double> copy3 = array.copy(new int[] {25, 304, 674, 485, 873});

//Assert lengths as expected
Assert.assertEquals(copy1.length(), array.length());
Assert.assertEquals(copy2.length(), 100);
Assert.assertEquals(copy3.length(), 5);

//Asset values as expected
IntStream.range(0, 1000).forEach(i -> Assert.assertEquals(copy1.getDouble(i), array.getDouble(i)));
IntStream.range(0, 100).forEach(i -> Assert.assertEquals(copy2.getDouble(i), array.getDouble(i+100)));
IntStream.of(0, 5).forEach(i -> {
    switch (i) {
        case 0: Assert.assertEquals(copy3.getDouble(i), array.getDouble(25));   break;
        case 1: Assert.assertEquals(copy3.getDouble(i), array.getDouble(304));   break;
        case 2: Assert.assertEquals(copy3.getDouble(i), array.getDouble(674));   break;
        case 3: Assert.assertEquals(copy3.getDouble(i), array.getDouble(485));   break;
        case 4: Assert.assertEquals(copy3.getDouble(i), array.getDouble(873));   break;
        default:
    }
});
```

### Streams

While Morpheus `Arrays` offer many of the programmatic features available with Java 8 Streams, they by 
no means cover everything. Either way, being able to expose Morpheus `Arrays` as Java 8 Streams will 
always be useful for compatibility purposes with other libraries. To that end, the `stream()` method
provides access to type specific streams as shown in the code examples below.

<?prettify?>
```java
//Create Array of various types
Array<Integer> integers = Array.of(0, 1, 2, 3, 4, 5);
Array<Long> longs = Array.of(0L, 1L, 2L, 3L, 4L);
Array<Double> doubles = Array.of(0d, 1d, 2d, 3d, 4d, 5d);
Array<LocalDate> dates = Array.of(LocalDate.now(), LocalDate.now().plusDays(1));

//Create Java 8 streams of these Arrays
IntStream intStream = integers.stream().ints();
LongStream longStream = longs.stream().longs();
DoubleStream doubleStream = doubles.stream().doubles();
Stream<LocalDate> dateStream = dates.stream().values();
```

### Expanding

Java native arrays cannot be expanded, however the Morpheus `Array` interface does support this. The
internal implementations have no choice but to re-create and re-populate internally, however this
feature does provide a convenient mechanism for growing `Arrays` without having to do the heavy lifting
yourself. In addition, given the support for dense, sparse and memory mapped backing stores, the
expansion behaviour differs across styles. The `expand()` method takes the new length to grow the
`Array` to, and the new elements will be initialized with the default value specified for the array
upon creation.

<?prettify?>
```java
//Create array of random doubles, with defauly value of -1
Array<Double> array = Array.of(Double.class, 10, -1d).applyDoubles(v -> Math.random());
//Double the size of the array
array.expand(20);
//Confirm new length is as expected
Assert.assertEquals(array.length(), 20);
//Confirm new values initialized with default value
IntStream.range(10, 20).forEach(i -> Assert.assertEquals(array.getDouble(i), -1d));
```

### Filtering

Creating a filtered `Array` given some user defined predicate is a common programmatic requirement, and to
support this, a `filter()` method is provided that takes a `Predicate` which accepts `ArrayValue` instances.
This design again allows boxing of primitive values to be avoided, and also makes the index of the value
accessible should that factor into the filtering logic. The code below creates an `Array` of double
precision random values, and creates a filter which only includes values > 5. 

<?prettify?>
```java
//Create random generator with seed
Random random = new Random(2);
//Create array of random doubles, with default value NaN
Array<Double> array = Array.of(Double.class, 1000, Double.NaN).applyDoubles(v -> random.nextDouble() * 10d);
//Filter to include all values > 5
Array<Double> filter = array.filter(v -> v.getDouble() > 5d);
//Assert length as expected
Assert.assertEquals(filter.length(), 486);
//Assert all value are > 5
filter.forEachValue(v -> Assert.assertTrue(v.getDouble() > 5d));
```

### Read-Only

Native Java arrays are inherently mutable, however since Morpheus `Arrays` are represented by an interface
we can easily create a light-weight wrapper that only supports read operations on the underlying `Array`. This
is useful when one needs to expose the `Array` to external code but in a way that guarantees that code
cannot modify the array contents in any way. The `readOnly()` method call shown below generates this 
light-weight ready-only proxy.

<?prettify?>
```java
//Create array of random doubles, with default value NaN
Array<Double> array = Array.of(Double.class, 1000, Double.NaN).applyDoubles(v -> Math.random());
//Create a light-weight read only wrapper
Array<Double> readOnly = array.readOnly();
```

### Filling

Filling an `Array` either over a range of indexes or the entire array with a specific value is supported by
two overloaded `fill()` methods. Below we create a random `Array` initialized with all `NaN` values, and
then proceed to fill indexes 10 through 20 with a fixed value.

<?prettify?>
```java
//Create array of random doubles, with default value NaN
Array<Double> array = Array.of(Double.class, 1000, Double.NaN);
//Fill indexes 10-20 (inclusive - exclusive) with value 25
array.fill(25d, 10, 20);
//Check results
IntStream.range(10, 20).forEach(i -> {
    Assert.assertEquals(array.getDouble(i), 25d);
});
```

### Distinct

Finding the distinct elements in an array is easy enough as you can simply collect the values in a `Set<T>`,
however this would once again come with the cost of boxing when the `Array` is backed by a primitive type.
In order to avoid this, two overloaded `distinct()` methods are provided, one which returns a new `Array` with
all the distinct values, and the second returns a truncated array of distinct values based on a user specified 
limit. The code below illustrates and example of how to use these.

<?prettify?>
```java
//Create a random with seed
Random random = new Random(10);
//Create Array of random LocalDates, all elements initially null
Array<LocalDate> dates = Array.of(LocalDate.class, 1000);
//Populate with some random dates that will likely have duplicates
dates.applyValues(v -> LocalDate.now().plusDays(random.nextInt(20)));
//Generate distinct Array
Array<LocalDate> distinct1 = dates.distinct();
//Generate distinct limiting to first 5 matches
Array<LocalDate> distinct2 = dates.distinct(5);
//Check expected results
Assert.assertEquals(distinct1.length(), 20);
Assert.assertEquals(distinct2.length(), 5);
```

### Bounds

Computing the upper and lower bounds of an `Array` can be achieved in a number of ways, as shown by the code
below. Firstly the `bounds()` method computes the min/max in one pass, and will work for any value that is 
`Comparable`. Alternatively, the `min()` and `max()` methods perform the same logic however only return
the lower or upper bound respectively. Below we also confirm that the `stats()` interface to the `Array`
generates the same results since in this example we are using an array of double precision values. If the 
`Array` was non-numeric, such as `LocalDate` for example, then the stats interface would not work and result 
in an `ArrayException`.

<?prettify?>
```java
//Create a random with seed
Random random = new Random(21);
//Create Array of random doubles, all elements initially null
Array<Double> array = Array.of(Double.class, 1000).applyDoubles(v -> random.nextDouble() * 100);
//Compute upper and lower bounds in one pass
Optional<Bounds<Double>> bounds = array.bounds();
//Confirm we have bounds
Assert.assertTrue(bounds.isPresent());
//Confirm expected results
bounds.ifPresent(b -> {
    Assert.assertEquals(b.lower(), 0.13021930271921445);
    Assert.assertEquals(b.upper(), 99.9557586162974);
    Assert.assertEquals(b.lower(), array.min().get());
    Assert.assertEquals(b.upper(), array.max().get());
    Assert.assertEquals(b.lower(), array.stats().min());
    Assert.assertEquals(b.upper(), array.stats().max());
});
```

### Null Check

A convenience method named `isNull()` exists on the `Array` and `ArrayValue` interface. In the former case,
it takes the index of the array element to check for null, in the latter no arguments are required since the
`ArrayValue` is already pointing at some entry. This method is convenient as it can avoid accessing the value
to check for null, which may again incur a boxing cost. In addition, `Double.NaN` is considered to be null
so one can avoid this special null check case as shown by the example below.

<?prettify?>
```java
//Create a random with seed
Random random = new Random(21);
//Create Array of random doubles, all elements initially null
Array<Double> array = Array.of(Double.class, 1000).applyDoubles(v -> random.nextDouble() * 100);
//Set some values to NaN
array.fill(Double.NaN, 10, 20);
//Set some values to null, which is the same as NaN for double precision
array.fill(null, 20, 30);
//Filter out NaN values using is null
Array<Double> filtered = array.filter(v -> !v.isNull());
//Assert length
Assert.assertEquals(filtered.length(), array.length() - 20);
```

### Swapping

Swapping elements in an `Array` without having to access them directly is also supported, which means that optimized 
implementations, such as for the `LocalDate` type for example, can avoid boxing the internal representation (long 
epoch day values in the case of LocalDate). The `swap()` method is used as part of the sort algorithm, and a simple 
example of how to use it unrelated to sorting is shown below.

<?prettify?>
```java
//Create Array of doubles
Array<Double> array = Array.of(10d, 20d, 30d, 40d);
//Swap values
array.swap(0, 3);
//Assert values swapped
Assert.assertEquals(array.getDouble(0), 40d);
Assert.assertEquals(array.getDouble(3), 10d);
```