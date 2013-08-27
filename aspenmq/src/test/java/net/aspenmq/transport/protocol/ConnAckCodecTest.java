package net.aspenmq.transport.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import junit.framework.TestCase;
import net.aspenmq.transport.frame.FrameHeader;
import net.aspenmq.transport.frame.MessageType;

import org.junit.Test;

public class ConnAckCodecTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testCodec() throws IOException {
        connectAckCodecTest(ConnectAck.ERROR_BAD_CREDENTIALS);
        connectAckCodecTest(ConnectAck.ERROR_PROTOCOL_VERSION);
        connectAckCodecTest(ConnectAck.CONNECTION_ACCEPTED);
    }

    private void connectAckCodecTest(int returnCode) throws IOException {
        ConnectAck connectHeaderIn = new ConnectAck();
        connectHeaderIn.setReturnCode(returnCode);
        ByteBuf buf = connectHeaderIn.encode();
        FrameHeader frameHeader = FrameHeader.parseHeader(buf);
        assertTrue(frameHeader != null);
        assertEquals(frameHeader.getMessageType(), MessageType.CONNACK);
        assertEquals(frameHeader.getMessageLength(), buf.readableBytes());

        ConnectAck connectHeaderOut = ConnectAck.decode(buf);
        assertConnectAck(connectHeaderIn, connectHeaderOut);
    }

    private static void assertConnectAck(ConnectAck in, ConnectAck out) {
        assertEquals(in.getReturnCode(), out.getReturnCode());
    }
}
