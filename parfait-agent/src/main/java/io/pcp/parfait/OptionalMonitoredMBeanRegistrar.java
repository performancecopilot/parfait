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
