package io.pcp.parfait;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import io.pcp.parfait.DynamicMonitoringView;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:test.xml", "classpath:agent.xml" })
public class ParfaitAgentSpringTest {

    @Autowired
    private DynamicMonitoringView monitoringView;

    @Test
    public void testParfaitAgentSpringBeanWiring() {
        assertEquals("class io.pcp.parfait.DynamicMonitoringView", this.monitoringView.getClass().toString());
    }
}
