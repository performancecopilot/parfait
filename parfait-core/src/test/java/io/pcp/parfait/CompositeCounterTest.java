package io.pcp.parfait;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class CompositeCounterTest {
    @Test
    public void incrementActionsIncrementAllSubcounters() {
        SimpleCounter first = new SimpleCounter();
        SimpleCounter second = new SimpleCounter();

        CompositeCounter counter = new CompositeCounter(Arrays.asList(first, second));
        counter.inc();
        Assert.assertEquals(1, first.value);
        Assert.assertEquals(1, second.value);

        counter.inc(10);
        Assert.assertEquals(11, first.value);
        Assert.assertEquals(11, second.value);

    }

    private static final class SimpleCounter implements Counter {
        private int value;

        @Override
        public void inc() {
            inc(1);
        }

        @Override
        public void inc(long increment) {
            value += increment;
        }
    }
}
