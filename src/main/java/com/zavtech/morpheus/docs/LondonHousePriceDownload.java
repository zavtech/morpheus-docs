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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.stream.IntStream;

/**
 * Downloads London house price data from the UK Land registry from 1995 through to 2014.
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public class LondonHousePriceDownload {

    private static String userHome = System.getProperty("user.home");
    private static String url = "http://prod.publicdata.landregistry.gov.uk.s3-website-eu-west-1.amazonaws.com/pp-%1$d.csv";

    public static void main(String[] args) {
        IntStream.range(1995, 2015).forEach(year -> {
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                final String urlString = String.format(url, year);
                final String fileName = String.format("uk-house-prices-%1$d.csv", year);
                final File file = new File(userHome, "uk-house-prices/" + fileName);
                file.getParentFile().mkdirs();
                bos = new BufferedOutputStream(new FileOutputStream(file));
                bis = new BufferedInputStream(new URL(urlString).openStream());
                System.out.println("Downloading house prices to " + file.getAbsolutePath());
                final byte[] buffer = new byte[(int)Math.pow(1024, 2)];
                while (true) {
                    final int read = bis.read(buffer);
                    if (read < 0) break;
                    else {
                        bos.write(buffer, 0, read);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (bis != null) bis.close();
                    if (bos != null) bos.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
