package com.custardsource.parfait.pcp.types;


/**
 * Convenience TypeHandler to provide a simple implementation of {@link TypeHandler#getMetricType()}
 * 
 * @author Cowan
 * @param <JavaType>
 *            the Java type handled by this TypeHandler implementation
 */
public abstract class AbstractTypeHandler<JavaType> implements TypeHandler<JavaType> {
    private final MmvMetricType type;
	private final int dataLength;

    public AbstractTypeHandler(MmvMetricType type, int dataLength) {
        this.type = type;
        this.dataLength = dataLength;
    }

    public MmvMetricType getMetricType() {
        return type;
    }

	public int getDataLength() {
		return dataLength;
	}
}