package net.aspenmq.transport.protocol

import net.aspenmq.transport.frame.{SMessageType, SQoS}

class PublishRec(val messageId: Int) extends AckProtocolMessage {
  def messageType = SMessageType.PUBREC
  def qos = SQoS.QOS_RESERVED
}