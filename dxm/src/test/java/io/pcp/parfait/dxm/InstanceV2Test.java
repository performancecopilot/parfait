package io.pcp.parfait.dxm;

import org.junit.Test;
import org.mockito.ArgumentMatchers;

import java.nio.ByteBuffer;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class InstanceV2Test {

    public static final String INSTANCE_NAME = "myinst";

    @Test(expected = RuntimeException.class)
    public void writeToMmvShouldRaiseAnErrorAsItIsNotImplementedYet() {
        InstanceV2 instanceV2 = new InstanceV2(mock(InstanceDomain.class), INSTANCE_NAME, 123, mock(PcpString.class));

        instanceV2.writeToMmv(mock(ByteBuffer.class));
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