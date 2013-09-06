package net.aspenmq.transport.protocol

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.aspenmq.transport.frame.SMessageType

class Disconnect extends FixedHeaderProtocolMessage {
  override def messageType = SMessageType.DISCONNECT
}
