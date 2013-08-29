package net.aspenmq.transport.connection

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import net.aspenmq.transport.frame.{FrameHeader, MessageType}
import net.aspenmq.transport.protocol.{Connect, ConnectAck, ProtocolMessage}

class AMQFrameDecoder extends ByteToMessageDecoder {
  private var amqMessageInProgress: FrameHeader = null

  override def decode(ctx: ChannelHandlerContext, buf: ByteBuf, frames: java.util.List[Object]): Unit = {
    if (amqMessageInProgress == null) {
      val frameHeader = FrameHeader.parseHeader(buf);
      if (frameHeader != null) {
        amqMessageInProgress = frameHeader
      } else {
        println("not enough bytes to parse header")
        return
      }
    }

    if (buf.readableBytes() >= amqMessageInProgress.getMessageLength()) {
      val body = decodeFrame(amqMessageInProgress.getMessageType(), buf)
      frames.add(new AMQMessage(amqMessageInProgress, body))
      amqMessageInProgress = null
    } else {
      println("message body not available")
    }
  }

  override def decodeLast(ctx: ChannelHandlerContext, in: ByteBuf, out: java.util.List[Object]): Unit = {
  }

  private def decodeFrame(msgType: MessageType, buf: ByteBuf): ProtocolMessage = {
    msgType match {
      case MessageType.CONNECT => Connect.decode(buf)
      case MessageType.CONNACK => ConnectAck.decode(buf)
      case _ => null
    }
  }
}
