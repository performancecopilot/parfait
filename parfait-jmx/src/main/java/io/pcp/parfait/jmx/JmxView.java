package io.pcp.parfait.jmx;

import io.pcp.parfait.Monitor;
import io.pcp.parfait.Monitorable;
import io.pcp.parfait.MonitoringView;
import com.google.common.base.MoreObjects;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@ManagedResource
public class JmxView implements MonitoringView {
    private String[] jmxMonitoredNames;
    private Object[] jmxMonitoredValues;
    private Map<String, Integer> jmxArrayIndexMap;
    private CompositeType monitoredType;

    private final Monitor monitor = new JmxUpdatingMonitor();
    private volatile boolean started;


    @Override
    public void startMonitoring(Collection<Monitorable<?>> monitorables) {
        setupJmxValues(monitorables);
        for (Monitorable<?> monitorable : monitorables) {
            updateData(monitorable);
            monitorable.attachMonitor(monitor);
        }
        this.started = true;
    }

    @Override
    public void stopMonitoring(Collection<Monitorable<?>> monitorables) {
        for (Monitorable<?> monitorable : monitorables) {
            monitorable.removeMonitor(monitor);
        }
        this.started = false;
    }

    @Override
    public boolean isRunning() {
        return started;
    }

    private void setupJmxValues(Collection<Monitorable<?>> monitorables) {
        if (monitorables.isEmpty()) {
            return;
        }
        try {
            jmxMonitoredNames = new String[monitorables.size()];
            String[] descriptions = new String[monitorables.size()];
            jmxMonitoredValues = new Object[monitorables.size()];
            OpenType<?>[] types = new OpenType<?>[monitorables.size()];
            jmxArrayIndexMap = new HashMap<String, Integer>(monitorables.size());
            int index = 0;

            for (Monitorable<?> monitorable : monitorables) {
                jmxMonitoredNames[index] = monitorable.getName();
                descriptions[index] = MoreObjects.firstNonNull(monitorable.getDescription(),
                        "(unknown)");
                types[index] = getJmxType(monitorable.getType());
                jmxArrayIndexMap.put(monitorable.getName(), index);
                index++;
            }

            monitoredType = new CompositeType("Exposed PCP metrics",
                    "Details of all exposed PCP metrics", jmxMonitoredNames, descriptions, types);
        } catch (OpenDataException e) {
            throw new UnsupportedOperationException("Unable to configure JMX types", e);
        }
    }

    private OpenType<?> getJmxType(Class<?> type) {
        if (type == Boolean.class) {
            return SimpleType.BOOLEAN;
        } else if (type == Integer.class || type == AtomicInteger.class) {
            return SimpleType.INTEGER;
        } else if (type == Long.class || type == AtomicLong.class) {
            return SimpleType.LONG;
        } else if (type == Double.class) {
            return SimpleType.DOUBLE;
        } else if (type == String.class) {
            return SimpleType.STRING;
        } else {
            throw new UnsupportedOperationException(
                    "Don't know how to process Monitorable of type [" + type + "]");
        }
    }

    @ManagedAttribute(description = "All exposed parfait metrics")
    public CompositeData getExposedMetrics() {
        try {
            return new CompositeDataSupport(monitoredType, jmxMonitoredNames, jmxMonitoredValues);
        } catch (OpenDataException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateData(Monitorable<?> monitorable) {
        Class<?> type = monitorable.getType();
        Object jmxValue;

        if (type == Boolean.class || type == Integer.class || type == Long.class
                || type == Double.class || type == String.class) {
            jmxValue = monitorable.get();
        } else if (type == AtomicInteger.class) {
            jmxValue = ((AtomicInteger) monitorable.get()).intValue();
        } else if (type == AtomicLong.class) {
            jmxValue = ((AtomicLong) monitorable.get()).longValue();
        } else {
            throw new UnsupportedOperationException(
                    "Don't know how to process Monitorable of type [" + type + "]");
        }

        jmxMonitoredValues[jmxArrayIndexMap.get(monitorable.getName())] = jmxValue;
    }

    public class JmxUpdatingMonitor implements Monitor {
        public void valueChanged(Monitorable<?> monitorable) {
            updateData(monitorable);
        }
    }
}
