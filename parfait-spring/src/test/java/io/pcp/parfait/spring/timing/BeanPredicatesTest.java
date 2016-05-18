package io.pcp.parfait.spring.timing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BeanPredicatesTest {
    private ConfigurableApplicationContext context;

    @Before
    public void setUp() {
        context = new ClassPathXmlApplicationContext(new String[]{"test.xml"});
    }

    @Test
    public void predicateShouldMatchBeanWithCorrectMetaAttribute() {
        BeanSpecification specification = new BeanSpecification("beanWithMetaFoo", context.getBeanFactory());
        assertTrue(BeanPredicates.hasAttribute("metakey", "foo").apply(specification));
    }

    @Test
    public void predicateShouldNotMatchBeanWithIncorrectMetaAttribute() {
        BeanSpecification specification = new BeanSpecification("beanWithMetaBar", context.getBeanFactory());
        assertFalse(BeanPredicates.hasAttribute("metakey", "foo").apply(specification));
    }

    @Test
    public void predicateShouldNotMatchBeanWithNoMeta() {
        BeanSpecification specification = new BeanSpecification("beanWithNoMeta", context.getBeanFactory());
        assertFalse(BeanPredicates.hasAttribute("metakey", "foo").apply(specification));
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void predicateShouldThrowIfBeanNotPresent() {
        BeanSpecification specification = new BeanSpecification("nonExistentBean", context.getBeanFactory());
        BeanPredicates.hasAttribute("metakey", "foo").apply(specification);
    }
}
