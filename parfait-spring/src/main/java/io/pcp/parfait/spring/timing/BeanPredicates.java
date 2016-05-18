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
