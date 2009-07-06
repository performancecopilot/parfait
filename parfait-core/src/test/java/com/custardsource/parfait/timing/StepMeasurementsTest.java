package com.custardsource.parfait.timing;

import junit.framework.TestCase;

public class StepMeasurementsTest extends TestCase {
    public void testNullParent() {
        StepMeasurements timing = new StepMeasurements(null, String.class, null);
        assertNull(timing.getParent());
    }

    public void testParentSettingInConstructor() {
        StepMeasurements mummy = new StepMeasurements(null, String.class, null);
        StepMeasurements timing = new StepMeasurements(mummy, String.class, null);
        assertEquals(mummy, timing.getParent());
    }

    public void testBackTraceWithNoParentShowsOnlySelf() {
        StepMeasurements timing = new StepMeasurements(null, String.class, null);
        assertEquals("String", timing.getBackTrace());
    }

    public void testBackTraceWithParentShowsCorrectPath() {
        StepMeasurements mummy = new StepMeasurements(null, String.class, null);
        StepMeasurements timing = new StepMeasurements(mummy, Integer.class,
                null);
        assertEquals("String/Integer", timing.getBackTrace());
    }

    public void testBackTraceShowsAction() {
        StepMeasurements timing = new StepMeasurements(null, String.class,
                "scratchButt");
        assertEquals("String:scratchButt", timing.getBackTrace());
    }

    public void testForwardTraceWithNoChildrenShowsOnlySelf() {
        StepMeasurements timing = new StepMeasurements(null, String.class, null);
        assertEquals("String", timing.getForwardTrace());
    }

    public void testForwardTraceWithOneChildShowsCorrectPath() {
        StepMeasurements timing = new StepMeasurements(null, Integer.class,
                null);
        new StepMeasurements(timing, String.class, null);
        assertEquals("Integer/String", timing.getForwardTrace());
    }

    public void testForwardTraceWithMultipleChildrenShowsCorrectPath() {
        StepMeasurements timing = new StepMeasurements(null, Integer.class,
                null);
        new StepMeasurements(timing, String.class, null);
        new StepMeasurements(timing, Float.class, null);
        assertEquals("Integer/{String|Float}", timing
                .getForwardTrace());
    }

    public void testForwardTraceShowsAction() {
        StepMeasurements timing = new StepMeasurements(null, String.class,
                "scratchButt");
        assertEquals("String:scratchButt", timing.getForwardTrace());
    }

}
