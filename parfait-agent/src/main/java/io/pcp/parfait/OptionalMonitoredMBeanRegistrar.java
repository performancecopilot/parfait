package io.pcp.parfait;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;

import io.pcp.parfait.jmx.MonitoredMBeanAttributeFactory;

public class OptionalMonitoredMBeanRegistrar<T> extends MonitoredMBeanAttributeFactory<T> {

    public OptionalMonitoredMBeanRegistrar(String name, String description,
            String mBeanName, String attributeName, String compositeDataItem,
            MBeanServerConnection server) {
        super(name, description, mBeanName, attributeName, compositeDataItem, server);
    }

    public Monitorable<T> getObject() throws
            InstanceNotFoundException, IntrospectionException,
            ReflectionException, AttributeNotFoundException, MBeanException {
        try {
            return super.getObject();
        } catch (Exception e) {
            return null;
        }
    }
}
