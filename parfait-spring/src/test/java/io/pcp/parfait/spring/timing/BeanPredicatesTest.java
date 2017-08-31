/*
 * Copyright 2009-2017 Aconex
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
