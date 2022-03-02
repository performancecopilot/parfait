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

import static io.pcp.parfait.dxm.Matchers.ReflectiveMatcher.reflectivelyEqualing;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

public class SpecificationAdapterTest {

    private static final String NAME = "myname";
    private static final boolean OPTIONAL = true;
    private static final String DESCRIPTION = "My description";
    private static final String SEMANTICS = "";
    private static final String UNITS = "milliseconds";
    private static final String MBEAN_NAME = "My MBean Name";
    private static final String MBEAN_ATTRIBUTE_NAME = "My MBean attribute name";
    private static final String MBEAN_COMPOSITE_DATA_ITEM = "My MBean composite data item";
    private final SpecificationAdapter specificationAdapter = new SpecificationAdapter();

    @Test
    public void fromJson_shouldConstructASpecificationFromAJsonRepresentation() {
        JsonNode jsonNode = mock(JsonNode.class, RETURNS_DEEP_STUBS);

        when(jsonNode.path("name").asText()).thenReturn(NAME);
        when(jsonNode.path("optional").asBoolean()).thenReturn(OPTIONAL);
        when(jsonNode.path("description").asText()).thenReturn(DESCRIPTION);
        when(jsonNode.path("semantics").asText()).thenReturn(SEMANTICS);
        when(jsonNode.path("units").asText()).thenReturn(UNITS);
        when(jsonNode.path("mBeanName").asText()).thenReturn(MBEAN_NAME);
        when(jsonNode.path("mBeanAttributeName").asText()).thenReturn(MBEAN_ATTRIBUTE_NAME);
        when(jsonNode.path("mBeanCompositeDataItem").asText()).thenReturn(MBEAN_COMPOSITE_DATA_ITEM);

        Specification actual = specificationAdapter.fromJson(jsonNode);
        Specification expected = new Specification(NAME, OPTIONAL, DESCRIPTION, SEMANTICS, UNITS, MBEAN_NAME, MBEAN_ATTRIBUTE_NAME, MBEAN_COMPOSITE_DATA_ITEM);

        assertThat(actual, is(reflectivelyEqualing(expected)));
    }



}