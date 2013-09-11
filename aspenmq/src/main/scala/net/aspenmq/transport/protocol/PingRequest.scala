package net.aspenmq.transport.protocol

import net.aspenmq.transport.frame.SMessageType

class PingRequest extends FixedHeaderProtocolMessage {
  override def messageType = SMessageType.PINGREQ
}
