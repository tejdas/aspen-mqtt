package net.aspenmq.transport.protocol

import io.netty.buffer.ByteBuf
import net.aspenmq.transport.frame.MessageType

class PingRequest extends FixedHeaderProtocolMessage {
  override def messageType = MessageType.PINGREQ
}
