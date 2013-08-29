package net.aspenmq.transport.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import net.aspenmq.transport.frame.FrameHeader;
import net.aspenmq.transport.frame.FrameHeader.QoS;
import net.aspenmq.transport.frame.MessageType;

public class ConnectAck  implements ProtocolMessage {
    public static final int CONNECTION_ACCEPTED = 0x00;
    public static final int ERROR_PROTOCOL_VERSION = 0x01;
    public static final int ERROR_IDENTIFIER_REJECTED = 0x02;
    public static final int ERROR_SERVER_UNAVAILABLE = 0x03;
    public static final int ERROR_BAD_CREDENTIALS = 0x04;
    public static final int ERROR_UNAUTHORIZED = 0x05;

    private int returnCode;
    int getReturnCode() {
        return returnCode;
    }
    void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    @Override
    public ByteBuf encode() throws IOException {
        FrameHeader frameHeader = new FrameHeader(false,
                QoS.QOS_RESERVED,
                false,
                MessageType.CONNACK,
                2);
        byte[] headerBuf = new byte[FrameHeader.FIXED_HEADER_MAX_LENGTH];
        int headerLength = frameHeader.marshalHeader(headerBuf);

        ByteBuf buf = Unpooled.buffer(4);
        buf.writeBytes(headerBuf, 0, headerLength);
        ByteBufOutputStream bos = new ByteBufOutputStream(buf);
        bos.writeByte(0);
        bos.writeByte(returnCode);
        bos.flush();
        return buf;
    }

    public static ConnectAck decode(ByteBuf buf) throws IOException {
        ConnectAck connack = new ConnectAck();
        ByteBufInputStream bis = new ByteBufInputStream(buf);
        bis.readByte();
        connack.returnCode = bis.readByte();
        return connack;
    }
}
