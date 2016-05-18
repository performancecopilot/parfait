package io.pcp.parfait.dxm;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class StringParsingIdentifierSourceSetTest {
    static final int FIXED_FALLBACK = 0xFEEDaBEE;
    private static final IdentifierSource FALLBACK_SOURCE = new ConstantIdentifierSource(
            FIXED_FALLBACK);
    static final IdentifierSourceSet FALLBACK_SOURCES = new IdentifierSourceSet() {
        @Override
        public IdentifierSource metricSource() {
            return FALLBACK_SOURCE;
        }

        @Override
        public IdentifierSource instanceSource(String domain) {
            return FALLBACK_SOURCE;
        }

        @Override
        public IdentifierSource instanceDomainSource() {
            return FALLBACK_SOURCE;
        }
    };

    private static final String METRIC_NAME = "aMetric";
    private static final int METRIC_ID = 123;

    private static final String DOMAIN_NAME = "aDomain";
    private static final int DOMAIN_ID = 456;

    private static final String WHITESPACE = " ";

    private static final String INSTANCE_DOMAIN_LINE = DOMAIN_NAME + WHITESPACE + DOMAIN_ID;
    private static final String INSTANCE_NAME = "someInstance";
    private static final int INSTANCE_ID = 789;

    @Test
    public void metricSourceShouldReturnSpecifiedValue() {
        assertMetricResultMatches(ImmutableList.of(METRIC_NAME + WHITESPACE + METRIC_ID));
    }

    @Test
    public void metricParsingShouldSkipCommentLines() {
        assertMetricResultMatches(ImmutableList.of("# comment", METRIC_NAME + WHITESPACE
                + METRIC_ID));
    }

    @Test
    public void metricParsingShouldSkipCommentLinesWithLeadingSpaces() {
        assertMetricResultMatches(ImmutableList.of(" # comment", METRIC_NAME + WHITESPACE
                + METRIC_ID));
    }

    @Test
    public void metricParsingShouldIgnoreLeadingSpaces() {
        assertMetricResultMatches(ImmutableList.of(WHITESPACE + METRIC_NAME + WHITESPACE
                + METRIC_ID));
    }

    @Test
    public void metricParsingShouldIgnoreTrailingSpaces() {
        assertMetricResultMatches(ImmutableList.of(METRIC_NAME + WHITESPACE + METRIC_ID
                + WHITESPACE));
    }

    @Test
    public void metricParsingShouldCollapseIntermediateSpaces() {
        assertMetricResultMatches(ImmutableList.of(METRIC_NAME + WHITESPACE + WHITESPACE
                + METRIC_ID));
    }

    @Test(expected = IllegalArgumentException.class)
    public void metricParsingShouldFailIfOnlyOneColumn() {
        assertMetricResultMatches(ImmutableList.of(METRIC_NAME));
    }

    @Test(expected = IllegalArgumentException.class)
    public void metricParsingShouldFailIfValueNotAnInteger() {
        assertMetricResultMatches(ImmutableList.of(METRIC_NAME + WHITESPACE + "blah"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void metricParsingShouldFailWhenDuplicateIds() {
        assertMetricResultMatches(ImmutableList.of(METRIC_NAME + WHITESPACE + METRIC_ID,
                "duplicate" + WHITESPACE + METRIC_ID));
    }

    @Test
    public void domainSourceShouldReturnSpecifiedValue() {
        assertDomainResultMatches(ImmutableList.of(DOMAIN_NAME + WHITESPACE + DOMAIN_ID));
    }

    @Test
    public void domainParsingShouldIgnoreTrailingSpaces() {
        assertDomainResultMatches(ImmutableList.of(DOMAIN_NAME + WHITESPACE + DOMAIN_ID
                + WHITESPACE));
    }

    @Test
    public void domainParsingShouldCollapseIntermediateSpaces() {
        assertDomainResultMatches(ImmutableList.of(DOMAIN_NAME + WHITESPACE + WHITESPACE
                + DOMAIN_ID));
    }

    @Test(expected = IllegalArgumentException.class)
    public void domainParsingShouldFailIfOnlyOneColumn() {
        assertDomainResultMatches(ImmutableList.of(DOMAIN_NAME + WHITESPACE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void domainParsingShouldFailIfValueNotAnInteger() {
        assertDomainResultMatches(ImmutableList.of(DOMAIN_NAME + WHITESPACE + "bloop"));
    }

    @Test
    public void domainSourceShouldSkipCommentLines() {
        assertDomainResultMatches(ImmutableList.of("# Comment", DOMAIN_NAME + WHITESPACE
                + DOMAIN_ID));
    }

    @Test
    public void domainSourceShouldSkipCommentLinesWithLeadingSpaces() {
        assertDomainResultMatches(ImmutableList.of("\t# Comment", DOMAIN_NAME + WHITESPACE
                + DOMAIN_ID));
    }

    @Test(expected = IllegalArgumentException.class)
    public void instanceParsingShouldFailIfNoDomainSpecifiedYet() {
        assertDomainResultMatches(ImmutableList.of(WHITESPACE + INSTANCE_NAME + WHITESPACE
                + INSTANCE_ID));
    }

    @Test
    public void instanceSourceShouldReturnSpecifiedValue() {
        assertInstanceResultMatches(ImmutableList.of(INSTANCE_DOMAIN_LINE, WHITESPACE
                + INSTANCE_NAME + WHITESPACE + INSTANCE_ID));
    }

    @Test
    public void instanceSourceShouldIgnoreTrailingSpaces() {
        assertInstanceResultMatches(ImmutableList.of(INSTANCE_DOMAIN_LINE, WHITESPACE
                + INSTANCE_NAME + WHITESPACE + INSTANCE_ID + WHITESPACE));
    }

    @Test
    public void instanceSourceShouldCollapseIntermediateSpaces() {
        assertInstanceResultMatches(ImmutableList.of(INSTANCE_DOMAIN_LINE, WHITESPACE
                + INSTANCE_NAME + WHITESPACE + WHITESPACE + INSTANCE_ID));
    }

    @Test
    public void instanceSourceShouldSkipCommentLines() {
        assertInstanceResultMatches(ImmutableList.of(INSTANCE_DOMAIN_LINE, " # Comment ",
                WHITESPACE + INSTANCE_NAME + WHITESPACE + WHITESPACE + INSTANCE_ID));
    }

    @Test(expected = IllegalArgumentException.class)
    public void instanceSourceShouldFailIfOnlyOneColumn() {
        assertInstanceResultMatches(ImmutableList.of(INSTANCE_DOMAIN_LINE, WHITESPACE
                + INSTANCE_NAME));
    }

    @Test(expected = IllegalArgumentException.class)
    public void instanceSourceShouldFailIfValueNotAnInteger() {
        assertInstanceResultMatches(ImmutableList.of(INSTANCE_DOMAIN_LINE, WHITESPACE
                + INSTANCE_NAME + WHITESPACE + "blah"));
    }

    private void assertMetricResultMatches(List<String> input) {
        IdentifierSourceSet sources = new StringParsingIdentifierSourceSet(Collections
                .<String> emptyList(), input, FALLBACK_SOURCES);
        assertEquals(METRIC_ID, sources.metricSource().calculateId(METRIC_NAME,
                Collections.<Integer> emptySet()));
    }

    private void assertDomainResultMatches(List<String> input) {
        IdentifierSourceSet sources = new StringParsingIdentifierSourceSet(input, Collections
                .<String> emptyList(), FALLBACK_SOURCES);
        assertEquals(DOMAIN_ID, sources.instanceDomainSource().calculateId(DOMAIN_NAME,
                Collections.<Integer> emptySet()));
    }

    private void assertInstanceResultMatches(List<String> input) {
        IdentifierSourceSet sources = new StringParsingIdentifierSourceSet(input, Collections
                .<String> emptyList(), FALLBACK_SOURCES);
        assertEquals(INSTANCE_ID, sources.instanceSource(DOMAIN_NAME).calculateId(INSTANCE_NAME,
                Collections.<Integer> emptySet()));
    }
}
