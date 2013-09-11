package net.aspenmq.transport.connection;

import static org.junit.Assert.assertEquals;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.aspenmq.transport.frame.SQoS;
import net.aspenmq.transport.protocol.Connect;
import net.aspenmq.transport.protocol.ConnectAck;
import net.aspenmq.transport.protocol.Disconnect;
import net.aspenmq.transport.protocol.PingRequest;
import net.aspenmq.transport.protocol.PingResponse;
import net.aspenmq.transport.protocol.Publish;
import net.aspenmq.transport.protocol.Subscribe;
import net.aspenmq.transport.protocol.UnsubscribeAck;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AMQFrameDecoderTest {
    @Sharable
    private static class TestConnectionHandler extends
            ChannelInboundHandlerAdapter {
        private final List<AMQMessage> receivedMessages = new ArrayList<>();

        public synchronized List<AMQMessage> getAndReset() {
            List<AMQMessage> messages = new ArrayList<>();
            messages.addAll(receivedMessages);
            receivedMessages.clear();
            return messages;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            synchronized (this) {
                receivedMessages.add((AMQMessage) msg);
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }

    private static final TestConnectionHandler handler = new TestConnectionHandler();

    private static class ChannelInitFactory extends
            AMQChannelInitializerFactory {
        @Override
        public AMQChannelInitializer createChannelInitializer() {
            return new AMQChannelInitializer() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("frameDecoder", new AMQFrameDecoder());
                    pipeline.addLast("handler", handler);
                }
            };
        }
    }

    private EventLoopGroup group = null;

    private Bootstrap b;

    private static AMQListener listener = null;

    @BeforeClass
    public static void setupBeforeClass() {
        AMQListener.channelInitializerFactory_$eq(new ChannelInitFactory());
        listener = new AMQListener();
        listener.start();

    }

    @AfterClass
    public static void teardownAfterClass() {
        listener.shutdown();
    }

    @Before
    public void setUp() throws Exception {
        group = new NioEventLoopGroup();
        b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class);
        b.handler(new ChannelInitFactory().createChannelInitializer());
    }

    @After
    public void tearDown() throws Exception {
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    @Test
    public void testMultipleFrames() throws InterruptedException, IOException {
        ChannelFuture f = b.connect("localhost",
                AMQConnectionConstants.MQTT_PORT).sync();
        Channel channel = f.sync().channel();
        Thread.sleep(2000);

        Connect connectHeaderIn = new Connect();
        connectHeaderIn.keepAliveDuration_$eq(30);
        connectHeaderIn.userName_$eq(RandomStringUtils.randomAlphanumeric(64));
        connectHeaderIn.password_$eq(RandomStringUtils.randomAlphanumeric(64));
        connectHeaderIn.willTopic_$eq(RandomStringUtils.randomAlphanumeric(64));

        ByteBuf buf = connectHeaderIn.encode();

        ConnectAck connack = new ConnectAck(ConnectAck.CONNECTION_ACCEPTED());
        ByteBuf buf2 = connack.encode();
        ByteBuf composite = Unpooled.wrappedBuffer(buf, buf2, buf);
        channel.writeAndFlush(composite).sync();
        Thread.sleep(2000);

        ChannelFuture cf = channel.close();
        cf.sync();
        Thread.sleep(1000);
        List<AMQMessage> messages = handler.getAndReset();
        assertEquals(3, messages.size());
    }

    @Test
    public void testMultipleFrameChunksNotAllignedAtFrameBoundary() throws InterruptedException,
            IOException {
        ChannelFuture f = b.connect("localhost",
                AMQConnectionConstants.MQTT_PORT).sync();
        Channel channel = f.sync().channel();
        Thread.sleep(2000);

        Connect connectHeaderIn = new Connect();
        connectHeaderIn.keepAliveDuration_$eq(30);
        connectHeaderIn.userName_$eq(RandomStringUtils.randomAlphanumeric(64));
        connectHeaderIn.password_$eq(RandomStringUtils.randomAlphanumeric(64));
        connectHeaderIn.willTopic_$eq(RandomStringUtils.randomAlphanumeric(64));

        ByteBuf connBuf = connectHeaderIn.encode();
        ByteBuf connackBuf = new ConnectAck(ConnectAck.CONNECTION_ACCEPTED()).encode();

        String payloadIn = RandomStringUtils.randomAlphanumeric(2048);
        Publish pub = new Publish(12,
                RandomStringUtils.randomAscii(64),
                payloadIn.getBytes());
        ByteBuf pubBuf = pub.encode(SQoS.QOS_ATLEAST_ONCE(), true, false);

        Subscribe sub = new Subscribe(12);
        sub.addTopic(SQoS.QOS_ATLEAST_ONCE(), RandomStringUtils.randomAscii(32));
        sub.addTopic(SQoS.QOS_ATMOST_ONCE(), RandomStringUtils.randomAscii(32));
        sub.addTopic(SQoS.QOS_EXACTLY_ONCE(), RandomStringUtils.randomAscii(32));
        ByteBuf subBuf = sub.encode(true);

        ByteBuf discBuf = new Disconnect().encode();

        ByteBuf pingReqBuf = new PingRequest().encode();

        ByteBuf pingRespBuf = new PingResponse().encode();

        ByteBuf unsubAckBuf = new UnsubscribeAck(15).encode(true);

        ByteBuf composite = Unpooled.wrappedBuffer(connBuf,
                connackBuf,
                subBuf,
                pingReqBuf,
                connackBuf,
                subBuf,
                discBuf,
                pubBuf,
                unsubAckBuf,
                connackBuf,
                pingRespBuf,
                connBuf);

        while (composite.isReadable()) {
            ByteBuf writeBuf = Unpooled.buffer(64);
            if (composite.readableBytes() >= 64)
                composite.readBytes(writeBuf);
            else
                composite.readBytes(writeBuf, composite.readableBytes());
            channel.writeAndFlush(writeBuf).sync();
            Thread.sleep(50);
        }

        ChannelFuture cf = channel.close();
        cf.sync();
        Thread.sleep(2000);
        List<AMQMessage> messages = handler.getAndReset();
        assertEquals(12, messages.size());
    }

    @Test
    public void testParseWithInsufficientFrameHeader() throws InterruptedException, IOException {
        ChannelFuture f = b.connect("localhost",
                AMQConnectionConstants.MQTT_PORT).sync();
        Channel channel = f.sync().channel();
        Thread.sleep(2000);

        Connect connectHeaderIn = new Connect();
        connectHeaderIn.keepAliveDuration_$eq(30);
        connectHeaderIn.userName_$eq(RandomStringUtils.randomAlphanumeric(64));
        connectHeaderIn.password_$eq(RandomStringUtils.randomAlphanumeric(64));
        connectHeaderIn.willTopic_$eq(RandomStringUtils.randomAlphanumeric(64));

        ByteBuf buf = connectHeaderIn.encode();
        ByteBuf buf4 = Unpooled.buffer(2);
        buf.readBytes(buf4);
        channel.writeAndFlush(buf4).sync();
        Thread.sleep(2000);

        ByteBuf buf3 = Unpooled.buffer(24);
        buf.readBytes(buf3);
        channel.writeAndFlush(buf3).sync();
        Thread.sleep(1000);
        channel.writeAndFlush(buf).sync();

        ConnectAck connack = new ConnectAck(ConnectAck.CONNECTION_ACCEPTED());
        ByteBuf buf2 = connack.encode();
        channel.writeAndFlush(buf2).sync();

        ChannelFuture cf = channel.close();
        cf.sync();
        Thread.sleep(2000);
        List<AMQMessage> messages = handler.getAndReset();
        assertEquals(2, messages.size());
    }
}
