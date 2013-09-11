package net.aspenmq.transport.protocol

import net.aspenmq.transport.frame.{SMessageType, SQoS}

class PublishRel(val messageId: Int) extends AckProtocolMessage {
  def messageType = SMessageType.PUBREL
  def qos = SQoS.QOS_ATLEAST_ONCE
}