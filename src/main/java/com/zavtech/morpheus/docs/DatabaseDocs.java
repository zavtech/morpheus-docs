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

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import javax.sql.DataSource;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.util.Tuple;
import com.zavtech.morpheus.util.sql.SQLExtractor;

public class DatabaseDocs {

    public void example1() throws Exception {

        //Ensure the JDBC driver is loaded
        Class.forName("org.h2.Driver");

        //Create a frame from a select statement
        DataFrame<Integer,String> frame = DataFrame.read().db(options -> {
            options.withConnection("jdbc:h2://databases/testDb", "sa", null);
            options.withSql("select * from Customer where city = 'London'");
        });
    }


    public void example2() {
        // Join products and inventory to see what we have where
        javax.sql.DataSource dataSource = getDataSource();
        DataFrame<Tuple,String> frame = DataFrame.read().db(options -> {
            options.withConnection(dataSource);
            options.withSql("select * from Product t1 inner join Inventory t2 on t1.productId = t2.productId");
            options.withExcludeColumns("productId", "warehouseId");  //not need as DataFrame columns, as part of row key
            options.withRowCapacity(1000);
            options.withRowKeyFunction(rs -> {
                try {
                    String productId = rs.getString("productId");
                    String warehouseId = rs.getString("warehouseId");
                    return Tuple.of(warehouseId, productId);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex.getMessage(), ex);
                }
            });
        });
    }


    public void example3() {
        //Convert the user's time zone into a ZoneId
        javax.sql.DataSource dataSource = getDataSource();
        DataFrame<Integer,String> frame = DataFrame.read().db(options -> {
            options.withConnection(dataSource);
            options.withSql("select * from Customer where city = ? and dob > ?");
            options.withParameters("London", LocalDate.of(1970, 1, 1));
            options.withExtractor("UserZone", SQLExtractor.with(ZoneId.class, (rs, colIndex) -> {
                final String tzName = rs.getString(colIndex);
                return tzName != null ? ZoneId.of(tzName) : null;
            }));
        });
    }

    public DataSource getDataSource() {
        return null;
    }
}
