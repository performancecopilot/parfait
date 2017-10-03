package io.pcp.parfait;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

import org.junit.Test;

public class SpecificationTest {

    @Test
    public void testSetter_setsProperly() throws NoSuchFieldException, IllegalAccessException {
        final Specification pojo = new Specification();
        pojo.name = "foo";
        pojo.description = "foo";
        pojo.mBeanName = "foo";
        pojo.mBeanAttributeName = "foo";
        pojo.mBeanCompositeDataItem = "foo";
        final Field field = pojo.getClass().getDeclaredField("value");
        field.setAccessible(true);
        assertEquals("Fields didn't match", field.get(pojo), "foo");
    }

    @Test
    public void testGetter_getsValue() throws NoSuchFieldException, IllegalAccessException {
        //given
        final Specification pojo = new Specification();
        final Field field = pojo.getClass().getDeclaredField("value");
        field.setAccessible(true);
        field.set(pojo, "otherValues");
        
        final String name = pojo.getName();
        final String description = pojo.getDescription();
        final String mBeanName = pojo.getMBeanName();
        final String mBeanAttributeName = pojo.getMBeanAttributeName();
        final String mBeanCompositeDataItem = pojo.getMBeanCompositeDataItem();

        assertEquals("field wasn't retrieved properly", name, "otherValues");
        assertEquals("field wasn't retrieved properly", description, "otherValues");
        assertEquals("field wasn't retrieved properly", mBeanName, "otherValues");
        assertEquals("field wasn't retrieved properly", mBeanAttributeName, "otherValues");
        assertEquals("field wasn't retrieved properly", mBeanCompositeDataItem, "otherValues");
    }

}