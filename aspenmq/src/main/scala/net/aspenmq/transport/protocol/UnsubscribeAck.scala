package net.aspenmq.transport.protocol

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.buffer.ByteBufOutputStream
import net.aspenmq.transport.frame.SFrameHeader
import net.aspenmq.transport.frame.SQoS
import net.aspenmq.transport.frame.SMessageType
import io.netty.buffer.ByteBufInputStream

object UnsubscribeAck {
  def decode(buf: ByteBuf): UnsubscribeAck = {
    val bis = new ByteBufInputStream(buf)
    try {
      val messageId = bis.readShort()
      val sub = new UnsubscribeAck(messageId)
      sub
    } finally {
      bis.close()
    }
  }
}

class UnsubscribeAck(val messsageId: Int) extends ProtocolMessage with VariableHeaderEncoder {
  def encode(): ByteBuf = {
    val buf = Unpooled.buffer(2)
    val bos = new ByteBufOutputStream(buf)
    try {
      bos.writeShort(this.messsageId)
      bos.flush()
    } finally {
      bos.close()
    }
    val frameHeader = new SFrameHeader(false, SQoS.QOS_RESERVED, false, SMessageType.UNSUBACK, buf.readableBytes())
    super.encode(frameHeader, buf)
  }
}