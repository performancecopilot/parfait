/**
 * 
 */
package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.types.TypeHandler;

public final class PcpValueInfo implements PcpOffset {
	private final MetricName metricName;
	private final Object initialValue;
	private final PcpMetricInfo metricInfo;
	private final Instance instance;
	private final PcpString largeValue;
	private int offset;

    PcpValueInfo(MetricName metricName, PcpMetricInfo metricInfo, Instance instance, 
    		Object initialValue, PcpMmvWriter basePcpWriter) {
        this.metricName = metricName;
        this.metricInfo = metricInfo;
        this.instance = instance;
        this.initialValue = initialValue;
        if (metricInfo.getTypeHandler().requiresLargeStorage()) {
            this.largeValue = basePcpWriter.createPcpString(initialValue.toString()); 
        } else {
            this.largeValue = null;
        }
    }

    public MetricName getMetricName() {
        return metricName;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public void setOffset(int offset) {
        this.offset = offset;
    }

    public TypeHandler<?> getTypeHandler() {
        return metricInfo.getTypeHandler();
    }

    public Object getInitialValue() {
        return initialValue;
    }

    int getInstanceOffset() {
        return instance == null ? 0 : instance.getOffset();
    }

    int getDescriptorOffset() {
        return metricInfo.getOffset();
    }
    
    PcpString getLargeValue() {
        return largeValue;
    }

}