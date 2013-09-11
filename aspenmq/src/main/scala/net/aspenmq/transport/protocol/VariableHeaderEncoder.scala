package net.aspenmq.transport.protocol

import io.netty.buffer.{ByteBuf, Unpooled}
import net.aspenmq.transport.frame.SFrameHeader

trait VariableHeaderEncoder {
  protected def encode(frameHeader: SFrameHeader, protocolBody: ByteBuf): ByteBuf = {
    val headerBuf2 = new Array[Byte](SFrameHeader.FIXED_HEADER_MAX_LENGTH)
    val headerBuf = Array.fill[Byte](SFrameHeader.FIXED_HEADER_MAX_LENGTH)(0)
    val headerLength = frameHeader.marshalHeader(headerBuf)
    val protocolBuf = Unpooled.buffer(headerLength + protocolBody.readableBytes())
    protocolBuf.writeBytes(headerBuf, 0, headerLength)
    protocolBuf.writeBytes(protocolBody, protocolBody.readableBytes())
    protocolBuf
  }
}