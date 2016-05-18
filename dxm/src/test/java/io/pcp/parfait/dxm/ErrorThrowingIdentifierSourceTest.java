package io.pcp.parfait.dxm;

import java.util.Collections;

import org.junit.Test;

public class ErrorThrowingIdentifierSourceTest {
    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowExceptionWhenIdentifierRequested() {
        new ErrorThrowingIdentifierSource().calculateId("foo", Collections.<Integer> emptySet());

    }
}
