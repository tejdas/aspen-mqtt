package net.aspenmq.transport.connection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class AMQFrameDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext arg0,
            ByteBuf buf,
            List<Object> arg2) throws Exception {
        System.out.println("in decode: " + buf.readableBytes());
        if (buf.readableBytes() > 0) {
        //ByteBufInputStream bis = new ByteBufInputStream(buf);
        //String protocolName = bis.readUTF();
        //System.out.println(protocolName);
        //arg2.add(protocolName);
        }
    }

    protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        System.out.println("In decodeLast: " + ctx.channel().localAddress().toString());
    }
}
