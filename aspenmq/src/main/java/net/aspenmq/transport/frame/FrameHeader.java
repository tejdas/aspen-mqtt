package net.aspenmq.transport.frame;

import io.netty.buffer.ByteBuf;

import java.util.Arrays;

public class FrameHeader {
    public static final int FIXED_HEADER_MIN_LENGTH = 2;
    public static final int FIXED_HEADER_MAX_LENGTH = 5;

    public static enum QoS {
        QOS_ATMOST_ONCE(0),
        QOS_ATLEAST_ONCE(1),
        QOS_EXACTLY_ONCE(2),
        QOS_RESERVED(4);

        private final int qosVal;

        private QoS(int qosVal) {
            this.qosVal = qosVal;
        }

        public int qosVal() {
            return qosVal;
        }

        public static QoS valueOf(int val) {
            for (QoS qos : QoS.values()) {
                if (qos.qosVal == val) {
                    return qos;
                }
            }
            throw new IllegalArgumentException();
        }
    }

    /**
     * Marshals MQTT header
     * @param header
     * @return
     *      length of the header
     */
    public int marshalHeader(byte[] header) {
        Arrays.fill(header, (byte) 0);
        int pos = 0;
        if (retain) {
            header[pos] |= 0x01;
        }

        header[pos] |= (qos.qosVal << 1);

        if (isDuplicate) {
            header[pos] |= 0x08;
        }

        header[pos] |= (messageType.type() << 4);
        int msgLen = messageLength;
        do {
            pos++;
            int val = msgLen % 128;
            header[pos] = (byte) (val & 0xFF);
            msgLen = msgLen / 128;
            if (msgLen > 0) {
                header[pos] |= 0x80;
            }
        } while ((msgLen > 0) && (pos < FIXED_HEADER_MAX_LENGTH - 1));

        if (msgLen > 0) {
            throw new IllegalArgumentException("message length not allowed: " + messageLength);
        }
        return pos+1;
    }

    public static FrameHeader parseHeader(ByteBuf headerBuf) {
        if (headerBuf.readableBytes() < FIXED_HEADER_MIN_LENGTH) {
            return null;
        }
        int pos = 0;
        headerBuf.markReaderIndex();
        byte currentByte = headerBuf.readByte();
        boolean retain = ((currentByte & 0x01) == 1);
        int qosVal = (currentByte >> 1) & 0x03;
        boolean isDuplicate = (((currentByte >> 3) & 0x01) == 1);
        int msgType = (currentByte >> 4) & 0x0F;

        int messageLength = 0; // TODO
        int multiplier = 1;
        do {
            pos++;
            if (pos == FIXED_HEADER_MAX_LENGTH) {
                throw new RuntimeException("message length not allowed");
            }
            if (!headerBuf.isReadable()) {
                /*
                 * Reached end of buffer before reading the complete frame header.
                 */
                headerBuf.resetReaderIndex();
                return null;
            }
            currentByte = headerBuf.readByte();
            messageLength += (currentByte & 0x7F) * multiplier;
            multiplier *= 128;
        } while ((currentByte & 0x80) != 0);

        return new FrameHeader(retain,
                QoS.valueOf(qosVal),
                isDuplicate,
                MessageType.valueOf(msgType),
                messageLength);
    }

    public FrameHeader(boolean retain,
            QoS qos,
            boolean isDuplicate,
            MessageType messageType,
            int messageLength) {
        super();
        this.retain = retain;
        this.qos = qos;
        this.isDuplicate = isDuplicate;
        this.messageType = messageType;
        this.messageLength = messageLength;
    }

    public boolean isRetain() {
        return retain;
    }

    public boolean isDuplicate() {
        return isDuplicate;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public QoS getQos() {
        return qos;
    }

    public int getMessageLength() {
        return messageLength;
    }

    private final boolean retain;

    private final QoS qos;

    private final boolean isDuplicate;

    private final MessageType messageType;

    private final int messageLength;
}
