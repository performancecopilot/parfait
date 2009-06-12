package com.custardsource.parfait.pcp.types;

/**
 * Metric types as represented in mmv_stats.h (which in turn parallel pmapi.h)
 * 
 * @author Cowan
 */
public enum MmvMetricType {
    NOT_SUPPORTED(-1),
    I32(0),
    U32(1),
    I64(2),
    U64(3),
    FLOAT(4),
    DOUBLE(5),
    INTEGRAL(10),
    DISCRETE(11);

    private final int identifier;

    private MmvMetricType(int identifier) {
        this.identifier = identifier;
    }

    public int getIdentifier() {
        return this.identifier;
    }
}