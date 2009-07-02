/**
 * 
 */
package com.custardsource.parfait.dxm;


final class PcpString implements PcpOffset {
    private final String initialValue;
    private int offset;
    
    public PcpString(String value) {
        this.initialValue = value;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public void setOffset(int offset) {
        this.offset = offset;
    }

    String getInitialValue() {
        return initialValue;
    }
}