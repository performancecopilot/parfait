package com.custardsource.parfait.pcp;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;

import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.dxm.MetricName;
import com.custardsource.parfait.dxm.ioutils.StringIterable;
import com.google.common.base.Supplier;

public class FileParsingTextSource implements TextSource {
    private final TextSource delegate;

    /**
     * Reads text from tab-separated file. Assumes platform default encoding.
     */
    public FileParsingTextSource(File data, TextSource fallback) {
        delegate = new StringParsingTextSource(StringIterable.fromFile(data), fallback);
    }

    /**
     * Reads text from tab-separated stream. Assumes platform default encoding.
     */
    public FileParsingTextSource(InputStream data, TextSource fallback) {
        delegate = new StringParsingTextSource(StringIterable.fromStream(data), fallback);
    }

    public FileParsingTextSource(Reader data, TextSource fallback) {
        delegate = new StringParsingTextSource(StringIterable.fromReader(data), fallback);
    }

    /**
     * Reads text from streams. Assumes platform default encoding. Uses {@link Supplier} to allow
     * easy provision of classpath resources.
     */
    public FileParsingTextSource(Supplier<InputStream> dataSupplier, TextSource fallback) {
        delegate = new StringParsingTextSource(StringIterable.fromStream(dataSupplier == null
                ? null : dataSupplier.get()), fallback);
    }

    @Override
    public String getText(Monitorable<?> monitorable, MetricName mappedName) {
        return delegate.getText(monitorable, mappedName);
    }

}
