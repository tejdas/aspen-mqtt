package net.aspenmq.transport.frame;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Arrays;

import junit.framework.TestCase;

import org.junit.Test;

import scala.Enumeration.Value;

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
                SQoS.QOS_ATLEAST_ONCE(),
                false,
                SMessageType.CONNECT(),
                89);
        testAndAssertFrameHeader(false,
                SQoS.QOS_EXACTLY_ONCE(),
                true,
                SMessageType.PUBLISH(),
                9932);
        testAndAssertFrameHeader(false,
                SQoS.QOS_ATMOST_ONCE(),
                false,
                SMessageType.PUBLISH(),
                25372);
        testAndAssertFrameHeader(true,
                SQoS.QOS_ATMOST_ONCE(),
                true,
                SMessageType.PUBLISH(),
                123678954);
        try {
            testAndAssertFrameHeader(true,
                    SQoS.QOS_EXACTLY_ONCE(),
                    true,
                    SMessageType.PUBLISH(),
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
        header[pos] |= (SQoS.QOS_ATLEAST_ONCE().id() << 1);
        header[pos] |= 0x08;
        header[pos] |= (SMessageType.PUBLISH().id() << 4);

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
            ByteBuf headerBuf = Unpooled.copiedBuffer(header);
            SFrameHeader.parseHeader(headerBuf);
            fail("Expected RuntimeException");
        } catch (RuntimeException ignore) {
        }
    }

    private static void testAndAssertFrameHeader(boolean retain,
            Value qos,
            boolean isDuplicate,
            Value msgType,
            int msgLen) {
        byte[] header = new byte[SFrameHeader.FIXED_HEADER_MAX_LENGTH()];
        SFrameHeader frameHeaderIn = new SFrameHeader(retain,
                qos,
                isDuplicate,
                msgType,
                msgLen);
        frameHeaderIn.marshalHeader(header);

        ByteBuf headerBuf = Unpooled.copiedBuffer(header);
        SFrameHeader frameHeaderOut = SFrameHeader.parseHeader(headerBuf);
        assertFrameHeader(frameHeaderIn, frameHeaderOut);
    }

    private static void assertFrameHeader(SFrameHeader in, SFrameHeader out) {
        assertEquals(in.duplicate(), out.duplicate());
        assertEquals(in.retain(), out.retain());
        assertEquals(in.messageType().id(), out.messageType().id());
        assertEquals(in.qos(), out.qos());
        assertEquals(in.messageLength(), out.messageLength());
    }
}