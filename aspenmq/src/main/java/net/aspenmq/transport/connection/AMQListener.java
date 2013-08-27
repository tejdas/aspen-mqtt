package net.aspenmq.transport.connection;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public final class AMQListener {

    private volatile boolean hasShutdown = false;

    private final String listenAddress = "0.0.0.0";

    private volatile Channel serverChannel = null;

    private final EventLoopGroup bossGroup = new NioEventLoopGroup();

    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    public void start() {
        System.out.println("DoveMQ Listener on port: " + AMQConnectionConstants.MQTT_PORT);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100);

            b.childHandler(new AMQChannelInitializer());

            ChannelFuture f = b.bind(listenAddress, AMQConnectionConstants.MQTT_PORT);
            f.sync();
            serverChannel = f.channel();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
        }
    }

    public void shutdown() {
        if (hasShutdown) {
            return;
        }
        hasShutdown = true;
        try {
            // Wait until the server socket is closed.
            if (serverChannel != null) {
                serverChannel.close().sync();
                serverChannel = null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        System.out.println("AspenMQ Listener shut down");
    }
}
