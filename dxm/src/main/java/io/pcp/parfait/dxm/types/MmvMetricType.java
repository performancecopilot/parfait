package io.pcp.parfait.dxm.types;


/**
 * Metric types as represented in mmv_stats.h (which in turn parallel pmapi.h)
 * 
 * @author Cowan
 */
public enum MmvMetricType {
    NOT_SUPPORTED(-1, "unsupported"),
    I32(0, "int"),
    U32(1, "uint"),
    I64(2, "long"),
    U64(3, "ulong"),
    FLOAT(4, "float"),
    DOUBLE(5, "double"),
    STRING(6, "string");

    private final int identifier;
	private final String description;

    private MmvMetricType(int identifier, String description) {
        this.identifier = identifier;
        this.description = description;
    }

    public int getIdentifier() {
        return this.identifier;
    }

	public String getDescription() {
		return description;
	}
}