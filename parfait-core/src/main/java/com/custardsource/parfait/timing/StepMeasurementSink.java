package com.custardsource.parfait.timing;

public interface StepMeasurementSink {
    void handle(StepMeasurements measurements, int level);
}
