package net.aspenmq.transport.connection

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.{Channel, ChannelOption}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel

object AMQListener {
  val listenAddress = "0.0.0.0"
  var channelInitializerFactory = new AMQChannelInitializerFactory()
}

class AMQListener {
  var hasShutdown = false
  var serverChannel : Channel = null
  val bossGroup = new NioEventLoopGroup()
  val workerGroup = new NioEventLoopGroup()

  def start(): Unit = {
    println("AspenMQ Listener on port: " + AMQConnectionConstants.MQTT_PORT)

    val b = new ServerBootstrap()
    b.group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
    b.option[Integer](ChannelOption.SO_BACKLOG, int2Integer(100))
    b.childHandler(AMQListener.channelInitializerFactory.createChannelInitializer())

    val f = b.bind(AMQListener.listenAddress, AMQConnectionConstants.MQTT_PORT)
    f.sync()
    serverChannel = f.channel()
  }

  def shutdown(): Unit = {
    if (hasShutdown) {
      return
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