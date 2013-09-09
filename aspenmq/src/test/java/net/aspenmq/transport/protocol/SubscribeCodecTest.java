package net.aspenmq.transport.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import junit.framework.TestCase;
import net.aspenmq.transport.frame.SFrameHeader;
import net.aspenmq.transport.frame.SMessageType;
import net.aspenmq.transport.frame.SQoS;

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
        sub.addTopic(SQoS.QOS_ATLEAST_ONCE(), "foo");
        sub.addTopic(SQoS.QOS_ATMOST_ONCE(), "bar");
        sub.addTopic(SQoS.QOS_EXACTLY_ONCE(), "nook");

        ByteBuf buf = sub.encode(true);
        SFrameHeader frameHeader = SFrameHeader.parseHeader(buf);
        assertTrue(frameHeader != null);
        assertEquals(frameHeader.messageType(), SMessageType.SUBSCRIBE());
        assertEquals(frameHeader.messageLength(), buf.readableBytes());
        System.out.println(buf.readableBytes());

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
        System.out.println(buf.readableBytes());

        SubscribeAck subOut = SubscribeAck.decode(buf);
        assertSubscriberAck(sub, subOut);
    }

    private static void assertSubscriber(Subscribe in, Subscribe out) {
        assertEquals(in.messsageId(), out.messsageId());
        List<Tuple2<Value, String>> inTopicList = in.getTopics();
        List<Tuple2<Value, String>> outTopicList = out.getTopics();
        assertEquals(inTopicList.size(), outTopicList.size());

        for (Tuple2<Value, String> topicDetail : JavaConversions.asJavaIterable(outTopicList)) {
            System.out.println(topicDetail._2);
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
            System.out.println(qosVal.id());
            assertTrue(inQosList.contains(qosVal));
        }
    }
}
