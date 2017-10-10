/*
 * Copyright 2009-2017 Red Hat Inc.
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

package io.pcp.parfait;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;

import io.pcp.parfait.DynamicMonitoringView;
import io.pcp.parfait.dxm.IdentifierSourceSet;
import io.pcp.parfait.dxm.PcpMmvWriter;
import io.pcp.parfait.Monitorable;
import io.pcp.parfait.MonitorableRegistry;
import io.pcp.parfait.MonitoredConstant;
import io.pcp.parfait.MonitoredValue;
import io.pcp.parfait.PollingMonitoredValue;
import io.pcp.parfait.pcp.PcpMonitorBridge;
import io.pcp.parfait.ValueSemantics;

import java.io.IOException;
import java.util.EnumSet;

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

import org.apache.log4j.Logger;


class AgentMonitoringView {
    private static final Logger logger = Logger.getLogger(ParfaitAgent.class);

    private MonitorableRegistry registry = MonitorableRegistry.DEFAULT_REGISTRY;

    private final MBeanServerConnection server;
    private final Long interval;
    private final String name;


    public AgentMonitoringView(MBeanServerConnection server) {
        this.server = server;
        this.name = MonitoringViewProperties.getName();
        this.interval = MonitoringViewProperties.getInterval();
    }

    public void start() {
        PcpMmvWriter writer;
        writer = new PcpMmvWriter(name, IdentifierSourceSet.DEFAULT_SET);
        writer.setClusterIdentifier(MonitoringViewProperties.getCluster());
        writer.setFlags(EnumSet.of(PcpMmvWriter.MmvFlag.MMV_FLAG_PROCESS));

        DynamicMonitoringView view;
        view = new DynamicMonitoringView(registry, 
                        new PcpMonitorBridge(writer),
                        MonitoringViewProperties.getStartup());
        view.start();
    }

    public <T> Monitorable<T> register(Specification specification) throws InstanceNotFoundException, IntrospectionException, AttributeNotFoundException, UnsupportedOperationException, ReflectionException, MBeanException, IOException {
        String beanName = registerBeanName(specification.getMBeanName());
        ObjectName mBeanName;
        try {
            mBeanName = new ObjectName(beanName);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception mbean name [" +
                                        beanName + "]", e);
        }
        return createMonitorable(mBeanName, specification);
    }

    private <T> Monitorable<T> createMonitorable(ObjectName mBeanName, Specification specification) throws InstanceNotFoundException, IntrospectionException, UnsupportedOperationException, ReflectionException, IOException, AttributeNotFoundException, MBeanException {
        String metric = specification.getName();
        String text = specification.getDescription();
        String attributeName = specification.getMBeanAttributeName();
        String compositeDataItem = specification.getMBeanCompositeDataItem();

        boolean optional = specification.getOptional();
        try {
            String typeName = checkAttributeName(mBeanName, attributeName);
            checkCompositeDataItem(mBeanName, typeName, attributeName, compositeDataItem);
        } catch (InstanceNotFoundException | UnsupportedOperationException e) {
            if (optional)
                return null;
            throw new RuntimeException("Metric " + metric + " is not optional but has bad attribute [" + attributeName + "]", e);
        }

        ValueSemantics semantics = specification.getSemantics();
        if (semantics == ValueSemantics.CONSTANT)
            return new MonitoredConstant<T>(metric, text, this.registry,
                            getAttributeValue(mBeanName, attributeName, compositeDataItem));
        return new PollingMonitoredValue<T>(metric, text, this.registry,
                        (int)(long)this.interval, new Supplier<T>() {
                            public T get() {
                                return getAttributeValue(mBeanName, attributeName, compositeDataItem);
                            }
                        }, semantics, specification.getUnits());
    }

    @SuppressWarnings("unchecked")
    protected <T> T getAttributeValue(ObjectName mBeanName, String attributeName, String compositeDataItem) {
        try {
            if (!Strings.isNullOrEmpty(compositeDataItem)) {
                logger.trace(this.name + " get: " + compositeDataItem);
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
            Integer subString = beanName.lastIndexOf("=")+1;
            Iterable<String> names;
            names = Splitter.on('|').split(beanName.substring(subString));
            for (String name : names) {
                try {
                    String returnValue = baseString + ",name=" + name;
                    ObjectName myBeanName = new ObjectName(returnValue);
                    if (server.isRegistered(myBeanName)) {
                        logger.trace(this.name + " registered mBean as " + returnValue);
                        return returnValue;
                    }
                }
                catch (MalformedObjectNameException mone) {
                    throw new RuntimeException("Unexpected exception, " +
                                "mBeanName name [" + beanName + "]", mone);
                } catch (IOException ioe) {
                    throw new RuntimeException("Unexpected IO error, " +
                                "mBeanName name [" + beanName + "]", ioe);
                }
            }
        }
        return beanName;
    }

    private String checkAttributeName(ObjectName mBeanName, String attributeName) throws UnsupportedOperationException, InstanceNotFoundException, IntrospectionException, ReflectionException, IOException {
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
            throw new UnsupportedOperationException("MBean [" + mBeanName +
                    "] has no attribute named [" + attributeName + "]");
        }
        return monitoredAttribute.getType();
    }

    private void checkCompositeDataItem(ObjectName mBeanName, String attributeTypeName, String attributeName, String compositeDataItem) throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        if (Strings.isNullOrEmpty(compositeDataItem))
            return;
        Preconditions.checkState(
                CompositeData.class.getName().equals(attributeTypeName),
                "MBean [%s] attribute [%s] must be of type CompositeData" +
                " if compositeDataItem is provided",
                mBeanName, attributeName);
        CompositeData data;
        data = (CompositeData) server.getAttribute(mBeanName, attributeName);
        Preconditions.checkState(
                data.getCompositeType().getType(compositeDataItem) != null,
                "MBean [%s] attribute [%s] has no data item named [%s]",
                mBeanName, attributeName, compositeDataItem);
    }
}
