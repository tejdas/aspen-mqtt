package net.aspenmq.transport.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

public interface ProtocolMessage {
    public ByteBuf encode() throws IOException;
}
