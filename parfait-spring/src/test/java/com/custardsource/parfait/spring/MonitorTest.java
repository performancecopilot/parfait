package com.custardsource.parfait.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;


public class MonitorTest extends TestCase {
	public void testThing() {
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"test.xml"});
		DelayingBean test = ((DelayingBean) context.getBean("test"));
		DelayingBean other = ((DelayingBean) context.getBean("other"));
		test.doThing();
		other.doThing();
	}
}
