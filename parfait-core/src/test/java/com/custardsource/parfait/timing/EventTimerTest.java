package com.custardsource.parfait.timing;

import junit.framework.TestCase;

import com.custardsource.parfait.MonitorableRegistry;
import com.custardsource.parfait.timing.EventCounters;

public class EventTimerTest extends TestCase {

    private EventTimer metricFactory;
    private Timeable workflowWizardControl;
    private Timeable logonControl;
    private Timeable attachmentControl;

    @Override
    protected void setUp() throws Exception {
        initControllers();
    }

    private void initControllers() {
        MonitorableRegistry.clearDefaultRegistry();
        metricFactory = new EventTimer();
        workflowWizardControl = new DummyTimeable();
        logonControl = new DummyTimeable();
        attachmentControl = new DummyTimeable();

    }

    public void testTotalMonitoredCounterSize() {

        metricFactory.registerTimeable(workflowWizardControl, "/WorkFlowWizard");

        /**
         * This total value includes the invocation count counter, which is stored separately to the
         * other counters in the counter set object. So when comparing the number of counters for
         * each controller, this should not be taken into consideration.
         */
        Integer totalControllerCounterSize = metricFactory.getNumberOfTotalControllerCounters();
        EventCounters wizardCounterSet = metricFactory
                .getCounterSetForController(workflowWizardControl);
        Integer numberOfMetricCounters = wizardCounterSet.numberOfControllerCounters();
        assertEquals("Should be the same number of per controller and total counters",
                --totalControllerCounterSize, numberOfMetricCounters);

        metricFactory.registerTimeable(logonControl, "/Logon");

        assertEquals(
                "Number of total controller counters should not change after adding controller",
                ++totalControllerCounterSize, metricFactory.getNumberOfTotalControllerCounters());
        EventCounters logonCounterSet = metricFactory
                .getCounterSetForController(logonControl);
        assertEquals("Should be the same number of per controller and total counters",
                --totalControllerCounterSize, logonCounterSet.numberOfControllerCounters());

        metricFactory.registerTimeable(attachmentControl, "/Attachments");
        assertEquals(
                "Number of total controller counters should not change after adding controller",
                ++totalControllerCounterSize, metricFactory.getNumberOfTotalControllerCounters());
        EventCounters attachmentCounterSet = metricFactory
                .getCounterSetForController(attachmentControl);
        assertEquals("Should be the same number of per controller and total counters",
                --totalControllerCounterSize, attachmentCounterSet.numberOfControllerCounters());

    }

    public void testTotalMonitoredCounterSingletons() {

        metricFactory.registerTimeable(logonControl, "/Logon");
        metricFactory.registerTimeable(workflowWizardControl, "/WorkflowWizard");
        metricFactory.registerTimeable(attachmentControl, "/Attachments");

        EventCounters wizardCounterSet = metricFactory
                .getCounterSetForController(workflowWizardControl);
        EventCounters logonCounterSet = metricFactory
                .getCounterSetForController(logonControl);
        EventCounters attachmentsCounterSet = metricFactory
                .getCounterSetForController(attachmentControl);

        assertNotNull("Couldnt obtain counter set for workflow wizard controller", wizardCounterSet);
        assertNotNull("Couldnt obtain counter set for logon controller", logonCounterSet);
        assertNotNull("Couldnt obtain counter set for attachments controller",
                attachmentsCounterSet);

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
