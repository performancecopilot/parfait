package com.custardsource.parfait.timing;


/**
 * An object that implements some form of metric collection via a {@link EventTimer}.
 */
public interface Timeable {

    public void setEventTimer(EventTimer timer);

}
