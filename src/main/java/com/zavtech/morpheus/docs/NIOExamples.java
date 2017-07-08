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

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.DoubleBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by witdxav on 14/01/16.
 */
public class NIOExamples {


    public static void main(String[] args) throws Exception {
        writeDoubles();
        readDoubles();
    }


    private static void writeDoubles() throws Exception {
        final int count = 10000000;
        final long bufferSize = count * 8L;
        final double[] values = new double[count];
        for (int i=0; i<count; ++i) values[i] = Math.random();

        File file = new File("/Users/witdxav/Downloads/MappedDoubles.mdf");
        FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
        MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, bufferSize);
        for (double value : values) byteBuffer.putDouble(value);
        channel.close();
    }

    private static void readDoubles() throws Exception {
        final File file = new File("/Users/witdxav/Downloads/MappedDoubles.mdf");
        final long bufferSize = file.length();
        final long t1 = System.currentTimeMillis();
        FileChannel channel = new RandomAccessFile(file, "r").getChannel();
        MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, bufferSize);
        DoubleBuffer db = byteBuffer.asDoubleBuffer();
        final int size = db.capacity();
        final double[] values = new double[size];
        for (int i=0; i<size; ++i) values[i] = db.get();
        final long t2 = System.currentTimeMillis();
        System.out.println("Loaded " + db.capacity() + " in " + (t2-t1) + " millis");
    }
}
