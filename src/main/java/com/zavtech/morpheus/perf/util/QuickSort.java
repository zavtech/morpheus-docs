package com.zavtech.morpheus.perf.util;


import java.util.concurrent.ThreadLocalRandom;

public class QuickSort {

    public static void main(String[] args) {

        for (int k=0; k<5; ++k) {
            final double[] values = ThreadLocalRandom.current().doubles(10000000).toArray();
            final long t1 = System.currentTimeMillis();
            quickSort1(values, 0, values.length-1);
            final long t2 = System.currentTimeMillis();
            System.out.println("quickSort1() in " + (t2-t1) + " millis");
        }

        for (int k=0; k<5; ++k) {
            final double[] values = ThreadLocalRandom.current().doubles(10000000).toArray();
            final Swapper swapper = (i, j) -> { double v1 = values[i]; values[i] = values[j]; values[j] = v1; };
            final long t1 = System.currentTimeMillis();
            quickSort2(values, 0, values.length-1, Double::compare, swapper);
            final long t2 = System.currentTimeMillis();
            System.out.println("quickSort2() in " + (t2-t1) + " millis");
        }

    }


    static void quickSort1(double values[], int left, int right) {
        int i = left, j = right;
        double pivot = values[(left + right) / 2];
        while (i <= j) {
            while (values[i] < pivot) i++;
            while (values[j] > pivot) j--;
            if (i <= j) {
                double tmp = values[i];
                values[i] = values[j];
                values[j] = tmp;
                i++;
                j--;
            }
        }
        if (left < i - 1) {
            quickSort1(values, left, i - 1);
        }
        if (i < right) {
            quickSort1(values, i, right);
        }
    }


    static void quickSort2(double[] values, int left, int right, DoubleComparator comparator, Swapper swapper) {
        int i = left, j = right;
        double pivot = values[(left + right) / 2];
        while (i <= j) {
            while (comparator.compare(values[i], pivot) < 0) i++;
            while (comparator.compare(values[j], pivot) > 0) j--;
            if (i <= j) {
                swapper.swap(i, j);
                i++;
                j--;
            }
        }
        if (left < i - 1) {
            quickSort2(values, left, i - 1, comparator, swapper);
        }
        if (i < right) {
            quickSort2(values, i, right, comparator, swapper);
        }
    }


    interface DoubleComparator {

        int compare(double v1, double v2);
    }

    interface Swapper {

        void swap(int i, int j);
    }



}


