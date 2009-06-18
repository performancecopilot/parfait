package com.custardsource.parfait.pcp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.custardsource.parfait.Monitor;
import com.custardsource.parfait.Monitorable;
import com.custardsource.parfait.MonitorableRegistry;
import com.custardsource.parfait.MonitoringView;
import com.custardsource.parfait.dxm.MetricName;
import com.custardsource.parfait.dxm.PcpAconexPmdaWriter;
import com.custardsource.parfait.dxm.PcpWriter;
import com.google.common.base.Preconditions;

/**
 * PcpMonitorBridge bridges between the set of {@link Monitorable}s in the current system and a PCP
 * monitor agent. The bridge works by persisting any changes to a Monitorable into a section of
 * memory that is also mapped into the PCP monitor agents address space.
 * <p>
 * The format of the shared address space is specified in an associated header file. A full
 * description of the communication protocol can be found under the issue ACX-5426.
 */
@ManagedResource
public class PcpMonitorBridge extends MonitoringView {

    private final Logger LOG = Logger.getLogger(PcpMonitorBridge.class);

    public static final int UPDATE_QUEUE_SIZE = 1024;

    private final ArrayBlockingQueue<Monitorable<?>> monitorablesPendingUpdate = new ArrayBlockingQueue<Monitorable<?>>(
    		UPDATE_QUEUE_SIZE);

    private final Monitor monitor = new PcpMonitorBridgeMonitor();

    private final Thread updateThread;

    private final String serverName;

    private final File dataFileDir;

    /*
     * Determines whether value changes detected are written out to an external file for external
     * monitoring by the Aconex PCP agent.
     */
    private boolean outputValuesToPCPFile = true;

    private boolean deleteFilesOnExit = false;

	private volatile PcpWriter pcpWriter;


    public PcpMonitorBridge(String serverName, String dataFileDir) {
    	this(serverName, dataFileDir, MonitorableRegistry.DEFAULT_REGISTRY);
    }

    public PcpMonitorBridge(String serverName, String dataFileDir, MonitorableRegistry registry) {
		super(registry);
		Preconditions.checkNotNull(registry);
		Preconditions.checkArgument(StringUtils.isNotBlank(serverName),
				"Sever name can not be blank");
		this.serverName = serverName;
		this.dataFileDir = new File(dataFileDir);
		if (!this.dataFileDir.exists()) {
			this.dataFileDir.mkdirs();
		}
		Preconditions.checkArgument(this.dataFileDir.isDirectory(),
				"dataFileDir [%s] is not a directory.", dataFileDir);
		this.updateThread = new Thread(new Updater());
		this.updateThread.setName("PcpMonitorBridge-Updater-" + serverName);
		this.updateThread.setDaemon(true);
    }

    @Override
    public void stopMonitoring(Collection<Monitorable<?>> monitorables) {
        pcpWriter = null;
        for (Monitorable<?> monitorable : monitorables) {
            monitorable.removeMonitor(monitor);
        }
    }

    public boolean hasUpdatesPending() {
        return monitorablesPendingUpdate.size() > 0;
    }

    @Override
    protected void startMonitoring(Collection<Monitorable<?>> monitorables) {
        try {
            File headerFile = getHeaderFile();
            File dataFile = getDataFile();
            
            if (isDeleteFilesOnExit()) {
                headerFile.deleteOnExit();
                dataFile.deleteOnExit();
            }
            
			pcpWriter = new PcpAconexPmdaWriter(headerFile, dataFile);
			
            for (Monitorable<?> monitorable : monitorables) {
            	monitorable.attachMonitor(monitor);
                pcpWriter.addMetric(MetricName.parse(monitorable.getName()), monitorable
                        .get());
            }
            pcpWriter.start();

            updateThread.start();

            LOG.info("PCP monitoring bridge started for server [" + serverName
                    + "] - logging to directory [" + dataFileDir + "]");
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialise PCP monitoring bridge", e);
        }
    }

    private File getHeaderFile() {
        return new File(dataFileDir, serverName + ".pcp.header");
    }

    private File getDataFile() {
        return new File(dataFileDir, serverName + ".pcp.data");
    }

    /**
     * The Updater is responsible for taking any Monitorables that are pending in the update queue
     * and saving their current value to the PCP shared data file.
     */
    private class Updater implements Runnable {

        public void run() {
            try {
                Collection<Monitorable<?>> monitorablesToUpdate = new ArrayList<Monitorable<?>>();
                while (pcpWriter != null) {
                    try {
                    	// TODO should make a copy of the writer
                        monitorablesToUpdate.add(monitorablesPendingUpdate.take());
                        monitorablesPendingUpdate.drainTo(monitorablesToUpdate);
                        for (Monitorable<?> monitorable : monitorablesToUpdate) {
                            pcpWriter.updateMetric(MetricName
                                    .parse(monitorable.getName()), monitorable.get());
                        }
                        if (monitorablesPendingUpdate.size() >= UPDATE_QUEUE_SIZE) {
                            LOG.warn("Update queue was full - some updates may have been lost.");
                        }
                        monitorablesToUpdate.clear();
                    } catch (InterruptedException e) {
                        LOG.error("Updater was unexpectedly interrupted", e);
                    }
                }
            } catch (RuntimeException e) {
                LOG.fatal("Updater dying because of unexpected exception", e);
                throw e;
            } catch (Error e) {
                LOG.fatal("Updater dying because of unexpected exception", e);
                throw e;
            }
        }
    }

    /**
     * Responsible for adding any Monitorables that change to the queue of Monitorables that are
     * pending update. This class will never block, if the update queue is ever full then the we
     * just do nothing.
     */
    private class PcpMonitorBridgeMonitor implements Monitor {

        public void valueChanged(Monitorable<?> monitorable) {
            /*
             * If the master-arm switch to output values to a file is off, then abandon quickly. The
             * only reason it would be turned off is because we have suspected it is causing
             * performance grief. Highly unlikely, but just in case.
             */
            if (!isOutputValuesToPCPFile()) {
                return;
            }

            if (!monitorablesPendingUpdate.offer(monitorable)) {
                // The queue must be full... This will get detected by the Updater and logged we
                // should do nothing here as we don't want to block.
            }
        }
    }

    @ManagedAttribute(description = "If set, value changes are written to an external file monitored by the Aconex PCP Agent.")
    public boolean isOutputValuesToPCPFile() {
        return outputValuesToPCPFile;
    }

    @ManagedAttribute
    public void setOutputValuesToPCPFile(boolean outputValuesToPCPFile) {
        this.outputValuesToPCPFile = outputValuesToPCPFile;
    }

    /**
     * When set to true, both the header and data files created by this bridge are marked for deletion 
     * via JVM standard deletion policy on normal termination.  The default value is false, and highly encouraged
     * to leave this value off.  Only when circumstances require that the data file name pattern changes on each 
     * JVM launch (say, when using the Process ID in the filename) should this properties use be considered.
     * @param deleteFilesOnExit
     */
    public void setDeleteFilesOnExit(boolean deleteFilesOnExit) {
        this.deleteFilesOnExit = deleteFilesOnExit;
    }

    public final boolean isDeleteFilesOnExit() {
        return deleteFilesOnExit;
    }
}
