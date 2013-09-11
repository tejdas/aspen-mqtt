package net.aspenmq.transport.protocol

import net.aspenmq.transport.frame.{SMessageType, SQoS}

class PublishAck(val messageId: Int) extends AckProtocolMessage {
  def messageType = SMessageType.PUBACK
  def qos = SQoS.QOS_RESERVED
}