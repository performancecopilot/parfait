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

package io.pcp.parfait.pcp;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;

import io.pcp.parfait.Monitorable;
import io.pcp.parfait.dxm.MetricName;
import io.pcp.parfait.dxm.ioutils.StringIterable;
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
