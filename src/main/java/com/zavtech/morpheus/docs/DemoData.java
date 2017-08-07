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

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.util.Tuple;

public class DemoData {


    /**
     * Returns the ONS population dataset for UK boroughs
     * @return  the ONS population dataser
     */
    public static DataFrame<Tuple,String> loadPopulationDataset() {
        return DataFrame.read().csv(options -> {
            options.setResource("http://tinyurl.com/ons-population-year");
            options.setRowKeyParser(Tuple.class, row -> Tuple.of(Integer.parseInt(row[1]), row[2]));
            options.setExcludeColumns("Code");
            options.getFormats().setNullValues("-");
            options.setColumnType("All Males", Double.class);
            options.setColumnType("All Females", Double.class);
            options.setColumnType("All Persons", Double.class);
            options.setColumnType("[MF]\\s+\\d+", Double.class);
        });
    }

    /**
     * Returns the ONS population dataset for UK boroughs, and convert counts to weights
     * @return  the ONS population dataset, expressed as population weights
     */
    public static DataFrame<Tuple,String> loadPopulationDatasetWeights() {
        return DataFrame.read().<Tuple>csv(options -> {
            options.setResource("http://tinyurl.com/ons-population-year");
            options.setRowKeyParser(Tuple.class, row -> Tuple.of(Integer.parseInt(row[1]), row[2]));
            options.setExcludeColumns("Code");
            options.getFormats().setNullValues("-");
            options.setColumnType("All Males", Double.class);
            options.setColumnType("All Females", Double.class);
            options.setColumnType("All Persons", Double.class);
            options.setColumnType("[MF]\\s+\\d+", Double.class);
        }).applyValues(v -> {
           if (v.colKey().matches("[MF]\\s+\\d+")) {
               final double total = v.row().getDouble("All Persons");
               final double count = v.getDouble();
               return count / total;
           } else {
               return v.getValue();
           }
        });
    }



    /**
     * Returns the ATP match results for the year specified
     * @param year      the year for ATP results
     * @return          the ATP match results
     */
    public static DataFrame<Integer,String> loadTennisMatchData(int year) {
        return DataFrame.read().csv(options -> {
            options.setHeader(true);
            options.setResource("http://www.zavtech.com/data/tennis/atp/atp-" + year + ".csv");
            options.setExcludeColumns("ATP");
            options.setFormats(formats -> {
                formats.setDateFormat("Date", "dd/MMM/yy");
            });
        });
    }

}
