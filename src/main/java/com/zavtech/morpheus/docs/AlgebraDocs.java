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
import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.testng.Assert;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.util.PerfStat;
import com.zavtech.morpheus.viz.chart.Chart;

/**
 * Code for Linear Algebra documentation
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public class AlgebraDocs {


    /**
     * Returns a DataFrame of random values
     * @param rowCount      the row count
     * @param colCount      the column count
     * @return              the random DataFrame
     */
    DataFrame<Integer,Integer> random(int rowCount, int colCount) {
        final Range<Integer> rowKeys = Range.of(0, rowCount);
        final Range<Integer> colKeys = Range.of(0, colCount);
        return DataFrame.ofDoubles(rowKeys, colKeys, v -> Math.random() * 100d);
    }


    @Test()
    public void dotProduct() {
        DataFrame<Integer,Integer> left = random(120, 100);
        DataFrame<Integer,Integer> right = random(100, 50);
        DataFrame<Integer,Integer> result = left.dot(right);
        Assert.assertEquals(result.rowCount(), left.rowCount());
        Assert.assertEquals(result.colCount(), right.colCount());
        result.out().print();
    }


    @Test()
    public void dotProductPerformance() throws Exception {

        int count = 10;
        DataFrame<Integer,Integer> left1 = random(1000, 1000);
        DataFrame<Integer,Integer> right = random(1000, 100);
        DataFrame<Integer,Integer> left2 = left1.parallel();

        DataFrame<String,String> timing = PerfStat.run(count, TimeUnit.MILLISECONDS, false, tasks -> {
            tasks.put("Sequential", () -> left1.dot(right));
            tasks.put("Parallel", () -> left2.dot(right));
        });

        //Plot timing statistics as a bar chart
        Chart.create().withBarPlot(timing, false, chart -> {
            chart.title().withText("DataFrame Dot Product (Sample 10 times)");
            chart.plot().axes().domain().label().withText("Timing Statistic");
            chart.plot().axes().range(0).label().withText("Total Time in Milliseconds");
            chart.legend().on();
            chart.writerPng(new File("./morpheus-docs/docs/images/data-frame-apply-doubles.png"), 845, 400, true);
            chart.show();
        });

        Thread.currentThread().join();
    }


}
