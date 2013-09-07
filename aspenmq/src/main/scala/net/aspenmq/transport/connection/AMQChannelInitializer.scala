package net.aspenmq.transport.connection

import io.netty.channel.{ChannelHandler, ChannelInitializer}
import io.netty.channel.socket.SocketChannel

class AMQChannelInitializerFactory {
  def createChannelInitializer(): AMQChannelInitializer = new AMQChannelInitializer()
}

class AMQChannelInitializer extends ChannelInitializer[SocketChannel] {
  override def initChannel(ch: SocketChannel): Unit = {
    val pipeline = ch.pipeline()
    pipeline.addLast("frameDecoder", new AMQFrameDecoder())
    pipeline.addLast("handler", new AMQConnectionHandler())
  }
}