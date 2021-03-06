package net.aspenmq.transport.protocol

import net.aspenmq.transport.frame.{SMessageType, SQoS}

class UnsubscribeAck(val messageId: Int) extends AckProtocolMessage {
  def messageType = SMessageType.UNSUBACK
  def qos = SQoS.QOS_RESERVED
}