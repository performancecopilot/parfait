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

/**
 * <p>
 * A {@link StepMeasurementSink} which collects only a particular fraction of
 * events (e.g. for performance or log-space-saving reasons). A record is kept
 * of what fraction of 'candidate' StepMeasurements have already been passed to
 * the delegate; an incoming event will be passed through if and only if to
 * ignore it would take that fraction below the desired sampling fraction
 * specified at construction time.
 * </p>
 * <p>
 * Note that this class makes a slight sacrifice of accuracy for performance;
 * interleaving during increment of the internal counters may mean that very
 * slightly more or fewer events than the desired fraction end up being captured
 * in the long term.
 * </p>
 * <p>
 * Note also that this class will only pass on 'top-level' events (depth = 0).
 * </p>
 */
public class SamplingMeasurementSink implements StepMeasurementSink {
    private volatile long candidateEvents;
    private volatile long eventsSampled;
    private final float samplingFraction;
    private final StepMeasurementSink delegate;

    public SamplingMeasurementSink(StepMeasurementSink delegate, float samplingFraction) {
        this.samplingFraction = samplingFraction;
        this.delegate = delegate;
    }

    @Override
    public void handle(StepMeasurements measurements, int level) {
        if (level > 0) {
            return;
        }
        candidateEvents++;
        double fractionSampledSoFar = (double) eventsSampled / candidateEvents;

        if (fractionSampledSoFar < samplingFraction) {
            eventsSampled++;
            delegate.handle(measurements, level);
        }
    }
}
