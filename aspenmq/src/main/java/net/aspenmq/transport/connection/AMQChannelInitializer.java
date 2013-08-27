package net.aspenmq.transport.connection;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class AMQChannelInitializer extends
        ChannelInitializer<SocketChannel> {

    public AMQChannelInitializer() {
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // Add the number codec first,
        // pipeline.addLast("decoder", new BigIntegerDecoder());
        // pipeline.addLast("encoder", new NumberEncoder());

        // and then business logic.
        // pipeline.addLast("handler", new FactorialClientHandler(count));
        pipeline.addLast("frameDecoder", new AMQFrameDecoder());
        pipeline.addLast("handler", new AMQConnectionHandler());
    }
}
