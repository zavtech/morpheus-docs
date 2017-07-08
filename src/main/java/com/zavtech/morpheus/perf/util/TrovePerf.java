package com.zavtech.morpheus.perf.util;

import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.range.Range;

public class TrovePerf {
    public static void main(String[] args) {
        final Array<Integer> keys = Range.of(0, 10000000).toArray().shuffle(4);
        final TLongIntMap indexMap = new TLongIntHashMap(10000000);
        for (int i=0; i<keys.length(); ++i) {
            indexMap.put((long)keys.getInt(i), i);
        }
        final int[] indexes = new int[indexMap.size()];
        for (int i=0; i<5; ++i) {
            final long t1 = System.nanoTime();
            keys.forEachInt(x -> {
                final int value = indexMap.get(x);
                indexes[x] = value;
            });
            /*
            indexMap.forEachEntry((key, value) -> {
                indexes[value] = value;
                return true;
            });
            */
            final long t2 = System.nanoTime();
            System.out.println("Map traversal in " + ((t2-t1)/1000000) + " millis");
        }
    }
}
