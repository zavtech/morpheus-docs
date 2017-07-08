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

public class Cars93Example {

    public static void main(String[] args) {
        DataFrame.read().csv(options -> {
            options.setResource("http://zavtech.com/data/samples/cars93.csv");
            options.setExcludeColumnIndexes(0);
        }).rows().select(row -> {
            double weightKG = row.getDouble("Weight") * 0.453592d;
            double horsepower = row.getDouble("Horsepower");
            return horsepower / weightKG > 0.1d;
        }).cols().add("MPG(Highway/City)", Double.class, v -> {
            double cityMpg = v.row().getDouble("MPG.city");
            double highwayMpg = v.row().getDouble("MPG.highway");
            return highwayMpg / cityMpg;
        }).rows().sort(false, "MPG(Highway/City)").write().csv(options -> {
            options.setFile("/Users/witdxav/cars93m.csv");
            options.setTitle("DataFrame");
        });

        DataFrame.read().<Integer>csv(options -> {
            options.setResource("/Users/witdxav/cars93m.csv");
            options.setExcludeColumnIndexes(0);
            options.setRowKeyParser(Integer.class, values -> Integer.parseInt(values[0]));
        }).out().print(10);
    }
}
