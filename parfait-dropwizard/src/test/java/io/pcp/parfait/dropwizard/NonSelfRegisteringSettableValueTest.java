package io.pcp.parfait.dropwizard;

import static tec.units.ri.AbstractUnit.ONE;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import javax.measure.quantity.Dimensionless;
import javax.measure.Unit;

import io.pcp.parfait.Monitor;
import io.pcp.parfait.ValueSemantics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NonSelfRegisteringSettableValueTest {

    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final Unit<Dimensionless> UNIT = ONE;
    private static final int INITIAL_VALUE = 12345;
    private static final int NEW_VALUE = 789;
    private static final ValueSemantics VALUE_SEMANTICS = ValueSemantics.CONSTANT;
    private NonSelfRegisteringSettableValue<Integer> value;

    @Mock
    private Monitor monitor;

    @Before
    public void setUp() {
        value = new NonSelfRegisteringSettableValue<>(NAME, DESCRIPTION, UNIT, INITIAL_VALUE, VALUE_SEMANTICS);
    }

    @Test
    public void initialValueWillBeReturnedBeforeAnyChange() {
        assertThat(value.get(), is(INITIAL_VALUE));
    }

    @Test
    public void settingValueWillUpdateValueReturned() {
        value.set(NEW_VALUE);
        assertThat(value.get(), is(NEW_VALUE));
    }

    @Test
    public void monitorsWillBeNotifiedWhenTheValueChanges() {
        value.attachMonitor(monitor);
        value.set(NEW_VALUE);
        verify(monitor).valueChanged(value);
    }

    @Test
    public void monitorsWillNotBeNotifiedWhenTheValueDoesNotChange() {
        value.attachMonitor(monitor);
        value.set(INITIAL_VALUE);
        verify(monitor, never()).valueChanged(value);
    }
}
