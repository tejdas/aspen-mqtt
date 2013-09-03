package net.aspenmq.transport.protocol

import io.netty.buffer.ByteBuf
import net.aspenmq.transport.frame.{FrameHeader, MessageType}
import io.netty.buffer.Unpooled

class Disconnect extends FixedHeaderProtocolMessage {
  override def messageType = MessageType.DISCONNECT
}
