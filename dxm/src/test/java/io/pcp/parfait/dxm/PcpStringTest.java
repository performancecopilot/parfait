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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static io.pcp.parfait.dxm.Matchers.ReflectiveMatcher.reflectivelyEqualing;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyCollectionOf;

public class PcpStringTest {

    private static final Charset CHARSET = Charset.forName("US-ASCII");
    private static final String MY_STRING = "mystring";
    private static final int BUFFER_POSITION = 5;

    @Test
    public void shouldWriteToTheByteBufferWithANullTerminatingString() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(256);

        PcpString pcpString = new PcpString(MY_STRING);

        pcpString.writeToMmv(byteBuffer);

        byte[] expectedBytes = MY_STRING.getBytes(CHARSET);
        byteBuffer.position(0);
        byte[] writtenBytes = new byte[expectedBytes.length];
        byteBuffer.get(writtenBytes);
        assertThat(writtenBytes, CoreMatchers.equalTo(expectedBytes));
        assertThat(byteBuffer.get(), CoreMatchers.equalTo((byte) 0));
    }

    @Test
    public void shouldSetTheByteBufferToTheCorrectOffsetBeforeWriting() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(256);

        PcpString pcpString = new PcpString(MY_STRING);

        pcpString.setOffset(BUFFER_POSITION);
        pcpString.writeToMmv(byteBuffer);

        byte[] beforeOffset = new byte[BUFFER_POSITION];
        byteBuffer.position(0);
        byteBuffer.get(beforeOffset);
        assertThat(beforeOffset, CoreMatchers.equalTo(new byte[BUFFER_POSITION]));

        byte[] expectedBytes = MY_STRING.getBytes(CHARSET);
        byte[] writtenBytes = new byte[expectedBytes.length];
        byteBuffer.get(writtenBytes);
        assertThat(writtenBytes, CoreMatchers.equalTo(expectedBytes));
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