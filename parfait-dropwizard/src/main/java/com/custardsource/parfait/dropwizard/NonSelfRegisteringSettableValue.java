package com.custardsource.parfait.dropwizard;

import javax.measure.unit.Unit;

import com.custardsource.parfait.AbstractMonitorable;
import com.custardsource.parfait.ValueSemantics;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class NonSelfRegisteringSettableValue<T> extends AbstractMonitorable<T> {

    private T value;

    @SuppressWarnings({"unchecked", "PMD.ExcessiveParameterList"})
    public NonSelfRegisteringSettableValue(String name, String description, Unit<?> unit, T initialValue, ValueSemantics valueSemantics) {
        super(name, description, (Class<T>) initialValue.getClass(), unit, valueSemantics);
        value = initialValue;
    }

    @Override
    public T get() {
        return value;
    }

    /**
     * Sets the current value of this Monitorable. Some level of optimization is
     * performed to ensure that spurious updates are not sent out (i.e. when the
     * new value is the same as the old), but this is optimized for performance
     * and no guarantee is made that such notifications will not be sent.
     */
    public void set(T newValue) {
        Preconditions.checkNotNull(newValue, "Monitored value can not be null");
        if (Objects.equal(this.value, newValue)) {
            return;
        }
        this.value = newValue;
        notifyMonitors();
    }
}
