package net.aspenmq.transport.connection

import net.aspenmq.transport.frame.SFrameHeader
import net.aspenmq.transport.protocol.ProtocolMessage

class AMQMessage(val frameHeader: SFrameHeader, val protocolMessage: ProtocolMessage)