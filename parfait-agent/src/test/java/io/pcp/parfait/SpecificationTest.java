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

import java.lang.reflect.Field;
import javax.measure.Unit;
import javax.measure.quantity.Time;
import org.junit.Test;
import systems.uom.quantity.Information;
import tec.uom.se.unit.MetricPrefix;
import tec.uom.se.unit.Units;

public class SpecificationTest {

    public void testSetterOfStringField(Specification pojo, String name) throws NoSuchFieldException, IllegalAccessException {
        final String value = "test_" + name;
        final Field field = pojo.getClass().getDeclaredField(name);
        field.setAccessible(true);
        assertEquals(name + " not set", field.get(pojo), value);
    }

    @Test
    public void testSettingNameField() throws NoSuchFieldException, IllegalAccessException {
        final Specification pojo = new Specification();
        pojo.name = "test_name";
        testSetterOfStringField(pojo, "name");
    }

    @Test
    public void testSettingTextField() throws NoSuchFieldException, IllegalAccessException {
        final Specification pojo = new Specification();
        pojo.description = "test_description";
        testSetterOfStringField(pojo, "description");
    }

    @Test
    public void testSettingMBeanNameField() throws NoSuchFieldException, IllegalAccessException {
        final Specification pojo = new Specification();
        pojo.mBeanName = "test_mBeanName";
        testSetterOfStringField(pojo, "mBeanName");
    }

    @Test
    public void testSettingMBeanAttributeNameField() throws NoSuchFieldException, IllegalAccessException {
        final Specification pojo = new Specification();
        pojo.mBeanAttributeName = "test_mBeanAttributeName";
        testSetterOfStringField(pojo, "mBeanAttributeName");
    }

    @Test
    public void testSettingMBeanCompositeDataItemField() throws NoSuchFieldException, IllegalAccessException {
        final Specification pojo = new Specification();
        pojo.mBeanCompositeDataItem = "test_mBeanCompositeDataItem";
        testSetterOfStringField(pojo, "mBeanCompositeDataItem");
    }

    @Test
    public void testParsingUnitsTime() {
        final Specification pojo = new Specification();
        final Unit<Time> MILLISECONDS = MetricPrefix.MILLI(Units.SECOND);
        Unit<?> unit = pojo.parseUnits("milliseconds");
        assertEquals("Parsing milliseconds", unit, MILLISECONDS);
    }

    @Test
    public void testParsingUnitsInformation() {
        final Specification pojo = new Specification();
        final Unit<Information> BYTE = systems.uom.unicode.CLDR.BYTE;
        Unit<?> unit = pojo.parseUnits("bytes");
        assertEquals("Parsing bytes", unit, BYTE);
    }

    @Test
    public void testSettingUnitsField() throws NoSuchFieldException, IllegalAccessException {
        final Unit<Time> MILLISECONDS = MetricPrefix.MILLI(Units.SECOND);
        final Specification pojo = new Specification();
        pojo.unit = MILLISECONDS;
        final Field unitField = pojo.getClass().getDeclaredField("unit");
        unitField.setAccessible(true);
        assertEquals("Unit not set", unitField.get(pojo), MILLISECONDS);
    }

    @Test
    public void testSettingSemanticsField() throws NoSuchFieldException, IllegalAccessException {
        final Specification pojo = new Specification();
        pojo.semantics = ValueSemantics.CONSTANT;
        final Field semanticsField = pojo.getClass().getDeclaredField("semantics");
        semanticsField.setAccessible(true);
        assertEquals("Semantics not set", semanticsField.get(pojo), ValueSemantics.CONSTANT);
    }

    public void testGetterOfStringField(String name) throws NoSuchFieldException, IllegalAccessException {
        final String value = "test_" + name;
        final Specification pojo = new Specification();
        final Field field = pojo.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(pojo, value);
        assertEquals(name + " not set", field.get(pojo), value);
    }

    @Test
    public void testGetterGetsNameValue() throws NoSuchFieldException, IllegalAccessException {
        testGetterOfStringField("name");
    }

    @Test
    public void testGetterGetsDescriptionValue() throws NoSuchFieldException, IllegalAccessException {
        testGetterOfStringField("description");
    }

    @Test
    public void testGetterGetsMBeanNameValue() throws NoSuchFieldException, IllegalAccessException {
        testGetterOfStringField("mBeanName");
    }

    @Test
    public void testGetterGetsMBeanAttributeNameValue() throws NoSuchFieldException, IllegalAccessException {
        testGetterOfStringField("mBeanAttributeName");
    }

    @Test
    public void testGetterGetsMBeanCompositeDataItemValue() throws NoSuchFieldException, IllegalAccessException {
        testGetterOfStringField("mBeanCompositeDataItem");
    }
}
