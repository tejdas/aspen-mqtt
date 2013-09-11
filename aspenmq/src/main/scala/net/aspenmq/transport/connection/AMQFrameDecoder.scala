package net.aspenmq.transport.connection

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import net.aspenmq.transport.protocol.{Connect, ConnectAck, ProtocolMessage}
import net.aspenmq.transport.frame.SFrameHeader
import net.aspenmq.transport.frame.SMessageType
import net.aspenmq.transport.protocol.Subscribe
import net.aspenmq.transport.protocol.Publish
import net.aspenmq.transport.protocol.SubscribeAck
import net.aspenmq.transport.protocol.Unsubscribe
import net.aspenmq.transport.protocol.UnsubscribeAck
import net.aspenmq.transport.protocol.Disconnect
import net.aspenmq.transport.protocol.PingRequest
import net.aspenmq.transport.protocol.PingResponse
import net.aspenmq.transport.protocol.AckProtocolMessage

class AMQFrameDecoder extends ByteToMessageDecoder {
  private var amqMessageInProgress: SFrameHeader = null

  override def decode(ctx: ChannelHandlerContext, buf: ByteBuf, frames: java.util.List[Object]): Unit = {
    if (amqMessageInProgress == null) {
      val frameHeader = SFrameHeader.parseHeader(buf);
      if (frameHeader != null) {
        amqMessageInProgress = frameHeader
      } else {
        println("not enough bytes to parse header")
        return
      }
    }

    if (0 == amqMessageInProgress.messageLength) {
      parseAndDecode(null, frames)
    } else if (buf.readableBytes() >= amqMessageInProgress.messageLength) {
      val readerIndex = buf.readerIndex()
      val protocolData = buf.slice(readerIndex, amqMessageInProgress.messageLength)
      buf.readerIndex(readerIndex + amqMessageInProgress.messageLength)
      parseAndDecode(protocolData, frames)
    } else if (buf.readableBytes() == amqMessageInProgress.messageLength) {
      parseAndDecode(buf, frames)
    } else {
      /*
       * message body not available
       */
    }
  }

  override def decodeLast(ctx: ChannelHandlerContext, in: ByteBuf, out: java.util.List[Object]): Unit = {
  }

  private def parseAndDecode(buf: ByteBuf, frames: java.util.List[Object]): Unit = {
      val body = decodeFrame(amqMessageInProgress.messageType, buf)
      frames.add(new AMQMessage(amqMessageInProgress, body))
      amqMessageInProgress = null
  }

  private def decodeFrame(msgType: SMessageType.Value, buf: ByteBuf): ProtocolMessage = {
    msgType match {
      case SMessageType.CONNECT => Connect.decode(buf)
      case SMessageType.CONNACK => ConnectAck.decode(buf)
      case SMessageType.DISCONNECT => new Disconnect()
      case SMessageType.PINGREQ => new PingRequest()
      case SMessageType.PINGRESP => new PingResponse()
      case SMessageType.SUBSCRIBE => Subscribe.decode(buf)
      case SMessageType.SUBACK => SubscribeAck.decode(buf)
      case SMessageType.UNSUBSCRIBE => Unsubscribe.decode(buf)
      case SMessageType.PUBLISH => Publish.decode(buf)
      case _ => AckProtocolMessage.decode(buf, msgType)
    }
  }
}
