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

import java.util.Collections;

import io.pcp.parfait.MonitorableRegistry;
import junit.framework.TestCase;

public class EventTimerTest extends TestCase {

    private EventTimer metricFactory;
    private Timeable workflowWizardControl;
    private Timeable logonControl;
    private Timeable attachmentControl;

    @Override
    protected void setUp() throws Exception {
        initEvents();
    }

    private void initEvents() {
        metricFactory = new EventTimer("test", new MonitorableRegistry(), ThreadMetricSuite
                .withDefaultMetrics(), true, true, Collections.<StepMeasurementSink>singletonList(new LoggerSink()));
        workflowWizardControl = new DummyTimeable();
        logonControl = new DummyTimeable();
        attachmentControl = new DummyTimeable();

    }

    public void testTotalMonitoredCounterSize() {

        metricFactory.registerTimeable(workflowWizardControl, "/WorkFlowWizard");

        /**
         * This total value includes the invocation count counter, which is stored separately to the
         * other counters in the counter set object. So when comparing the number of counters for
         * each event, this should not be taken into consideration.
         */
        Integer totalEventCounterSize = metricFactory.getNumberOfTotalEventCounters();
        EventCounters wizardCounterSet = metricFactory
                .getCounterSetForEventGroup(workflowWizardControl);
        Integer numberOfMetricCounters = wizardCounterSet.numberOfTimerCounters();
        assertEquals("Should be the same number of per event and total counters",
                --totalEventCounterSize, numberOfMetricCounters);

        metricFactory.registerTimeable(logonControl, "/Logon");

        assertEquals(
                "Number of total event counters should not change after adding event",
                ++totalEventCounterSize, metricFactory.getNumberOfTotalEventCounters());
        EventCounters logonCounterSet = metricFactory
                .getCounterSetForEventGroup(logonControl);
        assertEquals("Should be the same number of per event and total counters",
                --totalEventCounterSize, logonCounterSet.numberOfTimerCounters());

        metricFactory.registerTimeable(attachmentControl, "/Attachments");
        assertEquals(
                "Number of total event counters should not change after adding event",
                ++totalEventCounterSize, metricFactory.getNumberOfTotalEventCounters());
        EventCounters attachmentCounterSet = metricFactory
                .getCounterSetForEventGroup(attachmentControl);
        assertEquals("Should be the same number of per event and total counters",
                --totalEventCounterSize, attachmentCounterSet.numberOfTimerCounters());

    }

    public void testTotalMonitoredCounterSingletons() {

        metricFactory.registerTimeable(logonControl, "/Logon");
        metricFactory.registerTimeable(workflowWizardControl, "/WorkflowWizard");
        metricFactory.registerTimeable(attachmentControl, "/Attachments");

        EventCounters wizardCounterSet = metricFactory.getCounterSetForEventGroup(workflowWizardControl);
        EventCounters logonCounterSet = metricFactory.getCounterSetForEventGroup(logonControl);
        EventCounters attachmentsCounterSet = metricFactory
                .getCounterSetForEventGroup(attachmentControl);

        assertNotNull("Couldnt obtain counter set for workflow wizard event", wizardCounterSet);
        assertNotNull("Couldnt obtain counter set for logon event", logonCounterSet);
        assertNotNull("Couldnt obtain counter set for attachments event", attachmentsCounterSet);

        for (ThreadMetric metric : wizardCounterSet.getMetrics().keySet()) {
            EventMetricCounters wizardCounter = wizardCounterSet.getMetrics().get(metric);
            assertNotNull("Couldnt obtain wizard counter for metric " + metric.getMetricName(),
                    wizardCounter);
            EventMetricCounters logonCounter = logonCounterSet.getMetrics().get(metric);
            assertNotNull("Couldnt obtain logon counter for metric " + metric.getMetricName(),
                    logonCounter);
            EventMetricCounters attachmentsCounter = attachmentsCounterSet.getMetrics()
                    .get(metric);
            assertNotNull(
                    "Couldnt obtain attachments counter for metric " + metric.getMetricName(),
                    attachmentsCounter);

            assertEquals("Total counter for metric " + metric + " is not a singleton",
                    wizardCounter.getTotalCounter(), logonCounter.getTotalCounter());
            assertEquals("Total counter for metric " + metric + " is not a singleton", logonCounter
                    .getTotalCounter(), attachmentsCounter.getTotalCounter());

        }

    }

    public static class DummyTimeable implements Timeable {
        public void setEventTimer(EventTimer timer) {
        }
    }
}
