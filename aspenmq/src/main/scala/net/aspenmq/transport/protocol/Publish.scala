package net.aspenmq.transport.protocol

import io.netty.buffer.{ByteBuf, ByteBufInputStream, ByteBufOutputStream, Unpooled}
import net.aspenmq.transport.frame.{SFrameHeader, SMessageType, SQoS}

object Publish {
    def decode(buf: ByteBuf): Publish = {
    val bis = new ByteBufInputStream(buf)
    try {
      val topic = bis.readUTF()
      val messageId = bis.readShort()
      val payload = new Array[Byte](buf.readableBytes())
      buf.readBytes(payload)
      new Publish(messageId, topic, payload)
    } finally {
      bis.close()
    }
  }
}

class Publish(val messsageId: Int, val topic: String, val payload: Array[Byte])
  extends ProtocolMessage {

  def encode(qos:SQoS.Value, duplicate:Boolean, retain:Boolean): ByteBuf = {
    val buf = Unpooled.buffer(256)
    val bos = new ByteBufOutputStream(buf)
    try {
      bos.writeUTF(topic)
      bos.writeShort(this.messsageId)
      bos.flush()
    } finally {
      bos.close()
    }

    val headerBuf = new Array[Byte](SFrameHeader.FIXED_HEADER_MAX_LENGTH)
    val frameHeader = new SFrameHeader(retain, qos, duplicate, SMessageType.PUBLISH, buf.readableBytes() + payload.length)
    val headerLength = frameHeader.marshalHeader(headerBuf)
    val protocolBuf = Unpooled.buffer(headerLength + buf.readableBytes())
    protocolBuf.writeBytes(headerBuf, 0, headerLength)
    protocolBuf.writeBytes(buf, buf.readableBytes())

    Unpooled.wrappedBuffer(protocolBuf, Unpooled.wrappedBuffer(payload))
  }
}