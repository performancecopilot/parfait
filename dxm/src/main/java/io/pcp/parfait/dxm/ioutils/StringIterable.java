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

