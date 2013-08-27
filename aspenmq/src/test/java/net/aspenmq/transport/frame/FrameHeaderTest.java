package net.aspenmq.transport.frame;

import java.util.Arrays;

import junit.framework.TestCase;
import net.aspenmq.transport.frame.FrameHeader.QoS;

import org.junit.Test;

public class FrameHeaderTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testMQTTFixedHeaderCodec() {
        testAndAssertFrameHeader(true,
                QoS.QOS_ATLEAST_ONCE,
                false,
                MessageType.CONNECT,
                89);
        testAndAssertFrameHeader(false,
                QoS.QOS_EXACTLY_ONCE,
                true,
                MessageType.PUBLISH,
                9932);
        testAndAssertFrameHeader(false,
                QoS.QOS_ATMOST_ONCE,
                false,
                MessageType.PUBLISH,
                25372);
        testAndAssertFrameHeader(true,
                QoS.QOS_ATMOST_ONCE,
                true,
                MessageType.PUBLISH,
                123678954);
        try {
            testAndAssertFrameHeader(true,
                    QoS.QOS_EXACTLY_ONCE,
                    true,
                    MessageType.PUBLISH,
                    423678954);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testParseWithLargeMsgLenExpectedToFail() {
        testParseMessageLengthBeyondLimit(423678954);
        testParseMessageLengthBeyondLimit(Integer.MAX_VALUE);
    }

    private static void testParseMessageLengthBeyondLimit(int msgLen) {
        byte[] header = new byte[10];
        Arrays.fill(header, (byte) 0);

        int pos = 0;
        header[pos] &= 0;
        header[pos] |= 0x01;
        header[pos] |= (QoS.QOS_ATLEAST_ONCE.qosVal() << 1);
        header[pos] |= 0x08;
        header[pos] |= (MessageType.PUBLISH.type() << 4);

        int messageLength = msgLen;
        do {
            pos++;
            int val = messageLength % 128;
            header[pos] = (byte) (val & 0xFF);
            messageLength = messageLength / 128;
            if (messageLength > 0) {
                header[pos] |= 0x80;
            }
        } while (messageLength > 0);

        try {
            FrameHeader.parseHeader(header);
            fail("Expected RuntimeException");
        } catch (RuntimeException ignore) {
        }
    }

    private static void testAndAssertFrameHeader(boolean retain,
            QoS qos,
            boolean isDuplicate,
            MessageType msgType,
            int msgLen) {
        byte[] headerBuf = new byte[FrameHeader.FIXED_HEADER_MAX_LENGTH];
        FrameHeader frameHeaderIn = new FrameHeader(retain,
                qos,
                isDuplicate,
                msgType,
                msgLen);
        frameHeaderIn.marshalHeader(headerBuf);

        FrameHeader frameHeaderOut = FrameHeader.parseHeader(headerBuf);
        assertFrameHeader(frameHeaderIn, frameHeaderOut);
    }

    private static void assertFrameHeader(FrameHeader in, FrameHeader out) {
        assertEquals(in.isDuplicate(), out.isDuplicate());
        assertEquals(in.isRetain(), out.isRetain());
        assertEquals(in.getMessageType(), out.getMessageType());
        assertEquals(in.getQos(), out.getQos());
        assertEquals(in.getMessageLength(), out.getMessageLength());
    }
}