/**
 * 
 */
package com.custardsource.parfait.dxm.semantics;

enum Dimension implements UnitValued {
    PER(-1),
    NONE(0),
    UNITS(1);

    private final int pcpDimension;

    Dimension(int pcpDimension) {
        this.pcpDimension = pcpDimension;
    }

    public int getPmUnitsValue() {
        return pcpDimension;
    }
}