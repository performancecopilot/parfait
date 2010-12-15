package com.custardsource.parfait.dxm;

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
}
