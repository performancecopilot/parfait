package io.pcp.parfait.jmx;

import static tec.uom.se.AbstractUnit.ONE;

import java.io.IOException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.measure.Unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.jmx.support.JmxUtils;

import io.pcp.parfait.Monitorable;
import io.pcp.parfait.MonitorableRegistry;
import io.pcp.parfait.MonitoredConstant;
import io.pcp.parfait.MonitoredValue;
import io.pcp.parfait.PollingMonitoredValue;
import io.pcp.parfait.ValueSemantics;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;

/**
 * Factory bean that generates a monitor which tracks the value of the provided MBean attribute.
 * <p>
 * Support is provided for monitoring simple attributes and also the data items of attributes that
 * are of type {@link CompositeData}.
 */
public class MonitoredMBeanAttributeFactory<T> implements FactoryBean<Monitorable<T>> {

    public static final Logger LOG = LoggerFactory.getLogger(MonitoredMBeanAttributeFactory.class.getName());

    /**
     * May be passed as the update interval to indicate that no updates are required. This is useful
     * for tracking attributes which will never change in value.
     */
    public static final int DO_NOT_UPDATE_VALUE = -1;

    private final String name;

    private final String description;

    private final ObjectName mBeanName;

    private final String attributeName;

    private final String compositeDataItem;

    private MBeanServerConnection server;

    private int updateInterval = DO_NOT_UPDATE_VALUE;

    private Unit<?> unit = ONE;
    
    private ValueSemantics semantics = ValueSemantics.FREE_RUNNING;

    private MonitorableRegistry monitorableRegistry = MonitorableRegistry.DEFAULT_REGISTRY;

    public MonitoredMBeanAttributeFactory(String name, String description,
            String mBeanName, String attributeName) {
        this(name, description, mBeanName, attributeName, null,
                JmxUtils.locateMBeanServer());
    }

    public MonitoredMBeanAttributeFactory(String name, String description,
            String mBeanName, String attributeName, String compositeDataItem) {
        this(name, description, mBeanName, attributeName, compositeDataItem,
                JmxUtils.locateMBeanServer());
    }
    
    public MonitoredMBeanAttributeFactory(String name, String description,
            String mBeanName, String attributeName, String compositeDataItem,
            MBeanServerConnection server) {
        this.name = name;
        this.server = server;
        this.description = description;
        this.attributeName = attributeName;
        this.compositeDataItem = compositeDataItem;

        String beanName = registerBeanName(server, mBeanName);
        try {
            this.mBeanName = new ObjectName(beanName);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception mBeanName name [" + mBeanName
                    + "]", e);
        }
    }

    public void setMonitorableRegistry(MonitorableRegistry registry) {
        this.monitorableRegistry = Preconditions.checkNotNull(registry);
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public void setValueSemantics(ValueSemantics semantics) {
        this.semantics = Preconditions.checkNotNull(semantics);
    }

    public void setUnit(Unit<?> unit) {
        this.unit = unit;
    }
    
    public Monitorable<T> getObject() throws InstanceNotFoundException, IntrospectionException,
            ReflectionException, AttributeNotFoundException, MBeanException, IOException {

        MBeanInfo beanInfo = server.getMBeanInfo(mBeanName);

        MBeanAttributeInfo monitoredAttribute = null;
        MBeanAttributeInfo[] attributes = beanInfo.getAttributes();
        for (MBeanAttributeInfo attribute : attributes) {
            if (attribute.getName().equals(attributeName)) {
                monitoredAttribute = attribute;
                break;
            }
        }
        if (monitoredAttribute == null) {
            throw new UnsupportedOperationException("MBean [" + mBeanName
                    + "] does not have an attribute named [" + attributeName + "]");
        }

        if (!Strings.isNullOrEmpty(compositeDataItem)) {
			Preconditions
					.checkState(
							CompositeData.class.getName().equals(
									monitoredAttribute.getType()),
							"MBean [%s] attribute [%s] must be of type CompositeData if compositeDataItem is provided",
							mBeanName, attributeName);
            CompositeData data = (CompositeData) server.getAttribute(mBeanName, attributeName);
			Preconditions
					.checkState(
							data.getCompositeType().getType(compositeDataItem) != null,
							"MBean [%s] attribute [%s] does not have a data item called [%s]",
							mBeanName, attributeName, compositeDataItem);
        }

        if (isConstant()) {
        	return new MonitoredConstant<T>(name, description, getAttributeValue());
        } else {
        	return new PollingMonitoredValue<T>(name, description, monitorableRegistry, updateInterval, new Supplier<T>() {
        		
        		public T get() {
        			return getAttributeValue();
        		}
        		
        	}, semantics, unit);
        }
    }

    public Class<?> getObjectType() {
        return isConstant() ? MonitoredConstant.class : MonitoredValue.class;
    }

    private boolean isConstant() {
        return updateInterval == DO_NOT_UPDATE_VALUE;
    }

    public boolean isSingleton() {
        return true;
    }

    @SuppressWarnings("unchecked")
    protected T getAttributeValue() {
        try {
            if (!Strings.isNullOrEmpty(compositeDataItem)) {
                CompositeData data = (CompositeData) server.getAttribute(mBeanName, attributeName);
                return (T) data.get(compositeDataItem);
            } else {
                return (T) server.getAttribute(mBeanName, attributeName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String registerBeanName(MBeanServerConnection server, String beanName) {
        int pos = beanName.lastIndexOf(",name=");
        if (pos > 0) {
            String baseString = beanName.substring(0, pos);
            Iterable<String> namesString = Splitter.on('|').split(beanName.substring(beanName.lastIndexOf("=")+1));
            for (String name : namesString) {
                try {
                    String returnValue = baseString+",name="+name;
                    ObjectName objectName = new ObjectName(returnValue);
                    if (server.isRegistered(objectName)) {
                        LOG.trace(this.name + " registered as " + returnValue);
                        return returnValue;
                    }
                }
                catch (MalformedObjectNameException mone) {
                    throw new RuntimeException("Unexpected exception mBeanName name [" + beanName
                            + "]", mone);
                } catch (IOException ioe) {
                    throw new RuntimeException("Unexpected IO error, mBeanName name [" + beanName
                            + "]", ioe);
				}
            }
        }
        return beanName;
    }
    
}
