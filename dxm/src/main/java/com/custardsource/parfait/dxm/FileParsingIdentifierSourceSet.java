package com.custardsource.parfait.dxm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.google.common.base.Supplier;

public class FileParsingIdentifierSourceSet extends StringParsingIdentifierSourceSet {
    // TODO explicit encoding
    /**
     * Reads instance values from files. Assumes platform default encoding.
     */
    public FileParsingIdentifierSourceSet(File instanceData, File metricData,
            IdentifierSourceSet fallbacks) {
        super(fileToStrings(instanceData), fileToStrings(metricData), fallbacks);
    }

    /**
     * Reads instance values from streams. Assumes platform default encoding.
     */
    public FileParsingIdentifierSourceSet(InputStream instanceData, InputStream metricData,
            IdentifierSourceSet fallbacks) {
        super(streamToStrings(instanceData), streamToStrings(metricData), fallbacks);
    }

    public FileParsingIdentifierSourceSet(Reader instanceData, Reader metricData,
            IdentifierSourceSet fallbacks) {
        super(readerToStrings(instanceData), readerToStrings(metricData), fallbacks);
    }

    /**
     * Reads instance values from streams. Assumes platform default encoding. Uses {@link Supplier}
     * to allow easy provision of classpath resources.
     */
    public FileParsingIdentifierSourceSet(Supplier<InputStream> instanceData,
            Supplier<InputStream> metricData, IdentifierSourceSet fallbacks) {
        super(streamToStrings(instanceData == null ? null : instanceData.get()),
                streamToStrings(metricData == null ? null : metricData.get()), fallbacks);
    }

    private static Iterable<String> fileToStrings(File data) {
        if (data == null) {
            return Collections.emptyList();
        }
        try {
            return readerToStrings(new FileReader(data));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Iterable<String> streamToStrings(InputStream input) {
        if (input == null) {
            return Collections.emptyList();
        }
        return readerToStrings(new InputStreamReader(input));
    }

    private static Iterable<String> readerToStrings(Reader reader) {
        if (reader == null) {
            return Collections.emptyList();
        }
        LineNumberReader lineReader = new LineNumberReader(reader);
        Collection<String> results = new ArrayList<String>();
        String line = null;
        try {
            while ((line = lineReader.readLine()) != null) {
                results.add(line);
            }
            lineReader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return results;
    }
}
