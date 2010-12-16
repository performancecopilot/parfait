package com.custardsource.parfait.pcp;

import com.custardsource.parfait.Monitor;
import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.MonitoringView;
import com.custardsource.parfait.ValueSemantics;
import com.custardsource.parfait.dxm.MetricName;
import com.custardsource.parfait.dxm.PcpWriter;
import com.custardsource.parfait.dxm.semantics.Semantics;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * PcpMonitorBridge bridges between the set of {@link Monitorable}s in the current system and a PCP
 * monitor agent. The bridge works by persisting any changes to a Monitorable into a section of
 * memory that is also mapped into the PCP monitor agents address space.
 */
public class PcpMonitorBridge implements MonitoringView {

    private static final Logger LOG = Logger.getLogger(PcpMonitorBridge.class);

    private static final TextSource DEFAULT_SHORT_TEXT_SOURCE = new MetricDescriptionTextSource();
    private static final TextSource DEFAULT_LONG_TEXT_SOURCE = new EmptyTextSource();

    public static final int UPDATE_QUEUE_SIZE = 1024;

    private static final Map<ValueSemantics, Semantics> SEMANTICS_MAP = ImmutableMap.of(
            ValueSemantics.CONSTANT, Semantics.DISCRETE, ValueSemantics.FREE_RUNNING,
            Semantics.INSTANT, ValueSemantics.MONOTONICALLY_INCREASING, Semantics.COUNTER);

    private final ArrayBlockingQueue<Monitorable<?>> monitorablesPendingUpdate = new ArrayBlockingQueue<Monitorable<?>>(
            UPDATE_QUEUE_SIZE);

    private final Monitor monitor = new PcpMonitorBridgeMonitor();
    private final MetricNameMapper mapper;
    private final TextSource shortTextSource;
    private final TextSource longTextSource;

    private volatile PcpWriter pcpWriter;
    private volatile boolean started;


    public PcpMonitorBridge(PcpWriter writer) {
        this(writer, MetricNameMapper.PASSTHROUGH_MAPPER, DEFAULT_SHORT_TEXT_SOURCE,
                DEFAULT_LONG_TEXT_SOURCE);
    }

    public PcpMonitorBridge(PcpWriter writer,
                            MetricNameMapper mapper, TextSource shortTextSource, TextSource longTextSource) {
        this.pcpWriter = Preconditions.checkNotNull(writer);
        this.mapper = Preconditions.checkNotNull(mapper);
        this.shortTextSource = Preconditions.checkNotNull(shortTextSource);
        this.longTextSource = Preconditions.checkNotNull(longTextSource);
    }

    @Override
    public void stopMonitoring(Collection<Monitorable<?>> monitorables) {
        for (Monitorable<?> monitorable : monitorables) {
            monitorable.removeMonitor(monitor);
        }
        this.started = false;

        pcpWriter.reset();

    }

    @Override
    public boolean isRunning() {
        return started;
    }

    public boolean hasUpdatesPending() {
        return monitorablesPendingUpdate.size() > 0;
    }

    // TODO synchronization checking

    @Override
    public void startMonitoring(Collection<Monitorable<?>> monitorables) {
        // TODO precondition check on whether this is already started or not
        try {
            for (Monitorable<?> monitorable : monitorables) {
                monitorable.attachMonitor(monitor);
                MetricName metricName = mapper.map(monitorable.getName());
                pcpWriter.addMetric(metricName,
                        convertToPcpSemantics(monitorable.getSemantics()), monitorable.getUnit(),
                        monitorable.get());
                pcpWriter.setMetricHelpText(metricName.getMetric(), shortTextSource.getText(
                        monitorable, metricName), longTextSource.getText(monitorable, metricName));
            }
            pcpWriter.start();

            this.started = true;

            LOG.info("PCP monitoring bridge started for writer [" + pcpWriter + "]");
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialise PCP monitoring bridge", e);
        }
    }

    private Semantics convertToPcpSemantics(ValueSemantics semantics) {
        return SEMANTICS_MAP.get(semantics);
    }

    /**
     * Responsible for adding any Monitorables that change to the queue of Monitorables that are
     * pending update. This class will never block, if the update queue is ever full then the we
     * just do nothing.
     */
    private class PcpMonitorBridgeMonitor implements Monitor {

        public void valueChanged(Monitorable<?> monitorable) {
            writeUpdate(monitorable);
        }
    }

    private void writeUpdate(Monitorable<?> monitorable) {
        PcpWriter writerCopy = pcpWriter;
        if (writerCopy == null) {
            return;
        }
        writerCopy.updateMetric(mapper.map(monitorable.getName()), monitorable.get());
    }

}
