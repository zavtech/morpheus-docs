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
package com.zavtech.morpheus.docs;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.array.ArrayStyle;
import com.zavtech.morpheus.array.ArrayValue;

public class ArrayOverview {


    @Test()
    public void construction1() {

        //Create a dense array of double precision values with default value of NaN
        Array<Double> denseArray = Array.of(Double.class, 1000, Double.NaN);
        //Create a sparse array which we expect to be only half populated, default value = 0
        Array<Double> sparseArray = Array.of(Double.class, 1000, 0d, 0.5f);
        //Created a memory mapped array of double values using an anonymous file
        Array<Double> mappedArray1 = Array.map(Double.class, 1000, Double.NaN);
        //Created a memory mapped array of double values using a user specified file
        Array<Double> mappedArray2 = Array.map(Double.class, 1000, Double.NaN, "test.dat");

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
    }


    @Test
    public void construction2() {

        Array<Boolean> booleans = Array.of(true, false, true, false, true, true);
        Array<Integer> integers = Array.of(0, 1, 2, 3, 4, 5);
        Array<Long> longs = Array.of(0L, 1L, 2L, 3L, 4L);
        Array<Double> doubles = Array.of(0d, 1d, 2d, 3d, 4d, 5d);
        Array<LocalDate> dates = Array.of(LocalDate.now(), LocalDate.now().plusDays(1));
    }

    @Test()
    public void construction3() {
        Array<Double> sparseArray = Array.of(Double.class, 1000, 0d, 0.5f).applyDoubles(v -> {
            return v.index() % 2 == 0 ? Math.random() : 0d;
        });
    }

    @Test()
    public void access1() {
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
    }

    @Test()
    public void iteration1() {

        //Create dense array of 20K random doubles
        Array<Double> array = Array.of(Double.class, 20000, Double.NaN).applyDoubles(v -> Math.random());
        //Iterate values by boxing doubles
        array.forEach(value -> Assert.assertTrue(value > 0d));
        //Iterate values and avoid boxing
        array.forEachDouble(v -> Assert.assertTrue(v > 0d));

        //Parallel iterate values by boxing doubles
        array.parallel().forEach(value -> Assert.assertTrue(value > 0d));
        //Parallel iterate values and avoid boxing
        array.parallel().forEachDouble(v -> Assert.assertTrue(v > 0d));

        //Print values at index 0, 1000, 2000 etc...
        array.forEachValue(v -> {
            if (v.index() % 1000 == 0) {
                System.out.printf("\nv = %s at %s", v.getDouble(), v.index());
            }
        });

        //Parallel Print values at index 0, 1000, 2000 etc...
        array.parallel().forEachValue(v -> {
            if (v.index() % 1000 == 0) {
                System.out.printf("\nv = %s at %s", v.getDouble(), v.index());
            }
        });

    }

    @Test()
    public void cap() {
        //Create dense array of 1 million doubles
        Array<Double> array = Array.of(Double.class, 1000000, Double.NaN);
        //Update with random values
        array.applyDoubles(v -> Math.random() * 100d);
        //Cap Values to be no larger than 50
        array.applyDoubles(v -> Math.min(50d, v.getDouble()));
        //Assert values are capped
        array.forEachValue(v -> Assert.assertTrue(v.getDouble() <= 50d));

        //Parallel update with random values
        array.parallel().applyDoubles(v -> Math.random() * 100d);
        //Parallel Cap Values to be no larger than 50
        array.parallel().applyDoubles(v -> Math.min(50d, v.getDouble()));
        //Assert values are capped
        array.parallel().forEachValue(v -> Assert.assertTrue(v.getDouble() <= 50d));
    }


    @Test
    public void map() {

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
        //Parallel check day counts resolve back to original dates
        dayCounts.parallel().forEachValue(v -> {
            long dayCount = v.getLong();
            LocalDate expected = dates.getValue(v.index());
            LocalDate actual = LocalDate.now().minusDays(dayCount);
            Assert.assertEquals(actual, expected);
        });

        //Parallel map dates to day count offsets from today
        Array<Long> dayCounts2 = dates.parallel().mapToLongs(v -> {
            LocalDate now = LocalDate.now();
            LocalDate value = v.getValue();
            return ChronoUnit.DAYS.between(value, now);
        });

    }

    @Test()
    public void stats() {
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
    }



    @Test()
    public void search() {

        //Create random with seed
        Random random = new Random(3);
        //Create dense array double precision values
        Array<Double> array = Array.of(Double.class, 1000, Double.NaN).applyDoubles(v -> random.nextDouble() * 55d);

        //Find first value above
        Assert.assertTrue(array.first(v -> v.getDouble() > 50d).isPresent());
        array.first(v -> v.getDouble() > 50d).ifPresent(v -> {
            Assert.assertEquals(v.getDouble(), 51.997892373318116d, 0.000001);
            Assert.assertEquals(v.index(), 9);
        });

        //Find last value above
        Assert.assertTrue(array.last(v -> v.getDouble() > 50d).isPresent());
        array.last(v -> v.getDouble() > 50d).ifPresent(v -> {
            Assert.assertEquals(v.getDouble(), 51.864302849037315d, 0.000001);
            Assert.assertEquals(v.index(), 992);
        });

        //Sort the array for binary search
        Array<Double> sorted = array.sort(true);

        //Perform binary search over entire array for various chosen values
        IntStream.of(27, 45, 145, 378, 945).forEach(index -> {
            double value = sorted.getDouble(index);
            int actual = sorted.binarySearch(0, 1000, value);
            Assert.assertEquals(actual, index);
        });

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

    }


    @Test()
    public void sorting() {

        //Create random generator with seed
        Random random = new Random(22);
        //Create dense array double precision values
        Array<Double> array = Array.of(Double.class, 1000, Double.NaN);
        //Initialise with random values
        array.applyDoubles(v -> {
            final double sign = random.nextDouble() > 0.5d ? 1d : -1d;
            return random.nextDouble() * 10 * sign;
        });

        /*
        //Sort ascending
        array.sort(true);
        //Sort descending
        array.sort(false);

        //Sort values between indexes 100 and 200 in ascending order
        array.sort(100, 200, true);
        //Sort values between indexes 100 and 200 in ascending order
        array.sort(100, 200, false);
*/

        //Parallel sort by absolute ascending value
        array.parallel().sort(100, 200, (v1, v2) -> {
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
    }


    @Test()
    public void copying() {
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
    }


    @Test()
    public void streams() {
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
    }


    @Test()
    public void expand() {
        //Create array of random doubles, with defauly value of -1
        Array<Double> array = Array.of(Double.class, 10, -1d).applyDoubles(v -> Math.random());
        //Double the size of the array
        array.expand(20);
        //Confirm new length is as expected
        Assert.assertEquals(array.length(), 20);
        //Confirm new values initialized with default value
        IntStream.range(10, 20).forEach(i -> Assert.assertEquals(array.getDouble(i), -1d));
    }


    @Test()
    public void filtering() {
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
    }


    @Test()
    public void bounds() {

    }

    @Test()
    public void readOnly() {
        //Create array of random doubles, with default value NaN
        Array<Double> array = Array.of(Double.class, 1000, Double.NaN).applyDoubles(v -> Math.random());
        //Create a light-weight read only wrapper
        Array<Double> readOnly = array.readOnly();
    }


}
