/*
 * Copyright 2009-2017 Aconex
 *
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.pcp.parfait.timing;

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
