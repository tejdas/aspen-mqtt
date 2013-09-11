package net.aspenmq.transport.protocol

import net.aspenmq.transport.frame.SMessageType

class Disconnect extends FixedHeaderProtocolMessage {
  override def messageType = SMessageType.DISCONNECT
}
