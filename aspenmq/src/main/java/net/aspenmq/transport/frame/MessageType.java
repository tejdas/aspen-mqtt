package net.aspenmq.transport.frame;


public enum MessageType {
    RESERVED(0),
    CONNECT(1),
    CONNACK(2),
    PUBLISH(3),
    PUBACK(4),
    PUBREC(5),
    PUBREL(6),
    PUBCOMP(7),
    SUBSCRIBE(8),
    SUBACK(9),
    UNSUBSCRIBE(10),
    UNSUBACK(11),
    PINGREQ(12),
    PINGRESP(13),
    DISCONNECT(14),
    RESERVED1(15);

    private final int type;

    private MessageType(int type) {
        this.type = type;
    }

    protected int type() {
        return type;
    }

    public static MessageType valueOf(int val) {
        for (MessageType msgType : MessageType.values()) {
            if (msgType.type == val) {
                return msgType;
            }
        }
        throw new IllegalArgumentException();
    }
}