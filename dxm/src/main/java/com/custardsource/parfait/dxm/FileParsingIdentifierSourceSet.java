package com.custardsource.parfait.dxm;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;

import com.custardsource.parfait.dxm.ioutils.StringIterable;
import com.google.common.base.Supplier;

public class FileParsingIdentifierSourceSet extends StringParsingIdentifierSourceSet {
    // TODO explicit encoding
    /**
     * Reads instance values from files. Assumes platform default encoding.
     */
    public FileParsingIdentifierSourceSet(File instanceData, File metricData,
            IdentifierSourceSet fallbacks) {
        super(StringIterable.fromFile(instanceData), StringIterable.fromFile(metricData), fallbacks);
    }

    /**
     * Reads instance values from streams. Assumes platform default encoding.
     */
    public FileParsingIdentifierSourceSet(InputStream instanceData, InputStream metricData,
            IdentifierSourceSet fallbacks) {
        super(StringIterable.fromStream(instanceData), StringIterable.fromStream(metricData),
                fallbacks);
    }

    public FileParsingIdentifierSourceSet(Reader instanceData, Reader metricData,
            IdentifierSourceSet fallbacks) {
        super(StringIterable.fromReader(instanceData), StringIterable.fromReader(metricData),
                fallbacks);
    }

    /**
     * Reads instance values from streams. Assumes platform default encoding. Uses {@link Supplier}
     * to allow easy provision of classpath resources.
     */
    public FileParsingIdentifierSourceSet(Supplier<InputStream> instanceData,
            Supplier<InputStream> metricData, IdentifierSourceSet fallbacks) {
        super(StringIterable.fromStream(instanceData == null ? null : instanceData.get()),
                StringIterable.fromStream(metricData == null ? null : metricData.get()), fallbacks);
    }
}
