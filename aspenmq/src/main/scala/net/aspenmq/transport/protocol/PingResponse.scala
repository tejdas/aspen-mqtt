package net.aspenmq.transport.protocol

import net.aspenmq.transport.frame.SMessageType

class PingResponse extends FixedHeaderProtocolMessage {
  override def messageType = SMessageType.PINGRESP
}