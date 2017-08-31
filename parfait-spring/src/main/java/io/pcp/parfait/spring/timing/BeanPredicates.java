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

import com.google.common.base.Predicate;
import org.springframework.beans.factory.config.BeanDefinition;

public class BeanPredicates {
    public static Predicate<BeanSpecification> hasAttribute(final String meta, final Object value) {
        return new Predicate<BeanSpecification>() {
            @Override
            public boolean apply(BeanSpecification input) {
                BeanDefinition definition = input.getBeanFactory().getBeanDefinition(input.getName());
                Object attribute = definition.getAttribute(meta);
                return value.equals(attribute);
            }
        };
    }
}
