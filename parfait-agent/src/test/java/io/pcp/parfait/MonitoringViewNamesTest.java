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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MonitoringViewNamesTest {
    @Test
    public void checkJvmProcessString() {
        // expecting to be of form "pid@host" (need new JVM-specific code if not)
        String name = MonitoringViewProperties.getRuntimeName();
        assertNotNull(name);
        assertNotSame(name, "");

        String[] pidAndHost = name.split("@", 2);
        assertNotNull(pidAndHost);
        assertEquals(pidAndHost.length, 2);

        int pid = Integer.parseInt(pidAndHost[0]);
        assertTrue(pid > 0);
    }

    @Test
    public void checkCommandBasenameExtraction() {
        assertEquals(MonitoringViewProperties.getCommandBasename(null), null);
        assertEquals(MonitoringViewProperties.getCommandBasename(""), null);
        assertEquals(MonitoringViewProperties.getCommandBasename("123"), null);
        assertEquals(MonitoringViewProperties.getCommandBasename("abc"), "abc");
        assertEquals(MonitoringViewProperties.getCommandBasename("abc1"), "abc1");
        assertEquals(MonitoringViewProperties.getCommandBasename("abc 123"), "abc");
        assertEquals(MonitoringViewProperties.getCommandBasename("#!?@"), null);
    }

    @Test
    public void checkProcessIdentifierExtraction() {
        assertEquals(MonitoringViewProperties.getFallbackName(""), MonitoringViewProperties.PARFAIT);
        assertEquals(MonitoringViewProperties.getFallbackName("12345"), MonitoringViewProperties.PARFAIT);
        assertEquals(MonitoringViewProperties.getFallbackName("@localhost"), MonitoringViewProperties.PARFAIT);
        assertEquals(MonitoringViewProperties.getFallbackName("12345@localhost"), MonitoringViewProperties.PARFAIT + "12345");
    }

    @Test
    public void checkDefaultNameResults() {
        assertEquals(MonitoringViewProperties.getDefaultName(null, null, null), MonitoringViewProperties.PARFAIT);
        assertEquals(MonitoringViewProperties.getDefaultName("foo", null, null), "foo");
        assertEquals(MonitoringViewProperties.getDefaultName("foo", "bar", null), "foo");
        assertEquals(MonitoringViewProperties.getDefaultName(null, "bar", null), "bar");
        assertEquals(MonitoringViewProperties.getDefaultName(null, null, "baz"), MonitoringViewProperties.PARFAIT);
    }
}
