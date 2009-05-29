package com.custardsource.parfait.timing;

import junit.framework.TestCase;

public class ControllerMetricSetTest extends TestCase {
    public void testNullParent() {
        ControllerMetricSet timing = new ControllerMetricSet(null, String.class, null);
        assertNull(timing.getParent());
    }

    public void testParentSettingInConstructor() {
        ControllerMetricSet mummy = new ControllerMetricSet(null, String.class, null);
        ControllerMetricSet timing = new ControllerMetricSet(mummy, String.class, null);
        assertEquals(mummy, timing.getParent());
    }

    public void testBackTraceWithNoParentShowsOnlySelf() {
        ControllerMetricSet timing = new ControllerMetricSet(null, String.class, null);
        assertEquals("String", timing.getBackTrace());
    }

    public void testBackTraceWithParentShowsCorrectPath() {
        ControllerMetricSet mummy = new ControllerMetricSet(null, String.class, null);
        ControllerMetricSet timing = new ControllerMetricSet(mummy, Integer.class,
                null);
        assertEquals("String/Integer", timing.getBackTrace());
    }

    public void testBackTraceShowsAction() {
        ControllerMetricSet timing = new ControllerMetricSet(null, String.class,
                "scratchButt");
        assertEquals("String:scratchButt", timing.getBackTrace());
    }

    public void testForwardTraceWithNoChildrenShowsOnlySelf() {
        ControllerMetricSet timing = new ControllerMetricSet(null, String.class, null);
        assertEquals("String", timing.getForwardTrace());
    }

    public void testForwardTraceWithOneChildShowsCorrectPath() {
        ControllerMetricSet timing = new ControllerMetricSet(null, Integer.class,
                null);
        new ControllerMetricSet(timing, String.class, null);
        assertEquals("Integer/String", timing.getForwardTrace());
    }

    public void testForwardTraceWithMultipleChildrenShowsCorrectPath() {
        ControllerMetricSet timing = new ControllerMetricSet(null, Integer.class,
                null);
        new ControllerMetricSet(timing, String.class, null);
        new ControllerMetricSet(timing, Float.class, null);
        assertEquals("Integer/{String|Float}", timing
                .getForwardTrace());
    }

    public void testForwardTraceShowsAction() {
        ControllerMetricSet timing = new ControllerMetricSet(null, String.class,
                "scratchButt");
        assertEquals("String:scratchButt", timing.getForwardTrace());
    }

}
