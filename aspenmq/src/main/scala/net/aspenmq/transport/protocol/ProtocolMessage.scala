package net.aspenmq.transport.protocol

import io.netty.buffer.ByteBuf

abstract class ProtocolMessage {
  def encode(): ByteBuf
}