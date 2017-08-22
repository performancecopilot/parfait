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

package io.pcp.parfait.dxm;

import io.pcp.parfait.dxm.PcpString.PcpStringStore;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static io.pcp.parfait.dxm.Matchers.ReflectiveMatcher.reflectivelyEqualing;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.Mockito.mock;

public class PcpStringTest {

    private static final Charset CHARSET = Charset.forName("US-ASCII");
    private static final String MY_STRING = "mystring";
    private static final int BUFFER_POSITION = 5;

    @Test
    public void shouldWriteToTheByteBufferWithANullTerminatingString() {
        ByteBuffer byteBuffer = mock(ByteBuffer.class);

        PcpString pcpString = new PcpString(MY_STRING);

        pcpString.writeToMmv(byteBuffer);

        InOrder inOrder = Mockito.inOrder(byteBuffer);
        inOrder.verify(byteBuffer).put(MY_STRING.getBytes(CHARSET));
        inOrder.verify(byteBuffer).put((byte) 0);
    }

    @Test
    public void shouldSetTheByteBufferToTheCorrectOffsetBeforeWriting() {
        ByteBuffer byteBuffer = mock(ByteBuffer.class);

        PcpString pcpString = new PcpString(MY_STRING);

        pcpString.setOffset(BUFFER_POSITION);
        pcpString.writeToMmv(byteBuffer);


        InOrder inOrder = Mockito.inOrder(byteBuffer);
        inOrder.verify(byteBuffer).position(BUFFER_POSITION);
        inOrder.verify(byteBuffer).put(anyByte());
    }

    @Test
    public void pcpStringStore_createNewPcpString_createsAndStoresAPcpString() {
        PcpStringStore pcpStringStore = new PcpStringStore();

        pcpStringStore.createPcpString(MY_STRING);
        PcpString expected = new PcpString(MY_STRING);

        assertThat(pcpStringStore.getStrings(), CoreMatchers.<PcpString>hasItem(reflectivelyEqualing(expected)));
    }

    @Test
    public void pcpStringStore_createNewPcpString_doesNotCreateANewStringIfNullIsGiven() {
        PcpStringStore pcpStringStore = new PcpStringStore();

        pcpStringStore.createPcpString(null);

        assertThat(pcpStringStore.getStrings(), emptyCollectionOf(PcpString.class));
    }

}