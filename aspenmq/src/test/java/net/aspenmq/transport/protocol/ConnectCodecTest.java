package net.aspenmq.transport.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import junit.framework.TestCase;
import net.aspenmq.transport.frame.FrameHeader;
import net.aspenmq.transport.frame.FrameHeader.QoS;
import net.aspenmq.transport.frame.MessageType;

import org.junit.Test;

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
        connectCodecTest(45, "testuser", "testpwd", true, QoS.QOS_ATMOST_ONCE, "randomTopic", false);
        connectCodecTest(9314, "testuser", "testpwd", true, QoS.QOS_EXACTLY_ONCE, null, true);
        connectCodecTest(9314, "testuser", "testpwd", false, QoS.QOS_ATLEAST_ONCE, "foobar", true);
        connectCodecTest(5982, null, null, true, QoS.QOS_ATMOST_ONCE, "randomTopic", true);
    }

    private void connectCodecTest(int keepAliveDuration,
            String user,
            String pwd,
            boolean willFlag,
            QoS willQoS,
            String willTopic,
            boolean willRetain) throws IOException {
        Connect connectHeaderIn = new Connect();
        connectHeaderIn.setKeepAliveDuration(keepAliveDuration);
        if (user != null) {
            connectHeaderIn.setUserName(user);
        }
        if (pwd != null) {
            connectHeaderIn.setPassword(pwd);
        }
        connectHeaderIn.setWillFlag(true);
        connectHeaderIn.setWillQoS(willQoS);
        if (willTopic != null) {
            connectHeaderIn.setWillTopic(willTopic);
        }
        connectHeaderIn.setWillRetain(willRetain);

        ByteBuf buf = connectHeaderIn.encode();
        FrameHeader frameHeader = FrameHeader.parseHeader(buf);
        assertTrue(frameHeader != null);
        assertEquals(frameHeader.getMessageType(), MessageType.CONNECT);
        assertEquals(frameHeader.getMessageLength(), buf.readableBytes());

        Connect connectHeaderOut = Connect.decode(buf);
        assertConnect(connectHeaderIn, connectHeaderOut);
    }

    private static void assertConnect(Connect in, Connect out) {
        assertEquals(in.getClientId(), out.getClientId());
        assertEquals(in.getKeepAliveDuration(), out.getKeepAliveDuration());
        assertEquals(in.getPassword(), out.getPassword());
        assertEquals(in.getUserName(), out.getUserName());
        assertEquals(in.getWillMessage(), out.getWillMessage());
        assertEquals(in.getWillTopic(), out.getWillTopic());
        assertEquals(in.getWillQoS().qosVal(), out.getWillQoS().qosVal());
        assertEquals(in.isCleanSession(), out.isCleanSession());
        assertEquals(in.isWillFlag(), out.isWillFlag());
        assertEquals(in.isWillRetain(), out.isWillRetain());
    }
}
