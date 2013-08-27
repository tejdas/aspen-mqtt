package net.aspenmq.transport.connection

import io.netty.channel.nio.NioEventLoopGroup
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.ChannelOption
import io.netty.channel.Channel

object SAspenMQListener {
  val listenAddress = "0.0.0.0"
}
class SAspenMQListener {
  val port = 1883
  var hasShutdown = false
  var serverChannel : Channel = null
  val bossGroup = new NioEventLoopGroup()
  val workerGroup = new NioEventLoopGroup()

  def start(listenPort: Int): Unit = {
    println("AspenMQ Listener on port: " + listenPort)

    val b = new ServerBootstrap()
    b.group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
    b.option[Integer](ChannelOption.SO_BACKLOG, int2Integer(100))

    val f = b.bind(SAspenMQListener.listenAddress, port)
    f.sync()
    serverChannel = f.channel()
  }

  def shutdown(): Unit = {
    if (hasShutdown) {
      return ;
    }
    hasShutdown = true
    try {
      if (serverChannel != null) {
        serverChannel.close().sync()
        serverChannel = null
      }
    } finally {
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
    println("AspenMQ Listener shut down")
  }
}