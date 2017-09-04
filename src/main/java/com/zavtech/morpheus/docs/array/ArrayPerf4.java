package com.zavtech.morpheus.docs.array;

import java.awt.Font;
import java.io.File;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.zavtech.morpheus.viz.chart.Chart;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.stats.StatType;
import com.zavtech.morpheus.util.PerfStat;

public class ArrayPerf4 {

    public static void main(String[] args) {

        final int count = 5;
        final int size = 1000000;

        final List<Class<?>> types = Arrays.asList(int.class, long.class, double.class, Date.class, LocalDate.class, String.class, ZonedDateTime.class);
        final Array<String> colKeys = Array.of("Native", "Morpheus (sequential)", "Morpheus (parallel)");
        final List<String> rowKeys = types.stream().map(Class::getSimpleName).collect(Collectors.toList());
        final DataFrame<String,String> memory = DataFrame.ofDoubles(rowKeys, colKeys);
        final DataFrame<String,String> times = DataFrame.ofDoubles(rowKeys, colKeys);
        types.forEach(type -> {
            for (int style : new int[] {0, 1, 2}) {
                System.out.println("Running tests for " + type);
                final String key = type.getSimpleName();
                final Callable<Object> callable = createCallable(style, type, size);
                final PerfStat stats = PerfStat.run(key, count, TimeUnit.MILLISECONDS, callable);
                final double runTime = stats.getCallTime(StatType.MEDIAN);
                times.data().setDouble(key, style, runTime);
                memory.data().setDouble(key, style, stats.getUsedMemory(StatType.MEDIAN));
            }
        });

        Chart.create().withBarPlot(times, false, chart -> {
            chart.title().withText("Native vs Morpheus (Sequential & Parallel) Median Initialisation Times (ex-GC), 1 Million Entries (Sample " + count + ")");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Data Type");
            chart.plot().axes().range(0).label().withText("Time (Milliseconds)");
            chart.plot().orient().horizontal();
            chart.legend().on();
            chart.writerPng(new File("./docs/images/native-vs-morpheus-init-times.png"), 845, 400, true);
            chart.show();
        });
    }

    private static Callable<Object> createCallable(int style, Class<?> type, int size) {
        switch (style) {
            case 0: return ArrayPerf4.createNativeCallable(type, size);
            case 1: return ArrayPerf4.createMorpheusCallable(type, size, false);
            case 2: return ArrayPerf4.createMorpheusCallable(type, size, true);
            default:  throw new IllegalArgumentException("Unsupported style: " + style);
        }
    }


    /**
     * Returns a newly created to test Morpheus Array iteration performance
     * @param type  the array element type
     * @param size  the size for the array
     * @return      the newly created Callable
     */
    static Callable<Object> createMorpheusCallable(Class<?> type, int size, boolean parallel) {
        if (type == boolean.class) {
            return () -> {
                final AtomicInteger count = new AtomicInteger();
                final Array<Boolean> array = parallel ? Array.of(Boolean.class, size).parallel() : Array.of(Boolean.class, size);
                array.applyBooleans(v -> Math.random() > 0.5);
                array.forEachBoolean(value -> {
                    if (!value) {
                        count.incrementAndGet();
                    }
                });
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == int.class) {
            return () -> {
                final AtomicInteger count = new AtomicInteger();
                final Random random = new Random();
                final Array<Integer> array = parallel ? Array.of(Integer.class, size).parallel() : Array.of(Integer.class, size);
                final int toFind = random.nextInt();
                array.applyInts(v -> random.nextInt());
                array.forEachInt(value -> {
                    if (value == toFind) {
                        count.incrementAndGet();
                    }
                });
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == long.class) {
            return () -> {
                final AtomicInteger count = new AtomicInteger();
                final Random random = new Random();
                final Array<Long> array = parallel ? Array.of(Long.class, size).parallel() : Array.of(Long.class, size);
                final long toFind = random.nextLong();
                array.applyLongs(v -> random.nextLong());
                array.forEachLong(value -> {
                    if (value == toFind) {
                        count.incrementAndGet();
                    }
                });
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == double.class) {
            return () -> {
                final AtomicInteger count = new AtomicInteger();
                final Array<Double> array = parallel ? Array.of(Double.class, size).parallel() : Array.of(Double.class, size);
                final double toFind = Math.random();
                array.applyDoubles(v -> Math.random());
                array.forEachDouble(value -> {
                    if (value == toFind) {
                        count.incrementAndGet();
                    }
                });
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == Date.class) {
            return () -> {
                final AtomicInteger count = new AtomicInteger();
                final long now = System.currentTimeMillis();
                final Array<Date> array = parallel ? Array.of(Date.class, size).parallel() : Array.of(Date.class, size);
                final Date toFind = new Date((long) (now * Math.random()));
                array.applyValues(v -> new Date((long) (now * Math.random())));
                array.forEach(value -> {
                    if (value.equals(toFind)) {
                        count.incrementAndGet();
                    }
                });
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == String.class) {
            return () -> {
                final AtomicInteger count = new AtomicInteger();
                final Array<String> array = parallel ? Array.of(String.class, size).parallel() : Array.of(String.class, size);
                final String toFind = String.valueOf(Math.random());
                array.applyValues(v -> String.valueOf(Math.random()));
                array.forEach(value -> {
                    if (value.equals(toFind)) {
                        count.incrementAndGet();
                    }
                });
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == LocalDate.class) {
            return () -> {
                final AtomicInteger count = new AtomicInteger();
                final long now = LocalDate.now().toEpochDay();
                final Array<LocalDate> array = parallel ? Array.of(LocalDate.class, size).parallel() : Array.of(LocalDate.class, size);
                final LocalDate toFind = LocalDate.ofEpochDay((long)(now * Math.random()));
                array.applyValues(v -> LocalDate.ofEpochDay((long)(now * Math.random())));
                array.forEach(value -> {
                    if (value.equals(toFind)) {
                        count.incrementAndGet();
                    }
                });
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == ZonedDateTime.class) {
            return () -> {
                final AtomicInteger count = new AtomicInteger();
                final ZonedDateTime now = ZonedDateTime.now();
                final Array<ZonedDateTime> array = parallel ? Array.of(ZonedDateTime.class, size).parallel() : Array.of(ZonedDateTime.class, size);
                final ZonedDateTime toFind = now.plusDays(2);
                array.applyValues(v -> now.plusSeconds(v.index()));
                array.forEach(value -> {
                    if (value.equals(toFind)) {
                        count.incrementAndGet();
                    }
                });
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else {
            throw new IllegalArgumentException("Unsupported type specified: " + type);
        }
    }


    /**
     * Returns a newly created to test Native Array iteration performance
     * @param type  the array element type
     * @param size  the size for the array
     * @return      the newly created Callable
     */
    static Callable<Object> createNativeCallable(Class<?> type, int size) {
        if (type == boolean.class) {
            return () -> {
                final AtomicInteger count = new AtomicInteger();
                final boolean[] array = new boolean[size];
                IntStream.range(0, size).forEach(i -> array[i] = Math.random() > 0.5);
                for (int i = 0; i < size; ++i) {
                    final boolean value = array[i];
                    if (!value) {
                        count.incrementAndGet();
                    }
                }
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == int.class) {
            return () -> {
                final AtomicInteger count = new AtomicInteger();
                final Random random = new Random();
                final int[] array = new int[size];
                IntStream.range(0, size).forEach(i -> array[i] = random.nextInt());
                final int toFind = random.nextInt();
                for (int i = 0; i < size; ++i) {
                    final int value = array[i];
                    if (value == toFind) {
                        count.incrementAndGet();
                    }
                }
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == long.class) {
            return () -> {
                final AtomicInteger count = new AtomicInteger();
                final Random random = new Random();
                final long[] array = new long[size];
                IntStream.range(0, size).forEach(i -> array[i] = random.nextLong());
                final long toFind = random.nextLong();
                for (int i = 0; i < size; ++i) {
                    final long value = array[i];
                    if (value == toFind) {
                        count.incrementAndGet();
                    }
                }
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == double.class) {
            return () -> {
                final AtomicInteger count = new AtomicInteger();
                final double[] array = new double[size];
                IntStream.range(0, size).forEach(i -> array[i] = Math.random());
                final double toFind = Math.random();
                for (int i = 0; i < size; ++i) {
                    final double value = array[i];
                    if (value == toFind) {
                        count.incrementAndGet();
                    }
                }
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == Date.class) {
            return () -> {
                final AtomicInteger count = new AtomicInteger();
                final long now = System.currentTimeMillis();
                final Date[] array = new Date[size];
                IntStream.range(0, size).forEach(i -> array[i] = new Date((long)(now * Math.random())));
                final Date toFind = new Date((long)(now * Math.random()));
                for (int i=0; i<size; ++i) {
                    final Date value = array[i];
                    if (value.equals(toFind)) {
                        count.incrementAndGet();
                    }
                }
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == String.class) {
            return () -> {
                final AtomicInteger count = new AtomicInteger();
                final String[] array = new String[size];
                IntStream.range(0, size).forEach(i -> array[i] = String.valueOf(Math.random()));
                final String toFind = String.valueOf(Math.random());
                for (int i = 0; i < size; ++i) {
                    final String value = array[i];
                    if (value.equals(toFind)) {
                        count.incrementAndGet();
                    }
                }
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == LocalDate.class) {
            return () -> {
                final AtomicInteger count = new AtomicInteger();
                final long now = LocalDate.now().toEpochDay();
                final LocalDate[] array = new LocalDate[size];
                IntStream.range(0, size).forEach(i -> array[i] = LocalDate.ofEpochDay((long)(now * Math.random())));
                final LocalDate toFind = LocalDate.ofEpochDay((long)(now * Math.random()));
                for (int i = 0; i < size; ++i) {
                    final LocalDate value = array[i];
                    if (value.equals(toFind)) {
                        count.incrementAndGet();
                    }
                }
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == ZonedDateTime.class) {
            return () -> {
                final AtomicInteger count = new AtomicInteger();
                final ZonedDateTime now = ZonedDateTime.now();
                final ZonedDateTime[] array = new ZonedDateTime[size];
                for (int i=0; i<size; ++i) array[i] = now.plusSeconds(i);
                final ZonedDateTime toFind = now.plusDays(2);
                for (int i = 0; i < size; ++i) {
                    final ZonedDateTime value = array[i];
                    if (value.equals(toFind)) {
                        count.incrementAndGet();
                    }
                }
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else {
            throw new IllegalArgumentException("Unsupported type specified: " + type);
        }
    }

}
