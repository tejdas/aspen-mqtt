package net.aspenmq.transport.connection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.IOException;

import net.aspenmq.transport.protocol.Connect;

public class AMQConnection {
    private final Channel channel;

    private AMQConnectionHandler handler = null;

    public AMQConnection(Channel channel) {
        super();
        this.channel = channel;
        handler = (AMQConnectionHandler) channel.pipeline()
                .last();
    }

    void initiateHandshake() {
        Connect connectHeaderIn = new Connect();
        connectHeaderIn.setKeepAliveDuration(30);
        try {
            ByteBuf buf = connectHeaderIn.encode();
            channel.writeAndFlush(buf).sync();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    public void close() {
        ChannelFuture cf = channel.close();
        try {
            cf.sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
