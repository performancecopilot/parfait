package io.pcp.parfait.timing;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EventMetricCollectorTest {

    private static final String TOP_LEVEL_GROUP = "eventGroup1";
    private static final String NESTED_GROUP = "eventGroup2";

    private EventMetricCollector collector;
    @Mock
    private StepMeasurementSink stepMeasurementSink;
    @Mock
    private EventCounters topLevelCounter;
    @Mock
    private EventCounters nestedCounter;
    @Mock
    private EventMetricCounters topLevelMetricCounters;

    @Before
    public void givenAnEventMetricCollector() {
        MockitoAnnotations.initMocks(this);

        Map<Object, EventCounters> perEventCounters = Maps.newHashMap();
        perEventCounters.put(TOP_LEVEL_GROUP, topLevelCounter);
        perEventCounters.put(NESTED_GROUP, nestedCounter);
        collector = new EventMetricCollector(perEventCounters, newArrayList(stepMeasurementSink));

        when(topLevelCounter.getEventGroupName()).thenReturn(TOP_LEVEL_GROUP);
        when(nestedCounter.getEventGroupName()).thenReturn(NESTED_GROUP);
        EventMetricCounters invocationCounter = mock(EventMetricCounters.class);
        when(topLevelCounter.getInvocationCounter()).thenReturn(invocationCounter);
        ThreadMetric threadMetric = mock(ThreadMetric.class);
        when(topLevelCounter.getMetricSources()).thenReturn(newArrayList(threadMetric));
        when(topLevelCounter.getCounterForMetric(threadMetric)).thenReturn(topLevelMetricCounters);
    }

    @Test
    public void itShouldIncrementCountersForSingleCollectors() {
        collector.startTiming(TOP_LEVEL_GROUP, "topLevelEvent");
        collector.stopTiming();

        verify(topLevelCounter).getCounterForMetric(any(ThreadMetric.class));
        verify(topLevelMetricCounters).incrementCounters(anyLong());
    }

    @Test
    public void itShouldIncrementTopLevelCountersForNestedCollectors() {
        collector.startTiming(TOP_LEVEL_GROUP, "topLevelEvent");
        collector.startTiming(NESTED_GROUP, "nestedEvent");
        collector.stopTiming();
        collector.stopTiming();

        verify(topLevelCounter).getCounterForMetric(any(ThreadMetric.class));
        verify(topLevelMetricCounters).incrementCounters(anyLong());
    }

    @Test
    public void itShouldNotIncrementNestedCountersForNestedCollectors() {
        collector.startTiming(TOP_LEVEL_GROUP, "topLevelEvent");
        collector.startTiming(NESTED_GROUP, "nestedEvent");
        collector.stopTiming();
        collector.stopTiming();

        verify(nestedCounter, never()).getCounterForMetric(any(ThreadMetric.class));
    }

}
