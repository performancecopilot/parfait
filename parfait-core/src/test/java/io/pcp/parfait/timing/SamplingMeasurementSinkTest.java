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

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SamplingMeasurementSinkTest {
    @Test
    public void zeroFractionShouldNeverSampleEvents() {
        CountingMeasurementSink counter = new CountingMeasurementSink();
        StepMeasurementSink sink = new SamplingMeasurementSink(counter, 0.0f);
        assertEventSinkCounts(sink, 0, counter, 0, 0, 0, 0, 0);
    }

    @Test
    public void oneFractionShouldSampleAllEventCounts() {
        CountingMeasurementSink counter = new CountingMeasurementSink();
        StepMeasurementSink sink = new SamplingMeasurementSink(counter, 1.0f);
        assertEventSinkCounts(sink, 0, counter, 1, 2, 3, 4, 5);
    }

    @Test
    public void eventsSampledShouldNeverExceedSpecifiedFraction() {
        CountingMeasurementSink counter = new CountingMeasurementSink();
        StepMeasurementSink sink = new SamplingMeasurementSink(counter, 0.33333f);
        assertEventSinkCounts(sink, 0, counter, 1, 1, 1, 2, 2, 2, 3, 3);
    }

    @Test
    public void nestedEventsShouldNotBeDelegated() {
        CountingMeasurementSink counter = new CountingMeasurementSink();
        StepMeasurementSink sink = new SamplingMeasurementSink(counter, 1.0f);
        assertEventSinkCounts(sink, 1, counter, 0, 0, 0);
    }

    private void assertEventSinkCounts(StepMeasurementSink sink, int eventLevel,
            CountingMeasurementSink counter, int... progressiveCounts) {
        int seen = 0;
        for (int progressiveCount : progressiveCounts) {
            sink.handle(null, eventLevel);
            seen++;
            assertEquals(
                    String.format("After %s events, %s should be sunk", seen, progressiveCount),
                    progressiveCount, counter.eventsSunk);
        }
    }

    private static class CountingMeasurementSink implements StepMeasurementSink {
        private int eventsSunk = 0;

        @Override
        public void handle(StepMeasurements measurements, int level) {
            eventsSunk++;
        }
    }
}
