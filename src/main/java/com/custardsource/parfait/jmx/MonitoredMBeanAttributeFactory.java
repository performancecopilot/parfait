package com.custardsource.parfait.jmx;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.jmx.support.JmxUtils;

import com.custardsource.parfait.MonitoredValue;
import com.custardsource.parfait.Poller;
import com.custardsource.parfait.PollingMonitoredValue;
import com.aconex.utilities.Assert;

/**
 * Factory bean that generates a monitor which tracks the value of the provided MBean attribute.
 * <p>
 * Support is provided for monitoring simple attributes and also the data items of attributes that
 * are of type {@link CompositeData}.
 *
 * @author ohutchison
 */
public class MonitoredMBeanAttributeFactory<T> implements FactoryBean {

    public static final Logger LOG = Logger.getLogger(MonitoredMBeanAttributeFactory.class.getName());

    /**
     * May be passed as the update interval to indicate that no updates are required. This is useful
     * for tracking attributes which will never change in value.
     */    
    public static final int DO_NOT_UPDATE_VALUE = -1;

    private final MBeanServer server = JmxUtils.locateMBeanServer();

    private final String name;

    private final String description;

    private final int updateInterval;

    private final ObjectName mBeanName;

    private final String attributeName;

    private final String compositeDataItem;

    public MonitoredMBeanAttributeFactory(String name, String description, String mBeanName,
            String attributeName) {
        this(name, description, DO_NOT_UPDATE_VALUE, mBeanName, attributeName, null);
    }

    public MonitoredMBeanAttributeFactory(String name, String description, String mBeanName,
            String attributeName, String compositeDataItem) {
        this(name, description, DO_NOT_UPDATE_VALUE, mBeanName, attributeName, compositeDataItem);
    }

    public MonitoredMBeanAttributeFactory(String name, String description, int updateInterval,
            String mBeanName, String attributeName) {
        this(name, description, updateInterval, mBeanName, attributeName, null);
    }

    public MonitoredMBeanAttributeFactory(String name, String description, int updateInterval,
            String mBeanName, String attributeName, String compositeDataItem) {
        this.name = name;
        this.description = description;
        this.updateInterval = updateInterval;
        String beanName = registerBeanName(mBeanName);
        try {
            this.mBeanName = new ObjectName(beanName);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception mBeanName name [" + mBeanName
                    + "]", e);
        }
        this.attributeName = attributeName;
        this.compositeDataItem = compositeDataItem;
    }
    
    public MonitoredValue<T> getObject() throws InstanceNotFoundException, IntrospectionException,
            ReflectionException, AttributeNotFoundException, MBeanException {

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

        if (compositeDataItem != null) {
            Assert.equal(CompositeData.class.getName(), monitoredAttribute.getType(), "MBean ["
                    + mBeanName + "] attribute [" + attributeName
                    + "] must be of type CompositeData if compositeDataItem is provided");
            CompositeData data = (CompositeData) server.getAttribute(mBeanName, attributeName);
            Assert.notNull(data.getCompositeType().getType(compositeDataItem), "MBean ["
                    + mBeanName + "] attribute [" + attributeName
                    + "] does not have a data item called [" + compositeDataItem + "]");
        }

        if (updateInterval == DO_NOT_UPDATE_VALUE) {
        	return new MonitoredValue<T>(name, description, getAttributeValue());
        } else {
        	return new PollingMonitoredValue<T>(name, description, updateInterval, new Poller<T>() {
        		
        		public T poll() {
        			return getAttributeValue();
        		}
        		
        	});
        }
    }

    public Class<?> getObjectType() {
        return MonitoredValue.class;
    }

    public boolean isSingleton() {
        return true;
    }

    @SuppressWarnings("unchecked")
	protected T getAttributeValue() {
        try {
            if (compositeDataItem != null) {
                CompositeData data = (CompositeData) server.getAttribute(mBeanName, attributeName);
                return (T) data.get(compositeDataItem);
            } else {
                return (T) server.getAttribute(mBeanName, attributeName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private String registerBeanName(String beanName) {
        int pos = beanName.lastIndexOf(",name=");
        if (pos > 0) {
            String baseString = beanName.substring(0, pos);
            String[] namesString = StringUtils.split(beanName.substring(beanName.lastIndexOf("=")+1), "|");
            for (String name : namesString) {
                try {
                    String returnValue = baseString+",name="+name;
                    ObjectName objectName = new ObjectName(returnValue);
                    if (server.isRegistered(objectName)) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info(this.name+" registered as "+returnValue);
                        }
                        return returnValue;
                    }
                }
                catch (MalformedObjectNameException mone) {
                    throw new RuntimeException("Unexpected exception mBeanName name [" + beanName
                            + "]", mone);
                    
                }
            }
        }
        return beanName;
    }
    
}
