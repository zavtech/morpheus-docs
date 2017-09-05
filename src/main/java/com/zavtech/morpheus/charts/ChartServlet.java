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

package com.zavtech.morpheus.charts;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.viz.chart.Chart;

/**
 * A basic Java Servlet example that demonstrates how to generate a PNG image of a chart
 */
public class ChartServlet extends javax.servlet.http.HttpServlet {


    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            final String title = request.getParameter("title");
            final Optional<String> widthParam = Optional.ofNullable(request.getParameter("width"));
            final Optional<String> heightParam = Optional.ofNullable(request.getParameter("height"));
            final Optional<String> transParam = Optional.ofNullable(request.getParameter("transparent"));
            final DataFrame<Integer,String> frame = loadData();
            //Only the swing based charts support PNG generation
            Chart.create().asSwing().withLinePlot(frame, "DataDate", chart -> {
                try {
                    final int width = widthParam.map(Integer::parseInt).orElse(700);
                    final int height = heightParam.map(Integer::parseInt).orElse(400);
                    final boolean transparent = transParam.map(Boolean::parseBoolean).orElse(true);
                    response.setContentType("image/png");
                    chart.title().withText(title);
                    chart.legend().on().bottom();
                    chart.plot().axes().domain().label().withText("Data Date");
                    chart.plot().axes().range(0).label().withText("Temperature");
                    chart.writerPng(response.getOutputStream(), width, height, transparent);
                } catch (Exception ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            });
        } catch (Exception ex) {
            response.sendError(500, ex.getMessage());
        }
    }

    /**
     * Loads the data for the chart to plot
     * @return      the DataFrame with chart data
     */
    private DataFrame<Integer,String> loadData() {
        final int rowCount = 1000;
        final LocalDate startDate = LocalDate.of(2013, 1, 1);
        final Range<Integer> rowKeys = Range.of(0, rowCount);
        final Range<LocalDate> dates = rowKeys.map(startDate::plusDays);
        return DataFrame.of(rowKeys, String.class, columns -> {
            columns.add("DataDate", dates);
            Stream.of("A", "B", "C", "D").forEach(label -> {
                columns.add(label, Array.randn(rowCount).cumSum());
            });
        });
    }
}
