package com.custardsource.parfait.pcp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AconexNameMapperTest {
    @Test
    public void controllerNameBecomesInstance() {
        assertMappedName("aconex.controllers[mel/WorkflowWizard].db.time",
                "aconex.controllers.WorkflowWizard.db.time");
    }

    @Test
    public void cacheRegionBecomesInstance() {
        assertMappedName("aconex.cache[mel/_default_].idle_time",
                "aconex.cache._default_.idle_time");
    }

    @Test
    public void overallCacheStatsDoNotBecomeInstance() {
        assertMappedName("aconex[mel].cache.evictions", "aconex.cache.evictions");
    }

    @Test
    public void emailSinkBecomesInstance() {
        assertMappedName("aconex.email.sent[mel/guestLikeMailWithBodyAndAttachments].count",
                "aconex.email.sent.guestLikeMailWithBodyAndAttachments.count");
    }

    @Test
    public void sessionSizeBinBecomesInstance() {
        assertMappedName("aconex.sessions[mel/bin_64KB_256KB].total",
                "aconex.sessions.bin_64KB_256KB.total");
    }

    @Test
    public void overallSessionStatsDoNotBecomeInstance() {
        assertMappedName("aconex[mel].sessions.passivated", "aconex.sessions.passivated");
    }

    @Test
    public void genericInstanceGetsReplaced() {
        assertMappedName("aconex[mel].filestore.duplicate.bytes",
                "aconex.filestore.duplicate.bytes");
    }

    private void assertMappedName(String expectedName, String input) {
        assertEquals(expectedName, map(input));
    }

    private String map(String rawName) {
        return new AconexNameMapper("mel").map(rawName).toString();
    }
}
