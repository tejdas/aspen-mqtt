package net.aspenmq.transport.protocol

import scala.collection.mutable.ListBuffer

import io.netty.buffer.{ByteBuf, ByteBufInputStream, ByteBufOutputStream, Unpooled}
import net.aspenmq.transport.frame.{SFrameHeader, SMessageType, SQoS}

object Subscribe {
    def decode(buf: ByteBuf): Subscribe = {
    val bis = new ByteBufInputStream(buf)
    try {
      val messageId = bis.readShort()
      val sub = new Subscribe(messageId)
      while (buf.isReadable()) {
        val topicName = bis.readUTF()
        val qos = bis.readByte()
        sub.addTopic(SQoS.apply(qos), topicName)
      }
      sub
    } finally {
      bis.close()
    }
  }
}

class Subscribe(val messsageId: Int) extends ProtocolMessage with VariableHeaderEncoder {
  val topics = ListBuffer[(SQoS.Value, String)]()

  def addTopic(qos: SQoS.Value, topicName: String):Unit = {
    topics += ((qos, topicName))
  }

  def getTopics() = topics.toList

  def encode(duplicate:Boolean): ByteBuf = {
    val buf = Unpooled.buffer(256)
    val bos = new ByteBufOutputStream(buf)
    try {
      bos.writeShort(this.messsageId)
      topics.foreach(topicDetails => {
        bos.writeUTF(topicDetails._2)
        bos.writeByte(topicDetails._1.id)
      })
      bos.flush()
    } finally {
      bos.close()
    }
    val frameHeader = new SFrameHeader(false, SQoS.QOS_ATLEAST_ONCE, duplicate, SMessageType.SUBSCRIBE, buf.readableBytes())
    super.encode(frameHeader, buf)
  }
}