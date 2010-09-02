package com.custardsource.parfait.timing;

import junit.framework.TestCase;

public class StepMeasurementsTest extends TestCase {
    private static final String PARENT = "floop";
    private static final String CHILD = "bloop";
    private static final String OTHER_CHILD = "gloop";

    public void testNullParent() {
        StepMeasurements timing = new StepMeasurements(null, PARENT, null);
        assertNull(timing.getParent());
    }

    public void testParentSettingInConstructor() {
        StepMeasurements mummy = new StepMeasurements(null, PARENT, null);
        StepMeasurements timing = new StepMeasurements(mummy, CHILD, null);
        assertEquals(mummy, timing.getParent());
    }

    public void testBackTraceWithNoParentShowsOnlySelf() {
        StepMeasurements timing = new StepMeasurements(null, PARENT, null);
        assertEquals(PARENT, timing.getBackTrace());
    }

    public void testBackTraceWithParentShowsCorrectPath() {
        StepMeasurements mummy = new StepMeasurements(null, PARENT, null);
        StepMeasurements timing = new StepMeasurements(mummy, CHILD,
                null);
        assertEquals("floop/bloop", timing.getBackTrace());
    }

    public void testBackTraceShowsAction() {
        StepMeasurements timing = new StepMeasurements(null, PARENT,
                "scratchButt");
        assertEquals("floop:scratchButt", timing.getBackTrace());
    }

    public void testForwardTraceWithNoChildrenShowsOnlySelf() {
        StepMeasurements timing = new StepMeasurements(null, PARENT, null);
        assertEquals(PARENT, timing.getForwardTrace());
    }

    public void testForwardTraceWithOneChildShowsCorrectPath() {
        StepMeasurements timing = new StepMeasurements(null, PARENT,
                null);
        new StepMeasurements(timing, CHILD, null);
        assertEquals("floop/bloop", timing.getForwardTrace());
    }

    public void testForwardTraceWithMultipleChildrenShowsCorrectPath() {
        StepMeasurements timing = new StepMeasurements(null, PARENT,
                null);
        new StepMeasurements(timing, CHILD, null);
        new StepMeasurements(timing, OTHER_CHILD, null);
        assertEquals("floop/{bloop|gloop}", timing
                .getForwardTrace());
    }

    public void testForwardTraceShowsAction() {
        StepMeasurements timing = new StepMeasurements(null, PARENT,
                "scratchButt");
        assertEquals("floop:scratchButt", timing.getForwardTrace());
    }

}
