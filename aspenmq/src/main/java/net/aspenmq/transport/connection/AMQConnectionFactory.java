package net.aspenmq.transport.connection;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class AMQConnectionFactory {

    private EventLoopGroup group = null;
    private Bootstrap b;
    public void initialize() {
         group = new NioEventLoopGroup();
        try {
            b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class);

            b.handler(new AMQChannelInitializer());
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
             ChannelFuture f = b.connect(host, AMQConnectionConstants.MQTT_PORT).sync();
             Channel ch = f.sync().channel();
             System.out.println("Connected");
             Thread.sleep(2000);
             AMQConnection connection = new AMQConnection(ch);
             connection.initiateHandshake();
             return connection;
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
        }
        return null;
    }
}
