package net.aspenmq.transport.connection

import net.aspenmq.transport.protocol.ProtocolMessage
import net.aspenmq.transport.frame.SFrameHeader

class AMQMessage(val frameHeader: SFrameHeader, val protocolMessage: ProtocolMessage)