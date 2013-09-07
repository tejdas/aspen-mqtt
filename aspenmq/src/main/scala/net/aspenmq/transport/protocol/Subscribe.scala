package net.aspenmq.transport.protocol

import io.netty.buffer.ByteBuf
import scala.collection.mutable.ListBuffer
import net.aspenmq.transport.frame.SQoS
import io.netty.buffer.Unpooled
import io.netty.buffer.ByteBufOutputStream
import net.aspenmq.transport.frame.SFrameHeader
import net.aspenmq.transport.frame.SMessageType
import io.netty.buffer.ByteBufInputStream

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
class Subscribe(val messsageId: Int) extends ProtocolMessage {
  val topics = ListBuffer[(SQoS.Value, String)]()

  def addTopic(qos: SQoS.Value, topicName: String) = topics += ((qos, topicName))

  override def encode(): ByteBuf = {
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
    val frameHeader = new SFrameHeader(false, SQoS.QOS_RESERVED, false, SMessageType.SUBSCRIBE, buf.readableBytes())
    val headerBuf = new Array[Byte](SFrameHeader.FIXED_HEADER_MAX_LENGTH)
    val headerLength = frameHeader.marshalHeader(headerBuf)
    val subBuf = Unpooled.buffer(headerLength + buf.readableBytes())
    subBuf.writeBytes(headerBuf, 0, headerLength)
    subBuf.writeBytes(buf, buf.readableBytes())
    subBuf
  }
}