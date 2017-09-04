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


import java.awt.*;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.testng.annotations.Test;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.viz.chart.Chart;

public class HistogramDocs {


    @Test()
    public void histogram1() throws Exception {
        final RealDistribution gaussian = new NormalDistribution();
        final Array<Double> sample = Array.of(Double.class, 100000).applyDoubles(v -> gaussian.sample());
        final int binCount = 50;
        final double minValue = sample.min().orElse(Double.NaN);
        final double maxValue = sample.max().orElse(Double.NaN);
        final double step = (maxValue - minValue) / binCount;
        System.out.printf("\nMin Value: %s", minValue);
        System.out.printf("\nMax Value: %s", maxValue);
        System.out.printf("\nStep Size: %s", step);

        final Range<Double> rowKeys = Range.of(minValue, maxValue + step, step);
        final DataFrame<Double,String> hist = DataFrame.ofInts(rowKeys, "Count");
        sample.forEachValue(v -> {
            hist.rows().lowerKey(v.getDouble()).ifPresent(lowerKey -> {
                final int rowOrdinal = hist.rows().ordinalOf(lowerKey);
                final int count = hist.data().getInt(rowOrdinal, 0);
                hist.data().setInt(rowOrdinal, 0, count + 1);
            });
        });

        Chart.create().withBarPlot(hist, false, chart -> {
            chart.title().withText("Histogram");
            chart.title().withFont(new Font("Arial", Font.PLAIN, 16));
            chart.plot().style("Count").withColor(Color.RED);
            chart.plot().data().at(0).withLowerDomainInterval(v -> v + step);
            chart.plot().axes().range(0).label().withText("Frequency");
            chart.plot().axes().domain().label().withText("Values");
            chart.show();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void hist2() throws Exception {
        final RealDistribution gaussian = new NormalDistribution();
        final Array<Integer> rowKeys = Range.of(0, 100000).toArray();
        final DataFrame<Integer,String> frame = DataFrame.ofDoubles(rowKeys, Array.of("Normal"), v -> gaussian.sample());
        Chart.create().withHistPlot(frame, 100, "Normal", chart -> {
            chart.title().withText("Gaussian Distribution, N(0,1)");
            chart.show();
        });
        Thread.currentThread().join();
    }

}
