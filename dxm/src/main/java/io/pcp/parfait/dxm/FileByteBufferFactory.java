/*
 * Copyright 2009-2017 Aconex
 *
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.pcp.parfait.dxm;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class FileByteBufferFactory implements ByteBufferFactory {

    private final File file;

    public FileByteBufferFactory(File file) {
        this.file = file;
    }

    public ByteBuffer build(int length) throws IOException {
        RandomAccessFile fos = null;
        try {
            if (file.getParentFile().exists()) {
                file.delete();  /* directory update visible to MMV PMDA */
                if (file.exists()) {
                    throw new RuntimeException(
                            "Could not delete existing file "
                                    + file.getCanonicalPath());
                }
            } else if (!file.getParentFile().mkdirs()) {
                throw new RuntimeException(
                        "Could not create output directory "
                                + file.getParentFile().getCanonicalPath());
            }
            fos = new RandomAccessFile(file, "rw");
            fos.setLength(length);
            ByteBuffer tempDataFile = fos.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, length);
            tempDataFile.order(ByteOrder.nativeOrder());
            fos.close();

            return tempDataFile;
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    @Override
    public String toString() {
        try {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("file", file.getCanonicalPath()).toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
