package net.aspenmq.transport.connection

import net.aspenmq.transport.protocol.ProtocolMessage
import net.aspenmq.transport.frame.FrameHeader

class AMQMessage(val frameHeader: FrameHeader, val protocolMessage: ProtocolMessage)