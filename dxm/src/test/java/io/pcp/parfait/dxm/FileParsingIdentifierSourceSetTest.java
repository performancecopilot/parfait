package io.pcp.parfait.dxm;

import static io.pcp.parfait.dxm.StringParsingIdentifierSourceSetTest.FIXED_FALLBACK;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;

import org.junit.Test;

public class FileParsingIdentifierSourceSetTest {
    private static final String METRIC_NAME = "cheese.price";
    private static final int METRIC_ID = 123;

    private static final String DOMAIN_NAME = "cheese";
    private static final int DOMAIN_ID = 456;

    private static final String WHITESPACE = " ";

    private static final String INSTANCE_NAME = "gouda";
    private static final int INSTANCE_ID = 789;

    private static final String METRIC_FILE = METRIC_NAME + WHITESPACE + METRIC_ID;

    private static final String DOMAIN_LINE = DOMAIN_NAME + WHITESPACE + DOMAIN_ID;
    private static final String INSTANCE_LINE = WHITESPACE + INSTANCE_NAME + WHITESPACE
            + INSTANCE_ID;
    private static final String INSTANCE_FILE = DOMAIN_LINE + "\n" + INSTANCE_LINE;

    private static final Set<Integer> NO_IDS = Collections.<Integer> emptySet();

    @Test
    public void nullFileShouldUseDefaultMetricSource() {
        assertEquals(FIXED_FALLBACK, buildFileSet(null, null).metricSource().calculateId(
                METRIC_NAME, NO_IDS));
    }

    @Test
    public void nullFileShouldUseDefaultInstanceSource() {
        assertEquals(FIXED_FALLBACK, buildFileSet(null, null).instanceDomainSource().calculateId(
                INSTANCE_NAME, NO_IDS));
    }

    @Test
    public void metricValuesShouldBeParsedFromFile() {
        assertEquals(METRIC_ID, buildFileSet(null, tempFile(METRIC_FILE)).metricSource()
                .calculateId(METRIC_NAME, NO_IDS));
    }

    @Test
    public void instanceDomainValuesShouldBeParsedFromFile() {
        assertEquals(DOMAIN_ID, buildFileSet(tempFile(INSTANCE_FILE), null).instanceDomainSource()
                .calculateId(DOMAIN_NAME, NO_IDS));
    }

    @Test
    public void instanceValuesShouldBeParsedFromFile() {
        assertEquals(INSTANCE_ID, buildFileSet(tempFile(INSTANCE_FILE), null).instanceSource(
                DOMAIN_NAME).calculateId(INSTANCE_NAME, NO_IDS));
    }

    @Test
    public void metricValuesShouldBeParsedFromStream() {
        assertEquals(METRIC_ID, buildStreamSet(null, tempStream(METRIC_FILE)).metricSource()
                .calculateId(METRIC_NAME, NO_IDS));
    }

    @Test
    public void instanceDomainValuesShouldBeParsedFromStream() {
        assertEquals(DOMAIN_ID, buildStreamSet(tempStream(INSTANCE_FILE), null)
                .instanceDomainSource().calculateId(DOMAIN_NAME, NO_IDS));
    }

    @Test
    public void instanceValuesShouldBeParsedFromStream() {
        assertEquals(INSTANCE_ID, buildStreamSet(tempStream(INSTANCE_FILE), null).instanceSource(
                DOMAIN_NAME).calculateId(INSTANCE_NAME, NO_IDS));
    }

    @Test
    public void metricValuesShouldBeParsedFromReader() {
        assertEquals(METRIC_ID, buildReaderSet(null, tempReader(METRIC_FILE)).metricSource()
                .calculateId(METRIC_NAME, NO_IDS));
    }


    @Test
    public void instanceDomainValuesShouldBeParsedFromReader() {
        assertEquals(DOMAIN_ID, buildReaderSet(tempReader(INSTANCE_FILE), null)
                .instanceDomainSource().calculateId(DOMAIN_NAME, NO_IDS));
    }

    @Test
    public void instanceValuesShouldBeParsedFromReader() {
        assertEquals(INSTANCE_ID, buildReaderSet(tempReader(INSTANCE_FILE), null).instanceSource(
                DOMAIN_NAME).calculateId(INSTANCE_NAME, NO_IDS));
    }

    private File tempFile(String contents) {
        try {
            File output = File.createTempFile("parfait-fileparsing", "test");
            output.deleteOnExit();
            Writer outWriter = new FileWriter(output);
            outWriter.write(contents);
            outWriter.close();
            return output;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream tempStream(String contents) {
        return new ByteArrayInputStream(contents.getBytes());
    }

    private Reader tempReader(String contents) {
        return new StringReader(contents);
    }

    private IdentifierSourceSet buildFileSet(File instances, File metrics) {
        return new FileParsingIdentifierSourceSet(instances, metrics,
                StringParsingIdentifierSourceSetTest.FALLBACK_SOURCES);
    }

    private IdentifierSourceSet buildStreamSet(InputStream instances, InputStream metrics) {
        return new FileParsingIdentifierSourceSet(instances, metrics,
                StringParsingIdentifierSourceSetTest.FALLBACK_SOURCES);
    }
    
    private IdentifierSourceSet buildReaderSet(Reader instances, Reader metrics) {
        return new FileParsingIdentifierSourceSet(instances, metrics,
                StringParsingIdentifierSourceSetTest.FALLBACK_SOURCES);
    }

}
