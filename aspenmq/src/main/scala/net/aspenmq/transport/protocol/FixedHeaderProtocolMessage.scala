package net.aspenmq.transport.protocol

import io.netty.buffer.{ByteBuf, Unpooled}
import net.aspenmq.transport.frame.SFrameHeader
import net.aspenmq.transport.frame.SQoS
import net.aspenmq.transport.frame.SMessageType

trait FixedHeaderProtocolMessage extends ProtocolMessage {
  def messageType: SMessageType.Value

  override def encode(): ByteBuf = {
    val frameHeader = new SFrameHeader(false, SQoS.QOS_RESERVED, false, messageType, 2)
    val headerBuf = new Array[Byte](SFrameHeader.FIXED_HEADER_MIN_LENGTH)
    val headerLength = frameHeader.marshalHeader(headerBuf)
    Unpooled.copiedBuffer(headerBuf)
  }
}