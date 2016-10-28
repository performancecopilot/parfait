package io.pcp.parfait.dxm;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

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

}