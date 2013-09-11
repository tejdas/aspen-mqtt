package net.aspenmq.transport.protocol

import io.netty.buffer.{ByteBuf, Unpooled}
import net.aspenmq.transport.frame.{SFrameHeader, SMessageType, SQoS}

trait FixedHeaderProtocolMessage extends ProtocolMessage {
  def messageType: SMessageType.Value

  def encode(): ByteBuf = {
    val frameHeader = new SFrameHeader(false, SQoS.QOS_RESERVED, false, messageType, 0)
    val headerBuf = new Array[Byte](SFrameHeader.FIXED_HEADER_MIN_LENGTH)
    val headerLength = frameHeader.marshalHeader(headerBuf)
    Unpooled.copiedBuffer(headerBuf)
  }
}