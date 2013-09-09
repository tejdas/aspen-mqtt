package net.aspenmq.transport.protocol

import net.aspenmq.transport.frame.SFrameHeader
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

trait VariableHeaderEncoder {
  protected def encode(frameHeader: SFrameHeader, protocolBody: ByteBuf): ByteBuf = {
    val headerBuf = new Array[Byte](SFrameHeader.FIXED_HEADER_MAX_LENGTH)
    val headerLength = frameHeader.marshalHeader(headerBuf)
    val protocolBuf = Unpooled.buffer(headerLength + protocolBody.readableBytes())
    protocolBuf.writeBytes(headerBuf, 0, headerLength)
    protocolBuf.writeBytes(protocolBody, protocolBody.readableBytes())
    protocolBuf
  }
}