package net.aspenmq.transport.protocol

import io.netty.buffer.ByteBuf
import net.aspenmq.transport.frame.SMessageType

class PingRequest extends FixedHeaderProtocolMessage {
  override def messageType = SMessageType.PINGREQ
}
