package net.aspenmq.transport.protocol

import scala.collection.mutable.ListBuffer

import io.netty.buffer.{ByteBuf, ByteBufInputStream, ByteBufOutputStream, Unpooled}
import net.aspenmq.transport.frame.{SFrameHeader, SMessageType, SQoS}

object SubscribeAck {
  def decode(buf: ByteBuf): SubscribeAck = {
    val bis = new ByteBufInputStream(buf)
    try {
      val messageId = bis.readShort()
      val sub = new SubscribeAck(messageId)
      while (buf.isReadable()) {
        val qos = bis.readByte()
        sub.addGrantedQoS(SQoS.apply(qos))
      }
      sub
    } finally {
      bis.close()
    }
  }
}

class SubscribeAck(val messsageId: Int) extends ProtocolMessage with VariableHeaderEncoder {
  val grantedQoS = ListBuffer[SQoS.Value]()

  def addGrantedQoS(qos: SQoS.Value):Unit = {
    grantedQoS += qos
  }

  def getGrantedQoS() = grantedQoS.toList

  def encode(): ByteBuf = {
    val buf = Unpooled.buffer(256)
    val bos = new ByteBufOutputStream(buf)
    try {
      bos.writeShort(this.messsageId)
      grantedQoS.foreach(qos => bos.writeByte(qos.id))
      bos.flush()
    } finally {
      bos.close()
    }
    val frameHeader = new SFrameHeader(false, SQoS.QOS_RESERVED, false, SMessageType.SUBACK, buf.readableBytes())
    super.encode(frameHeader, buf)
  }
}