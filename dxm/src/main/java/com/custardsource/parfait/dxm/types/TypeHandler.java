package com.custardsource.parfait.dxm.types;

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
