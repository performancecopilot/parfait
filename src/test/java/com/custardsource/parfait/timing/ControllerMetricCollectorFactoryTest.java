package com.custardsource.parfait.timing;

import junit.framework.TestCase;

import com.custardsource.parfait.MonitorableRegistry;
import com.custardsource.parfait.timing.ControllerMetricCollector.MonitoredCounterSet;

public class ControllerMetricCollectorFactoryTest extends TestCase {

    private ControllerMetricCollectorFactory metricFactory;
    private MetricCollectorController workflowWizardControl;
    private MetricCollectorController logonControl;
    private MetricCollectorController attachmentControl;

    @Override
    protected void setUp() throws Exception {
        initControllers();
    }

    private void initControllers() {
        MonitorableRegistry.clearDefaultRegistry();
        metricFactory = new ControllerMetricCollectorFactory();
        workflowWizardControl = new DummyMetricCollectionController();
        logonControl = new DummyMetricCollectionController();
        attachmentControl = new DummyMetricCollectionController();

    }

    public void testTotalMonitoredCounterSize() {

        metricFactory.addController(workflowWizardControl, "/WorkFlowWizard");

        /**
         * This total value includes the invocation count counter, which is stored separately to the
         * other counters in the counter set object. So when comparing the number of counters for
         * each controller, this should not be taken into consideration.
         */
        Integer totalControllerCounterSize = metricFactory.getNumberOfTotalControllerCounters();
        MonitoredCounterSet wizardCounterSet = metricFactory
                .getCounterSetForController(workflowWizardControl);
        Integer numberOfMetricCounters = wizardCounterSet.numberOfControllerCounters();
        assertEquals("Should be the same number of per controller and total counters",
                --totalControllerCounterSize, numberOfMetricCounters);

        metricFactory.addController(logonControl, "/Logon");

        assertEquals(
                "Number of total controller counters should not change after adding controller",
                ++totalControllerCounterSize, metricFactory.getNumberOfTotalControllerCounters());
        MonitoredCounterSet logonCounterSet = metricFactory
                .getCounterSetForController(logonControl);
        assertEquals("Should be the same number of per controller and total counters",
                --totalControllerCounterSize, logonCounterSet.numberOfControllerCounters());

        metricFactory.addController(attachmentControl, "/Attachments");
        assertEquals(
                "Number of total controller counters should not change after adding controller",
                ++totalControllerCounterSize, metricFactory.getNumberOfTotalControllerCounters());
        MonitoredCounterSet attachmentCounterSet = metricFactory
                .getCounterSetForController(attachmentControl);
        assertEquals("Should be the same number of per controller and total counters",
                --totalControllerCounterSize, attachmentCounterSet.numberOfControllerCounters());

    }

    public void testTotalMonitoredCounterSingletons() {

        metricFactory.addController(logonControl, "/Logon");
        metricFactory.addController(workflowWizardControl, "/WorkflowWizard");
        metricFactory.addController(attachmentControl, "/Attachments");

        MonitoredCounterSet wizardCounterSet = metricFactory
                .getCounterSetForController(workflowWizardControl);
        MonitoredCounterSet logonCounterSet = metricFactory
                .getCounterSetForController(logonControl);
        MonitoredCounterSet attachmentsCounterSet = metricFactory
                .getCounterSetForController(attachmentControl);

        assertNotNull("Couldnt obtain counter set for workflow wizard controller", wizardCounterSet);
        assertNotNull("Couldnt obtain counter set for logon controller", logonCounterSet);
        assertNotNull("Couldnt obtain counter set for attachments controller",
                attachmentsCounterSet);

        for (ThreadMetric metric : wizardCounterSet.getMetrics().keySet()) {
            ControllerCounterSet wizardCounter = wizardCounterSet.getMetrics().get(metric);
            assertNotNull("Couldnt obtain wizard counter for metric " + metric.getMetricName(),
                    wizardCounter);
            ControllerCounterSet logonCounter = logonCounterSet.getMetrics().get(metric);
            assertNotNull("Couldnt obtain logon counter for metric " + metric.getMetricName(),
                    logonCounter);
            ControllerCounterSet attachmentsCounter = attachmentsCounterSet.getMetrics()
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
    
    public static class DummyMetricCollectionController implements MetricCollectorController {
        public void setMetricCollectorFactory(
                ControllerMetricCollectorFactory metricCollectorFactory) {
        }
    }
}
