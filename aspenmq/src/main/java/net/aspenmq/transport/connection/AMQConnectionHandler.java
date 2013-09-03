package net.aspenmq.transport.connection;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.aspenmq.transport.frame.MessageType;
import net.aspenmq.transport.protocol.Connect;

public class AMQConnectionHandler extends ChannelInboundHandlerAdapter {
    private volatile AMQConnection connection = null;
    void registerConnection(AMQConnection connection) {
        this.connection = connection;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof AMQMessage)) {
            return;
        }
        AMQMessage message = (AMQMessage) msg;
        if (message.frameHeader().getMessageType() == MessageType.CONNECT) {
            System.out.println("received CONNECT");
            Connect connect = (Connect) message.protocolMessage();
            connection = new AMQConnection(ctx.channel(), this);
            connection.processConnect(connect);
        } else {
            System.out.println("received protocolMessage:" + message.frameHeader().getMessageType());
            connection.processProtocolMessage(message);
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