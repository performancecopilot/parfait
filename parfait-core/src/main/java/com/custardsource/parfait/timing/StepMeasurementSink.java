package com.custardsource.parfait.timing;

/**
 * Destination for measurements of an individual event timing step. Implementations must be threadsafe.
 */
public interface StepMeasurementSink {
    void handle(StepMeasurements measurements, int level);
}
