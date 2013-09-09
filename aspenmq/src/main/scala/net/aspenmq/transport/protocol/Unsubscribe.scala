package net.aspenmq.transport.protocol

import scala.collection.mutable.ListBuffer
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.buffer.ByteBufOutputStream
import net.aspenmq.transport.frame.SFrameHeader
import net.aspenmq.transport.frame.SQoS
import net.aspenmq.transport.frame.SMessageType
import io.netty.buffer.ByteBufInputStream

object Unsubscribe {
    def decode(buf: ByteBuf): Unsubscribe = {
    val bis = new ByteBufInputStream(buf)
    try {
      val messageId = bis.readShort()
      val sub = new Unsubscribe(messageId)
      while (buf.isReadable()) {
        val topicName = bis.readUTF()
        sub.addTopic(topicName)
      }
      sub
    } finally {
      bis.close()
    }
  }
}

class Unsubscribe(val messsageId: Int) extends ProtocolMessage with VariableHeaderEncoder {
  val topics = ListBuffer[String]()

  def addTopic(topicName: String):Unit = topics += topicName

  def getTopics() = topics.toList

  def encode(duplicate:Boolean = false): ByteBuf = {
    val buf = Unpooled.buffer(256)
    val bos = new ByteBufOutputStream(buf)
    try {
      bos.writeShort(this.messsageId)
      topics.foreach(topic => bos.writeUTF(topic))
      bos.flush()
    } finally {
      bos.close()
    }
    val frameHeader = new SFrameHeader(false, SQoS.QOS_ATLEAST_ONCE, duplicate, SMessageType.UNSUBSCRIBE, buf.readableBytes())
    super.encode(frameHeader, buf)
  }
}
