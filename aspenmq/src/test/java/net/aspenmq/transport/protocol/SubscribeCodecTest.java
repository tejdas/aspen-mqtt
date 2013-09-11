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

import scala.Enumeration.Value;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.immutable.List;

public class SubscribeCodecTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testSubscribeCodec() throws IOException {
        Subscribe sub = new Subscribe(12);
        sub.addTopic(SQoS.QOS_ATLEAST_ONCE(), RandomStringUtils.randomAlphanumeric(32));
        sub.addTopic(SQoS.QOS_ATMOST_ONCE(), RandomStringUtils.randomAlphanumeric(32));
        sub.addTopic(SQoS.QOS_EXACTLY_ONCE(), RandomStringUtils.randomAlphanumeric(32));

        ByteBuf buf = sub.encode(true);
        SFrameHeader frameHeader = SFrameHeader.parseHeader(buf);
        assertTrue(frameHeader != null);
        assertEquals(frameHeader.messageType(), SMessageType.SUBSCRIBE());
        assertEquals(frameHeader.messageLength(), buf.readableBytes());

        Subscribe subOut = Subscribe.decode(buf);
        assertSubscriber(sub, subOut);
    }

    @Test
    public void testSubscribeAckCodec() throws IOException {
        SubscribeAck sub = new SubscribeAck(12);
        sub.addGrantedQoS(SQoS.QOS_ATLEAST_ONCE());
        sub.addGrantedQoS(SQoS.QOS_ATMOST_ONCE());
        sub.addGrantedQoS(SQoS.QOS_ATLEAST_ONCE());
        sub.addGrantedQoS(SQoS.QOS_EXACTLY_ONCE());

        ByteBuf buf = sub.encode();
        SFrameHeader frameHeader = SFrameHeader.parseHeader(buf);
        assertTrue(frameHeader != null);
        assertEquals(frameHeader.messageType(), SMessageType.SUBACK());
        assertEquals(frameHeader.messageLength(), buf.readableBytes());

        SubscribeAck subOut = SubscribeAck.decode(buf);
        assertSubscriberAck(sub, subOut);
    }

    @Test
    public void testUnsubscribeCodec() throws IOException {
        Unsubscribe sub = new Unsubscribe(12);
        sub.addTopic(RandomStringUtils.randomAlphanumeric(32));
        sub.addTopic(RandomStringUtils.randomAlphanumeric(32));
        sub.addTopic(RandomStringUtils.randomAlphanumeric(32));

        ByteBuf buf = sub.encode(true);
        SFrameHeader frameHeader = SFrameHeader.parseHeader(buf);
        assertTrue(frameHeader != null);
        assertEquals(frameHeader.messageType(), SMessageType.UNSUBSCRIBE());
        assertEquals(frameHeader.messageLength(), buf.readableBytes());

        Unsubscribe subOut = Unsubscribe.decode(buf);
        assertUnsubscriber(sub, subOut);
    }

    @Test
    public void testUnsubscribeAckCodec() {
        int msgId = new Random().nextInt(1024);
        ByteBuf pubrec = new UnsubscribeAck(msgId).encode(false);
        SFrameHeader frameHeader = SFrameHeader.parseHeader(pubrec);
        assertEquals(frameHeader.messageType(), SMessageType.UNSUBACK());
        assertTrue(!frameHeader.duplicate());
        UnsubscribeAck result = (UnsubscribeAck) AckProtocolMessage$.MODULE$.decode(pubrec, frameHeader.messageType());
        assertEquals(msgId, result.messageId());
        assertEquals(SQoS.QOS_RESERVED(), result.qos());
    }

    private static void assertSubscriber(Subscribe in, Subscribe out) {
        assertEquals(in.messsageId(), out.messsageId());
        List<Tuple2<Value, String>> inTopicList = in.getTopics();
        List<Tuple2<Value, String>> outTopicList = out.getTopics();
        assertEquals(inTopicList.size(), outTopicList.size());

        for (Tuple2<Value, String> topicDetail : JavaConversions.asJavaIterable(outTopicList)) {
            System.out.println(topicDetail._1());
            assertTrue(inTopicList.contains(topicDetail));
        }
    }

    private static void assertSubscriberAck(SubscribeAck in, SubscribeAck out) {
        assertEquals(in.messsageId(), out.messsageId());
        List<Value> inQosList = in.getGrantedQoS();
        List<Value> outQosList = out.getGrantedQoS();
        assertEquals(inQosList.size(), outQosList.size());

        for (Value qosVal : JavaConversions.asJavaIterable(outQosList)) {
            assertTrue(inQosList.contains(qosVal));
        }
    }

    private static void assertUnsubscriber(Unsubscribe in, Unsubscribe out) {
        assertEquals(in.messsageId(), out.messsageId());
        List<String> inTopicList = in.getTopics();
        List<String> outTopicList = out.getTopics();
        assertEquals(inTopicList.size(), outTopicList.size());
        for (String topic : JavaConversions.asJavaIterable(outTopicList)) {
            assertTrue(inTopicList.contains(topic));
        }
    }
}
