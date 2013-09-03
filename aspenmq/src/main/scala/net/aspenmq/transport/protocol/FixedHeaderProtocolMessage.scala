package net.aspenmq.transport.protocol

import io.netty.buffer.{ByteBuf, Unpooled}
import net.aspenmq.transport.frame.{FrameHeader, MessageType}

trait FixedHeaderProtocolMessage extends ProtocolMessage {
  def messageType: MessageType

  override def encode(): ByteBuf = {
    val frameHeader = new FrameHeader(false, FrameHeader.QoS.QOS_RESERVED, false, messageType, 2)
    val headerBuf = new Array[Byte](FrameHeader.FIXED_HEADER_MIN_LENGTH)
    val headerLength = frameHeader.marshalHeader(headerBuf)
    Unpooled.copiedBuffer(headerBuf)
  }
}