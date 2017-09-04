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
package com.zavtech.morpheus.docs.worldbank;

import java.time.LocalDate;
import java.util.Map;

import org.testng.annotations.Test;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.util.Collect;
import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.wb.source.WBIndicatorSource;

/**
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public class ClimateDocs {


    @Test()
    public void c02Plot() throws Exception {
        final WBIndicatorSource source = new WBIndicatorSource();
        final DataFrame<LocalDate,String> frame = source.read(options -> {
            options.setBatchSize(1000);
            options.setIndicator("EN.ATM.CO2E.PC");
            options.setStartDate(LocalDate.of(1970, 1, 1));
            options.setEndDate(LocalDate.of(2013, 1, 1));
            options.setCountries("JP", "US", "DE", "IT", "GB", "FR", "CA", "CN");
        });

        final Map<String,String> labelMap = Collect.asMap(map -> {
            map.put("JP", "Japan");
            map.put("US", "United States");
            map.put("DE", "Germany");
            map.put("IT", "Italy");
            map.put("GB", "United Kingdon");
            map.put("FR", "France");
            map.put("CA", "Canada");
            map.put("CN", "China");
        });

        frame.out().print();

        Chart.create().withBarPlot(frame.cols().mapKeys(col -> labelMap.get(col.key())), true, chart -> {
            chart.plot().data().at(0).withLowerDomainInterval(date -> date.plusYears(1));
            chart.plot().axes().domain().label().withText("Year");
            chart.plot().axes().range(0).label().withText("Metric tons per capita");
            chart.title().withText("CO2 emissions (metric tons per capita)");
            chart.subtitle().withText("Source: World Bank, Indicator: EN.ATM.CO2E.PC");
            chart.legend().on().bottom();
            chart.show();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void gdpPlot() throws Exception {

        final WBIndicatorSource source = new WBIndicatorSource();
        final DataFrame<LocalDate,String> frame = source.read(options -> {
            options.setBatchSize(1000);
            options.setIndicator("NY.GDP.PCAP.CD");
            options.setStartDate(LocalDate.of(1970, 1, 1));
            options.setEndDate(LocalDate.of(2016, 1, 1));
            options.setCountries("JP", "US", "DE", "IT", "GB", "FR", "CA", "CN");
        });

        final Map<String,String> labelMap = Collect.asMap(map -> {
            map.put("JP", "Japan");
            map.put("US", "United States");
            map.put("DE", "Germany");
            map.put("IT", "Italy");
            map.put("GB", "United Kingdon");
            map.put("FR", "France");
            map.put("CA", "Canada");
            map.put("CN", "China");
        });

        frame.out().print();

        Chart.create().withBarPlot(frame.cols().mapKeys(col -> labelMap.get(col.key())), true, chart -> {
            chart.plot().data().at(0).withLowerDomainInterval(date -> date.plusYears(1));
            chart.plot().axes().domain().label().withText("Year");
            chart.plot().axes().range(0).label().withText("GDP per capita (current US$)");
            chart.title().withText("GDP per capita (current US$)");
            chart.subtitle().withText("Source: World Bank, Indicator: NY.GDP.PCAP.CD");
            chart.legend().on().bottom();
            chart.show();
        });

        Thread.currentThread().join();

    }

}



