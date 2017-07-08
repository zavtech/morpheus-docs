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

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.util.Predicates;
import com.zavtech.morpheus.util.text.parser.Parser;

public class ParsingExamples {


    public static DataFrame<Integer,String> examples() {

        //Parse file or classpath resource, with first row as header
        DataFrame<Integer,String> frame1 = DataFrame.read().csv("/temp/data.csv");

        //Parse URL, with first row as header
        DataFrame<Integer,String> frame2 = DataFrame.read().csv("http://www.domain.com/data?file.csv");

        //Parse file, with first row as header, and row keys parsed as LocalDates from the first column, index=0
        DataFrame<LocalDate,String> frame3 = DataFrame.read().csv(options -> {
            options.setResource("/temp/data.csv");
            options.setRowKeyParser(LocalDate.class, v -> LocalDate.parse(v[0]));
        });

        //Parse URL, with first row as header, and custom decoders for specific columns
        DataFrame<Integer,String> frame4 = DataFrame.read().csv(options -> {
            ZoneId london = ZoneId.of("Europe/London");
            Matcher matcher = Pattern.compile("([A-Z]{6})\\s+.+").matcher("");
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(london);
            options.setResource("http://www.domain.com/data?file.csv");
            options.setColumnType("Month", Month.class);
            options.setParser("Timestamp", Parser.forObject(ZonedDateTime.class, v -> ZonedDateTime.parse(v, format)));
            options.setParser("Prefix", Parser.forObject(String.class, v -> matcher.reset(v).matches() ? matcher.group(1) : null));
        });


        //Parse a file, with bespoke decoders for specific columns, and UTF-16 encoding
        DataFrame<LocalDate,String> frame5 = DataFrame.read().csv(options -> {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MMM/yyyy");
            options.setResource("/temp/data.csv");
            options.setRowKeyParser(LocalDate.class, v -> LocalDate.parse(v[0]));
            options.setParser("Date", Parser.ofLocalDate(format));
            options.setParser("Weight", Parser.forDouble(v -> Double.parseDouble(v) * 100d));
            options.setColumnType("Month", Month.class);
            options.setCharset(StandardCharsets.UTF_16);
        });

        //Parse a file, with UTF-16 encoding, a row predicate, and selected columns only
        DataFrame<LocalDate,String> frame6 = DataFrame.read().csv(options -> {
            options.setResource("/temp/data.csv");
            options.setRowKeyParser(LocalDate.class, v -> LocalDate.parse(v[0]));
            options.setCharset(StandardCharsets.UTF_16);
            options.setRowPredicate(values -> values[0].startsWith("2013-12"));
            options.setColNamePredicate(Predicates.in("Date", "Weight", "Price"));
        });


        //Parse a file, or classpath resource from Morpheus JSON format
        DataFrame<LocalDate,String> frame7 = DataFrame.read().json("/temp/data.json");

        //Parse a file, or classpath resource from Morpheus JSON format, selecting only a subset of rows & columns
        final Set<String> columns = Stream.of("Date", "PostCode", "Street", "County").collect(Collectors.toSet());
        DataFrame<LocalDate,String> frame8 = DataFrame.read().json(options -> {
            options.setResource("/temp/data.json");
            options.setCharset(StandardCharsets.UTF_16);
            options.setRowPredicate(rowKey -> rowKey.getDayOfWeek() == DayOfWeek.MONDAY);
            options.setColPredicate(columns::contains);
        });

        return null;
    }
}
