package net.aspenmq.transport.protocol;

import io.netty.buffer.ByteBuf;

public class CodecUtil {
    static void writeInt(ByteBuf buf, int value) {
        buf.writeInt(value);
    }
}
