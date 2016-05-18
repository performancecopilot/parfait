package io.pcp.parfait.dxm.ioutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class StringIterable implements Iterable<String> {
    private final Reader reader;
    
    private StringIterable(Reader reader) {
        this.reader = (reader == null ? new StringReader("") : reader);
    }
    
    @Override
    public Iterator<String> iterator() {
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
        return results.iterator();
    }

    public static StringIterable fromFile(File data) {
        try {
            return new StringIterable(data == null ? null: new FileReader(data));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static StringIterable fromStream(InputStream input) {
            return new StringIterable(input == null ? null: new InputStreamReader(input));
    }

    public static StringIterable fromReader(Reader input) {
        return new StringIterable(input); 
    }
}

