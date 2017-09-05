package com.zavtech.morpheus.charts;

import java.time.Year;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.util.Collect;
import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.html.HtmlCode;

public class ChartEmbed {

    public static void main(String[] args) throws Exception {
        //Create some random data to plot
        final DataFrame<Year,String> frame = randomData();
        //Create the list of 4 charts, 2 JFree based, 2 Google based
        final List<Chart<?>> charts = createCharts(frame);
        //Generate Javascript code which can be embedded in an HTML page
        final String javascript = Chart.create().javascript(charts);
        //Code generate some HTML, embed the Javascript and create appropriate divs
        final HtmlCode htmlCode = new HtmlCode();
        htmlCode.newElement("html", html -> {
            html.newElement("head", head -> {
                head.newElement("script", script -> {
                    script.newAttribute("type", "text/javascript");
                    script.newAttribute("src", "https://www.gstatic.com/charts/loader.js");
                });
                head.newElement("script", script -> {
                    script.newAttribute("type", "text/javascript");
                    script.text(javascript);
                });
            });
            html.newElement("body", body -> {
                IntStream.range(0, 4).forEach(id -> {
                    body.newElement("div", div -> {
                        div.newAttribute("id", String.format("chart_%s", id));
                        div.newAttribute("style", "float:left;width:50%;height:400px;");
                    });
                });
            });
        });
        htmlCode.browse();
        System.out.println(htmlCode.toString());
    }


    /**
     * Returns a list of 4 charts, 2 swing based and 2 html based
     * @param frame     the data to plot in different forms
     * @return          the list of 4 charts
     */
    private static List<Chart<?>> createCharts(DataFrame<Year,String> frame) {
        return Collect.asList(
            Chart.create().asHtml().withLinePlot(frame, chart -> {
                chart.options().withId("chart_0");
                chart.title().withText("Single DataFrame Line Plot (Google)");
                chart.plot().axes().domain().label().withText("Year");
                chart.plot().axes().range(0).label().withText("Random Value");
                chart.legend().on().bottom();
            }),
            Chart.create().asHtml().withBarPlot(frame, true, chart -> {
                chart.options().withId("chart_1");
                chart.title().withText("Single DataFrame Bar Plot (Google)");
                chart.plot().axes().domain().label().withText("Year");
                chart.plot().axes().range(0).label().withText("Random Value");
                chart.legend().on().bottom();
            }),
            Chart.create().asSwing().withLinePlot(frame, chart -> {
                chart.options().withPreferredSize(600, 400).withId("chart_2");
                chart.title().withText("Single DataFrame Line Plot (JFree)");
                chart.plot().axes().domain().label().withText("Year");
                chart.plot().axes().range(0).label().withText("Random Value");
                chart.legend().on().bottom();
            }),
            Chart.create().asSwing().withBarPlot(frame, true, chart -> {
                chart.options().withPreferredSize(600, 400).withId("chart_3");
                chart.title().withText("Single DataFrame Bar Plot (JFree)");
                chart.plot().axes().domain().label().withText("Year");
                chart.plot().axes().range(0).label().withText("Random Value");
                chart.legend().on().bottom();
            })
        );
    }

    /**
     * Creates a DataFrame of random data with 4 series
     * @return      the DataFrame with chart data
     */
    private static DataFrame<Year,String> randomData() {
        final Array<Year> years = Range.of(2000, 2016).map(Year::of).toArray();
        return DataFrame.of(years, String.class, columns -> {
            Stream.of("A", "B", "C", "D").forEach(label -> {
                columns.add(label, Array.randn(years.length()).cumSum());
            });
        });
    }

}
