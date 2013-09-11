package net.aspenmq.transport.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.Random;

import junit.framework.TestCase;
import net.aspenmq.transport.frame.SFrameHeader;
import net.aspenmq.transport.frame.SMessageType;
import net.aspenmq.transport.frame.SQoS;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

public class PublishCodecTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testPublishCodec() throws IOException {
        String payloadIn = RandomStringUtils.randomAlphanumeric(16384);
        Publish pub = new Publish(12, RandomStringUtils.randomAscii(64), payloadIn.getBytes());

        ByteBuf buf = pub.encode(SQoS.QOS_ATLEAST_ONCE(), false, true);
        SFrameHeader frameHeader = SFrameHeader.parseHeader(buf);
        assertTrue(frameHeader.qos() == SQoS.QOS_ATLEAST_ONCE());
        assertTrue(!frameHeader.duplicate());
        assertTrue(frameHeader.retain());
        assertTrue(frameHeader != null);
        assertEquals(frameHeader.messageType(), SMessageType.PUBLISH());
        assertEquals(frameHeader.messageLength(), buf.readableBytes());
        System.out.println(buf.readableBytes());

        Publish pubOut = Publish.decode(buf);
        assertPublish(pub, pubOut);
    }

    @Test
    public void testPubAckCodec() {
        int msgId = new Random().nextInt(1024);
        ByteBuf puback = new PublishAck(msgId).encode(false);
        SFrameHeader frameHeader = SFrameHeader.parseHeader(puback);
        assertEquals(frameHeader.messageType(), SMessageType.PUBACK());
        assertFalse(frameHeader.duplicate());
        PublishAck result = (PublishAck) AckProtocolMessage$.MODULE$.decode(puback, frameHeader.messageType());
        assertEquals(msgId, result.messageId());
        assertEquals(SQoS.QOS_RESERVED(), result.qos());
    }

    @Test
    public void testPubRelCodec() {
        int msgId = new Random().nextInt(1024);
        ByteBuf pubrel = new PublishRel(msgId).encode(false);
        SFrameHeader frameHeader = SFrameHeader.parseHeader(pubrel);
        assertEquals(frameHeader.messageType(), SMessageType.PUBREL());
        assertFalse(frameHeader.duplicate());
        PublishRel result = (PublishRel) AckProtocolMessage$.MODULE$.decode(pubrel, frameHeader.messageType());
        assertEquals(msgId, result.messageId());
        assertEquals(SQoS.QOS_ATLEAST_ONCE(), result.qos());
    }

    @Test
    public void testPubRecCodec() {
        int msgId = new Random().nextInt(1024);
        ByteBuf pubrec = new PublishRec(msgId).encode(false);
        SFrameHeader frameHeader = SFrameHeader.parseHeader(pubrec);
        assertEquals(frameHeader.messageType(), SMessageType.PUBREC());
        assertTrue(!frameHeader.duplicate());
        PublishRec result = (PublishRec) AckProtocolMessage$.MODULE$.decode(pubrec, frameHeader.messageType());
        assertEquals(msgId, result.messageId());
        assertEquals(SQoS.QOS_RESERVED(), result.qos());
    }

    @Test
    public void testPubRelCodec2() {
        int msgId = new Random().nextInt(1024);
        ByteBuf pubrel = new PublishRel(msgId).encode(true);
        SFrameHeader frameHeader = SFrameHeader.parseHeader(pubrel);
        assertEquals(frameHeader.messageType(), SMessageType.PUBREL());
        assertTrue(frameHeader.duplicate());
        PublishRel result = (PublishRel) AckProtocolMessage$.MODULE$.decode(pubrel, frameHeader.messageType());
        assertEquals(msgId, result.messageId());
        assertEquals(SQoS.QOS_ATLEAST_ONCE(), result.qos());
    }

    @Test
    public void testPubCompCodec() {
        int msgId = new Random().nextInt(1024);
        ByteBuf pubrel = new PublishComp(msgId).encode(false);
        SFrameHeader frameHeader = SFrameHeader.parseHeader(pubrel);
        assertEquals(frameHeader.messageType(), SMessageType.PUBCOMP());
        assertFalse(frameHeader.duplicate());
        PublishComp result = (PublishComp) AckProtocolMessage$.MODULE$.decode(pubrel, frameHeader.messageType());
        assertEquals(msgId, result.messageId());
        assertEquals(SQoS.QOS_RESERVED(), result.qos());
    }

    private static void assertPublish(Publish in, Publish out) {
        assertEquals(in.messsageId(), out.messsageId());
        assertEquals(in.topic(), out.topic());
        String payloadIn = new String(in.payload());
        String payloadOut = new String(out.payload());
        assertEquals(payloadIn, payloadOut);
    }
}
