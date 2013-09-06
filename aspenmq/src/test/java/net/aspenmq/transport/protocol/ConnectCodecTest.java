package net.aspenmq.transport.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import junit.framework.TestCase;
import net.aspenmq.transport.frame.SFrameHeader;
import net.aspenmq.transport.frame.SMessageType;
import net.aspenmq.transport.frame.SQoS;

import org.junit.Test;

import scala.Enumeration.Value;

public class ConnectCodecTest extends TestCase {
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
        connectCodecTest(45, "testuser", "testpwd", true, SQoS.QOS_ATMOST_ONCE(), "randomTopic", false);
        connectCodecTest(9314, "testuser", "testpwd", true, SQoS.QOS_EXACTLY_ONCE(), null, true);
        connectCodecTest(9314, "testuser", "testpwd", false, SQoS.QOS_ATLEAST_ONCE(), "foobar", true);
        connectCodecTest(5982, null, null, true, SQoS.QOS_ATMOST_ONCE(), "randomTopic", true);
    }

    private void connectCodecTest(int keepAliveDuration,
            String user,
            String pwd,
            boolean willFlag,
            Value willQoS,
            String willTopic,
            boolean willRetain) throws IOException {
        Connect connectHeaderIn = new Connect();
        connectHeaderIn.keepAliveDuration_$eq(keepAliveDuration);
        if (user != null) {
            connectHeaderIn.userName_$eq(user);
        }
        if (pwd != null) {
            connectHeaderIn.password_$eq(pwd);
        }
        connectHeaderIn.willFlag_$eq(true);
        connectHeaderIn.willQoS_$eq(willQoS);
        if (willTopic != null) {
            connectHeaderIn.willTopic_$eq(willTopic);
        }
        connectHeaderIn.willRetain_$eq(willRetain);

        ByteBuf buf = connectHeaderIn.encode();
        SFrameHeader frameHeader = SFrameHeader.parseHeader(buf);
        assertTrue(frameHeader != null);
        assertEquals(frameHeader.messageType(), SMessageType.CONNECT());
        assertEquals(frameHeader.messageLength(), buf.readableBytes());
        System.out.println(buf.readableBytes());

        Connect connectHeaderOut = Connect.decode(buf);
        assertConnect(connectHeaderIn, connectHeaderOut);
    }

    private static void assertConnect(Connect in, Connect out) {
        assertEquals(in.clientId(), out.clientId());
        assertEquals(in.keepAliveDuration(), out.keepAliveDuration());
        assertEquals(in.password(), out.password());
        assertEquals(in.userName(), out.userName());
        assertEquals(in.willMessage(), out.willMessage());
        assertEquals(in.willTopic(), out.willTopic());
        assertEquals(in.willQoS(), out.willQoS());
        assertEquals(in.isCleanSession(), out.isCleanSession());
        assertEquals(in.willFlag(), out.willFlag());
        assertEquals(in.willRetain(), out.willRetain());
    }
}
