package net.aspenmq.transport.protocol

import io.netty.buffer.{ByteBuf, ByteBufInputStream, ByteBufOutputStream, Unpooled}
import net.aspenmq.transport.frame.{SFrameHeader, SMessageType, SQoS}

object ConnectAck {
  val CONNECTION_ACCEPTED = 0x00
  val ERROR_PROTOCOL_VERSION = 0x01
  val ERROR_IDENTIFIER_REJECTED = 0x02
  val ERROR_SERVER_UNAVAILABLE = 0x03
  val ERROR_BAD_CREDENTIALS = 0x04
  val ERROR_UNAUTHORIZED = 0x05

  def decode(buf: ByteBuf): ConnectAck = {
    val bis = new ByteBufInputStream(buf)
    try {
      bis.readByte()
      val returnCode = bis.readByte()
      new ConnectAck(returnCode)
    } finally {
      bis.close()
    }
  }
}

class ConnectAck(val returnCode: Int) extends ProtocolMessage with VariableHeaderEncoder {
  def encode(): ByteBuf = {
    val buf = Unpooled.buffer(2)
    val bos = new ByteBufOutputStream(buf)
    try {
      bos.writeByte(0)
      bos.writeByte(returnCode)
      bos.flush()
    } finally {
      bos.close()
    }
    val frameHeader = new SFrameHeader(false, SQoS.QOS_RESERVED, false, SMessageType.CONNACK, 2)
    super.encode(frameHeader, buf)
  }
}
