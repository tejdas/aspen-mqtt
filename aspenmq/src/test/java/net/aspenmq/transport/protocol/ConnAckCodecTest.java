package net.aspenmq.transport.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import junit.framework.TestCase;
import net.aspenmq.transport.frame.SFrameHeader;
import net.aspenmq.transport.frame.SMessageType;

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
        connectAckCodecTest(ConnectAck.ERROR_BAD_CREDENTIALS());
        connectAckCodecTest(ConnectAck.ERROR_PROTOCOL_VERSION());
        connectAckCodecTest(ConnectAck.CONNECTION_ACCEPTED());
    }

    private void connectAckCodecTest(int returnCode) throws IOException {
        ConnectAck connectHeaderIn = new ConnectAck(returnCode);
        ByteBuf buf = connectHeaderIn.encode();
        SFrameHeader frameHeader = SFrameHeader.parseHeader(buf);
        assertTrue(frameHeader != null);
        assertEquals(frameHeader.messageType(), SMessageType.CONNACK());
        assertEquals(frameHeader.messageLength(), buf.readableBytes());

        ConnectAck connectHeaderOut = ConnectAck.decode(buf);
        assertConnectAck(connectHeaderIn, connectHeaderOut);
    }

    private static void assertConnectAck(ConnectAck in, ConnectAck out) {
        assertEquals(in.returnCode(), out.returnCode());
    }
}
