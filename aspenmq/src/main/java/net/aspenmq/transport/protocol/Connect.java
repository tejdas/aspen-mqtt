package net.aspenmq.transport.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.util.UUID;

import net.aspenmq.transport.frame.FrameHeader;
import net.aspenmq.transport.frame.FrameHeader.QoS;
import net.aspenmq.transport.frame.MessageType;

import org.apache.commons.lang.StringUtils;

public class Connect implements ProtocolMessage {
    private static final String MQTT_PROTOCOL_NAME = "MQIsdp";
    private static final int MQTT_PROTOCOL_VERSION = 0x03;

    private int keepAliveDuration = 0;
    private boolean isCleanSession = true;
    private boolean willFlag = false;
    private QoS willQoS = QoS.QOS_RESERVED;
    private boolean willRetain = false;
    private String clientId = StringUtils.EMPTY;
    private String willTopic = StringUtils.EMPTY;
    private String willMessage = StringUtils.EMPTY;
    private String userName = StringUtils.EMPTY;
    private String password = StringUtils.EMPTY;

    @Override
    public ByteBuf encode() throws IOException {
        ByteBuf buf = Unpooled.buffer(256);
        ByteBufOutputStream bos = new ByteBufOutputStream(buf);
        bos.writeUTF(MQTT_PROTOCOL_NAME);
        bos.writeByte(MQTT_PROTOCOL_VERSION);

        byte flag = 0;
        if (isCleanSession) {
            flag |= 0x02;
        }
        if (willFlag) {
            flag |= 0x04;
            flag |= (willQoS.qosVal() << 3);
            if (willRetain) {
                flag |= 0x20;
            }
        }
        if (!StringUtils.isEmpty(password)) {
            flag |= 0x40;
        }
        if (!StringUtils.isEmpty(userName)) {
            flag |= 0x80;
        }
        bos.writeByte(flag);
        bos.writeInt(keepAliveDuration);
        clientId = UUID.randomUUID().toString();
        bos.writeUTF(clientId);
        if (willFlag) {
            bos.writeUTF(willTopic);
            bos.writeUTF(willMessage);
        }
        if (!StringUtils.isEmpty(userName)) {
            bos.writeUTF(userName);
        }
        if (!StringUtils.isEmpty(password)) {
            bos.writeUTF(password);
        }
        bos.flush();
        FrameHeader frameHeader = new FrameHeader(false,
                QoS.QOS_RESERVED,
                false,
                MessageType.CONNECT,
                buf.readableBytes());
        byte[] headerBuf = new byte[FrameHeader.FIXED_HEADER_MAX_LENGTH];
        int headerLength = frameHeader.marshalHeader(headerBuf);
        ByteBuf conBuf = Unpooled.buffer(headerLength + buf.readableBytes());
        conBuf.writeBytes(headerBuf, 0, headerLength);
        conBuf.writeBytes(buf, buf.readableBytes());
        return conBuf;
    }

    public static Connect decode(ByteBuf buf) throws IOException {
        Connect connect = new Connect();
        ByteBufInputStream bis = new ByteBufInputStream(buf);
        String protocolName = bis.readUTF();
        int version = bis.readByte();
        byte flag = bis.readByte();
        connect.isCleanSession = ((flag & 0x02) == 0x02);
        connect.willFlag = ((flag & 0x04) == 0x04);
        if (connect.willFlag) {
            connect.willQoS = QoS.valueOf(((flag >> 3) & 0x03));
            connect.willRetain = ((flag & 0x20) == 0x20);
        }

        boolean isPwdProvided = ((flag & 0x40) == 0x40);
        boolean isUserProvided = ((flag & 0x80) == 0x80);

        connect.keepAliveDuration = bis.readInt();
        connect.clientId = bis.readUTF();
        if (connect.willFlag) {
            connect.willTopic = bis.readUTF();
            connect.willMessage = bis.readUTF();
        }
        if (isUserProvided) {
            connect.userName = bis.readUTF();
        }
        if (isPwdProvided) {
            connect.password = bis.readUTF();
        }
        return connect;
    }

    public int getKeepAliveDuration() {
        return keepAliveDuration;
    }

    public void setKeepAliveDuration(int keepAliveDuration) {
        this.keepAliveDuration = keepAliveDuration;
    }

    public boolean isCleanSession() {
        return isCleanSession;
    }

    public void setCleanSession(boolean isCleanSession) {
        this.isCleanSession = isCleanSession;
    }

    public boolean isWillFlag() {
        return willFlag;
    }

    public void setWillFlag(boolean willFlag) {
        this.willFlag = willFlag;
    }

    public QoS getWillQoS() {
        return willQoS;
    }

    public void setWillQoS(QoS willQoS) {
        this.willQoS = willQoS;
    }

    public boolean isWillRetain() {
        return willRetain;
    }

    public void setWillRetain(boolean willRetain) {
        this.willRetain = willRetain;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getWillTopic() {
        return willTopic;
    }

    public void setWillTopic(String willTopic) {
        this.willTopic = willTopic;
    }

    public String getWillMessage() {
        return willMessage;
    }

    public void setWillMessage(String willMessage) {
        this.willMessage = willMessage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
