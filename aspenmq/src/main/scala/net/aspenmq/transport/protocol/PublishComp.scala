package net.aspenmq.transport.protocol

import net.aspenmq.transport.frame.{SMessageType, SQoS}

class PublishComp(val messageId: Int) extends AckProtocolMessage {
  def messageType = SMessageType.PUBCOMP
  def qos = SQoS.QOS_RESERVED
}