package com.custardsource.parfait.dxm.semantics;

public enum Semantics {
    NO_SEMANTICS(0),
    COUNTER(1),
    INSTANT(3),
    DISCRETE(4);
    
    private final int pcpValue;

    Semantics(int pcpValue) {
        this.pcpValue = pcpValue;
    }
    
    public int getPcpValue() {
        return pcpValue;
    }
}
