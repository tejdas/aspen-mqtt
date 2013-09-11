package net.aspenmq.transport.protocol

import java.util.UUID

import org.apache.commons.lang.StringUtils

import io.netty.buffer.{ByteBuf, ByteBufInputStream, ByteBufOutputStream, Unpooled}
import net.aspenmq.transport.frame.{SFrameHeader, SMessageType, SQoS}

object Connect {
  val MQTT_PROTOCOL_NAME = "MQIsdp"
  val MQTT_PROTOCOL_VERSION = 0x03

  def decode(buf: ByteBuf): Connect = {
    val connect = new Connect()
    val bis = new ByteBufInputStream(buf)
    try {
      val protocolName = bis.readUTF()
      val version = bis.readByte()
      val flag = bis.readByte()
      connect.isCleanSession = ((flag & 0x02) == 0x02)
      connect.willFlag = ((flag & 0x04) == 0x04)
      if (connect.willFlag) {
        connect.willQoS = SQoS.apply(((flag >> 3) & 0x03))
        connect.willRetain = ((flag & 0x20) == 0x20)
      }

      val isPwdProvided = ((flag & 0x40) == 0x40)
      val isUserProvided = ((flag & 0x80) == 0x80)

      connect.keepAliveDuration = bis.readInt()
      connect.clientId = bis.readUTF()
      if (connect.willFlag) {
        connect.willTopic = bis.readUTF()
        connect.willMessage = bis.readUTF()
      }
      if (isUserProvided) {
        connect.userName = bis.readUTF()
      }
      if (isPwdProvided) {
        connect.password = bis.readUTF()
      }
    } finally {
      bis.close()
    }
    connect
  }
}

class Connect extends ProtocolMessage with VariableHeaderEncoder {
  var keepAliveDuration = 0
  var isCleanSession = true
  var willFlag = false;
  var willQoS = SQoS.QOS_RESERVED
  var willRetain = false
  var clientId = StringUtils.EMPTY
  var willTopic = StringUtils.EMPTY
  var willMessage = StringUtils.EMPTY
  var userName = StringUtils.EMPTY
  var password = StringUtils.EMPTY

  def encode(): ByteBuf = {
    val buf = Unpooled.buffer(256)
    val bos = new ByteBufOutputStream(buf)
    try {
      bos.writeUTF(Connect.MQTT_PROTOCOL_NAME)
      bos.writeByte(Connect.MQTT_PROTOCOL_VERSION)

      var flag = 0
      if (isCleanSession) {
        flag |= 0x02
      }
      if (willFlag) {
        flag |= 0x04
        flag |= (willQoS.id << 3)
        if (willRetain) {
          flag |= 0x20
        }
      }
      if (!StringUtils.isEmpty(password)) {
        flag |= 0x40
      }
      if (!StringUtils.isEmpty(userName)) {
        flag |= 0x80
      }
      bos.writeByte(flag)
      bos.writeInt(keepAliveDuration)
      clientId = UUID.randomUUID().toString()
      bos.writeUTF(clientId)
      if (willFlag) {
        bos.writeUTF(willTopic)
        bos.writeUTF(willMessage)
      }
      if (!StringUtils.isEmpty(userName)) {
        bos.writeUTF(userName)
      }
      if (!StringUtils.isEmpty(password)) {
        bos.writeUTF(password)
      }
      bos.flush()
    } finally {
      bos.close()
    }

    val frameHeader = new SFrameHeader(false, SQoS.QOS_ATLEAST_ONCE, false, SMessageType.CONNECT, buf.readableBytes())
    super.encode(frameHeader, buf)
  }
}
