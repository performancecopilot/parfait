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

package io.pcp.parfait.dxm.types;

import java.nio.ByteBuffer;


/**
 * A class responsible for rendering a Java type into a byte format suitable for transfer to PCP.
 * 
 * @author Cowan
 * @param <JavaType>
 *            the Java type which can be converted by this handler
 */
public interface TypeHandler<JavaType> {
    /**
     * Render the provided value into the given ByteBuffer for access by PCP. No more than 16 bytes
     * should be written to the buffer, unless you want to overflow into the next metric.
     * 
     * @param buffer
     *            a ByteBuffer, positioned ready to receive the bytes representing this Java type to
     *            PCP
     * @param value
     *            the value to be converted
     */
    void putBytes(ByteBuffer buffer, JavaType value);

    /**
     * @return the {@link MmvMetricType} used by PCP to understand the bytes written by
     *         {@link #putBytes(ByteBuffer, Object)}
     */
    MmvMetricType getMetricType();

	int getDataLength();
	
	boolean requiresLargeStorage();
}
