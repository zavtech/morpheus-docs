package com.zavtech.morpheus.perf.index;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import com.zavtech.morpheus.index.Index;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.util.MemoryEstimator;

public class IndexMem {

    public static void main(String[] args) {
        final int size = 5000000;
        final TIntIntMap indexMap = new TIntIntHashMap(size, 0.75f, -1, -1);
        final MemoryEstimator estimator = new MemoryEstimator.DefaultMemoryEstimator();
        final long memory = estimator.getObjectSize(indexMap);
        System.out.println("Index Size = " + (memory / (1024 * 1024)) + "MB");
    }
}
