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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;

public class MemMappedExample {


    public static void main(String[] args) throws Exception {

        final File file = new File("/tmp/test-file.dat");

        DataOutputStream dos = null;
        try {
            final Random random = new Random();
            final String string1 = String.valueOf(random.nextDouble());
            final String string2 = String.valueOf(random.nextDouble());

            final boolean v1 = random.nextBoolean();
            final boolean v2 = random.nextBoolean();
            final double v3 = random.nextDouble();
            final long v4 = random.nextLong();
            final int v5 = random.nextInt();
            final float v6 = random.nextFloat();
            final int v7 = string1.getBytes().length;
            final String v8 = string1;
            final int v9 = string2.getBytes().length;
            final String v10 = string2;
            final double v11 = random.nextDouble();
            final boolean v12 = random.nextBoolean();



            dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            dos.writeBoolean(v1);
            dos.writeBoolean(v2);
            dos.writeDouble(v3);
            dos.writeLong(v4);
            dos.writeInt(v5);
            dos.writeFloat(v6);
            dos.writeInt(v7);
            dos.write(v8.getBytes());
            dos.writeInt(v9);
            dos.write(v10.getBytes());
            dos.writeDouble(v11);
            dos.writeBoolean(v12);
            dos.flush();
            dos.close();
            dos = null;

            System.out.println("Wrote: " + v1);
            System.out.println("Wrote: " + v2);
            System.out.println("Wrote: " + v3);
            System.out.println("Wrote: " + v4);
            System.out.println("Wrote: " + v5);
            System.out.println("Wrote: " + v6);
            System.out.println("Wrote: " + v8);
            System.out.println("Wrote: " + v10);
            System.out.println("Wrote: " + v11);
            System.out.println("Wrote: " + v12);

            final byte[] bytes = new byte[1024];
            final FileChannel fileChannel = new RandomAccessFile(file, "r").getChannel();
            final MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());

            System.out.println("");
            System.out.println("Read: " + (buffer.get() != 0));
            System.out.println("Read: " + (buffer.get() != 0));
            System.out.println("Read: " + buffer.getDouble());
            System.out.println("Read: " + buffer.getLong());
            System.out.println("Read: " + buffer.getInt());
            System.out.println("Read: " + buffer.getFloat());
            final int length1 = buffer.getInt();
            buffer.get(bytes, 0, length1);
            System.out.println("Read: " + new String(bytes, 0, length1));
            final int length2 = buffer.getInt();
            buffer.get(bytes, 0, length2);
            System.out.println("Read: " + new String(bytes, 0, length2));
            System.out.println("Read: " + buffer.getDouble());
            System.out.println("Read: " + (buffer.get() != 0));

        } finally {
            if (dos != null) {
                dos.close();
            }
        }

    }
}
