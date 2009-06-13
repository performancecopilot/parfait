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

    public AbstractTypeHandler(MmvMetricType type) {
        this.type = type;
    }

    public MmvMetricType getMetricType() {
        return type;
    }
}