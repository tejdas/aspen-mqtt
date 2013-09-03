package net.aspenmq.transport.connection;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class AMQConnectionFactory {

    private EventLoopGroup group = null;
    private Bootstrap bootStrap;
    public void initialize() {
         group = new NioEventLoopGroup();
        try {
            bootStrap = new Bootstrap();
            bootStrap.group(group)
             .channel(NioSocketChannel.class);
            bootStrap.handler(new AMQChannelInitializerFactory().createChannelInitializer());
        } finally {
        }
    }

    public void shutdown() {
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    public AMQConnection createConnection(String host) {
        try {
             ChannelFuture f = bootStrap.connect(host, AMQConnectionConstants.MQTT_PORT).sync();
             Channel ch = f.sync().channel();
             Thread.sleep(2000);
             AMQConnection connection = new AMQConnection(ch);
             connection.performHandshake();
             return connection;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
        }
        return null;
    }
}
