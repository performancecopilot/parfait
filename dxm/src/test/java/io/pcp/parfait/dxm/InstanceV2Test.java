package io.pcp.parfait.dxm;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.nio.ByteBuffer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class InstanceV2Test {

    private static final String INSTANCE_NAME = "myinst";
    private static final int INSTANCE_DOMAIN_ID = 123;
    private static final int EXPECTED_BYTE_SIZE = 24;
    private static final int INSTANCE_DOMAIN_OFFSET = 40;
    private static final int STRING_OFFSET = 30;
    private static final int MY_OFFSET = 10;

    @Test
    public void writeToMmvShouldWriteTheCorrectContentToTheByteBuffer() {
        InstanceDomain instanceDomain = mock(InstanceDomain.class);
        PcpString pcpString = mock(PcpString.class);
        ByteBuffer byteBuffer = ByteBuffer.allocate(EXPECTED_BYTE_SIZE);

        InstanceV2 instanceV2 = new InstanceV2(instanceDomain, INSTANCE_NAME, INSTANCE_DOMAIN_ID, pcpString);

        when(instanceDomain.getOffset()).thenReturn(INSTANCE_DOMAIN_OFFSET);
        when(pcpString.getOffset()).thenReturn(STRING_OFFSET);

        instanceV2.writeToMmv(byteBuffer);

        byte[] expected = {
            0,  0,  0,  0,  0,  0,  0, 40,
            0,  0,  0,  0,  0,  0,  0, 123,
            0,  0,  0,  0,  0,  0,  0,  30,
        };

        assertArrayEquals(expected, byteBuffer.array());
    }

    @Test
    public void shouldPositionTheByteBufferBeforeWriting() {
        InstanceV2 instanceV2 = new InstanceV2(mock(InstanceDomain.class), INSTANCE_NAME, INSTANCE_DOMAIN_ID, mock(PcpString.class));
        ByteBuffer byteBuffer = mock(ByteBuffer.class);

        instanceV2.setOffset(MY_OFFSET);
        instanceV2.writeToMmv(byteBuffer);

        InOrder inOrder = Mockito.inOrder(byteBuffer);
        inOrder.verify(byteBuffer).position(MY_OFFSET);
        inOrder.verify(byteBuffer).putLong(anyLong());
    }

    @Test
    public void byteSizeShouldRaiseAnErrorAsItIsNotImplementedYet() {
        InstanceV2 instanceV2 = new InstanceV2(mock(InstanceDomain.class), INSTANCE_NAME, INSTANCE_DOMAIN_ID, mock(PcpString.class));

        assertThat(instanceV2.byteSize(), is(EXPECTED_BYTE_SIZE));
    }

    @Test
    public void instanceStoreShouldCreateANewInstance() {
        IdentifierSourceSet identifierSourceSet = mock(IdentifierSourceSet.class);
        IdentifierSource identifierSource = mock(IdentifierSource.class);
        InstanceDomain instanceDomain = mock(InstanceDomain.class);
        PcpMmvWriter pcpMmvWriter = mock(PcpMmvWriter.class);
        PcpString pcpString = mock(PcpString.class);

        when(identifierSourceSet.instanceSource(INSTANCE_NAME)).thenReturn(identifierSource);
        when(identifierSource.calculateId(eq(INSTANCE_NAME), ArgumentMatchers.<Integer>anySet())).thenReturn(123);
        when(pcpMmvWriter.createPcpString(INSTANCE_NAME)).thenReturn(pcpString);

        InstanceV2.InstanceStoreV2 instanceStore = new InstanceV2.InstanceStoreV2(identifierSourceSet, INSTANCE_NAME,
                instanceDomain, pcpMmvWriter);

        Instance actual = instanceStore.byName(INSTANCE_NAME);

        InstanceV2 expected = new InstanceV2(instanceDomain, INSTANCE_NAME, 123, pcpString);

        assertReflectionEquals(expected, actual);
    }

}