package net.aspenmq.transport.protocol

import io.netty.buffer.{ByteBuf, ByteBufInputStream, ByteBufOutputStream, Unpooled}
import net.aspenmq.transport.frame.{SFrameHeader, SMessageType, SQoS}

object AckProtocolMessage {
  def decode(buf: ByteBuf, messageType: SMessageType.Value): AckProtocolMessage = {
    val bis = new ByteBufInputStream(buf)
    try {
      val messageId = bis.readShort()

      messageType match {
        case SMessageType.UNSUBACK => new UnsubscribeAck(messageId)
        case SMessageType.PUBACK => new PublishAck(messageId)
        case SMessageType.PUBREC => new PublishRec(messageId)
        case SMessageType.PUBREL => new PublishRel(messageId)
        case SMessageType.PUBCOMP => new PublishComp(messageId)
        case _ => null
      }
    } finally {
      bis.close()
    }
  }
}

trait AckProtocolMessage extends ProtocolMessage with VariableHeaderEncoder {
  def messageType: SMessageType.Value
  def qos: SQoS.Value
  def messageId: Int

  def encode(duplicate: Boolean): ByteBuf = {
    val buf = Unpooled.buffer(2)
    val bos = new ByteBufOutputStream(buf)
    try {
      bos.writeShort(messageId)
      bos.flush()
    } finally {
      bos.close()
    }
    val frameHeader = new SFrameHeader(false, qos, duplicate, messageType, buf.readableBytes())
    super.encode(frameHeader, buf)
  }
}