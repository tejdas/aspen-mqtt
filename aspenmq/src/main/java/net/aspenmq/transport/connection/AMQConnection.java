package net.aspenmq.transport.connection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.util.UUID;

import net.aspenmq.transport.frame.MessageType;
import net.aspenmq.transport.protocol.Connect;
import net.aspenmq.transport.protocol.ConnectAck;

public class AMQConnection {
    private final Channel channel;
    private String clientID = null;
    private boolean handshakeComplete = false;
    private final AMQConnectionHandler handler;

    public AMQConnection(Channel channel) {
        super();
        this.channel = channel;
        handler = (AMQConnectionHandler) channel.pipeline()
                .last();
        handler.registerConnection(this);
    }

    public AMQConnection(Channel channel, AMQConnectionHandler handler) {
        super();
        this.channel = channel;
        this.handler = handler;
    }

    void performHandshake() {
        Connect connectHeader = new Connect();
        connectHeader.keepAliveDuration_$eq(30);
        clientID = UUID.randomUUID().toString();
        connectHeader.clientId_$eq(clientID);
        try {
            ByteBuf buf = connectHeader.encode();
            channel.writeAndFlush(buf).sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        synchronized (this) {
            while (!handshakeComplete) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void handshakeComplete() {
        synchronized (this) {
            System.out.println("handshake complete");
            handshakeComplete = true;
            notifyAll();
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

    void processConnect(Connect connect) {
        clientID = connect.clientId();
        SAMQConnectionManager.registerConnection(clientID, this);

        ConnectAck connack = new ConnectAck(ConnectAck.CONNECTION_ACCEPTED());

        try {
            ByteBuf buf = connack.encode();
            channel.writeAndFlush(buf).sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    void processProtocolMessage(AMQMessage protocolMessage) {
        if (protocolMessage.frameHeader().getMessageType() == MessageType.CONNACK) {
            SAMQConnectionManager.registerConnection(clientID, this);
            handshakeComplete();
        } else {
            // TODO handle other types of protocol messages
        }
    }
}
