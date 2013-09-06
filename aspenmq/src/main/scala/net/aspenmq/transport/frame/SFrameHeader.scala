package net.aspenmq.transport.frame

import java.util.Arrays
import io.netty.buffer.ByteBuf

object SQoS extends Enumeration {
  val QOS_ATMOST_ONCE = Value(0)
  val QOS_ATLEAST_ONCE = Value(1)
  val QOS_EXACTLY_ONCE = Value(2)
  val QOS_RESERVED = Value(4)
}

object SMessageType extends Enumeration {
  val RESERVED = Value(0)
  val CONNECT = Value(1)
  val CONNACK = Value(2)
  val PUBLISH = Value(3)
  val PUBACK = Value(4)
  val PUBREC = Value(5)
  val PUBREL = Value(6)
  val PUBCOMP = Value(7)
  val SUBSCRIBE = Value(8)
  val SUBACK = Value(9)
  val UNSUBSCRIBE = Value(10)
  val UNSUBACK = Value(11)
  val PINGREQ = Value(12)
  val PINGRESP = Value(13)
  val DISCONNECT = Value(14)
  val RESERVED1 = Value(15)
}

object SFrameHeader {
  val FIXED_HEADER_MIN_LENGTH = 2
  val FIXED_HEADER_MAX_LENGTH = 5

  def parseHeader(headerBuf: ByteBuf): SFrameHeader = {
    if (headerBuf.readableBytes() < FIXED_HEADER_MIN_LENGTH) {
      null
    }

    headerBuf.markReaderIndex()
    var currentByte = headerBuf.readByte()
    val retain = ((currentByte & 0x01) == 1)
    val qosVal = (currentByte >> 1) & 0x03
    val isDuplicate = (((currentByte >> 3) & 0x01) == 1)
    val msgType = (currentByte >> 4) & 0x0F

    var messageLength = 0 // TODO
    var multiplier = 1
    var pos = 0
    do {
      pos += 1
      if (pos == FIXED_HEADER_MAX_LENGTH) {
        throw new RuntimeException("message length not allowed")
      }
      if (!headerBuf.isReadable()) {
        /*
         * Reached end of buffer before reading the complete frame header.
         */
        headerBuf.resetReaderIndex()
        return null
      }
      currentByte = headerBuf.readByte()
      messageLength += (currentByte & 0x7F) * multiplier
      multiplier *= 128
    } while ((currentByte & 0x80) != 0)

    new SFrameHeader(retain,
      SQoS.apply(qosVal),
      isDuplicate,
      SMessageType.apply(msgType),
      messageLength)
  }
}

class SFrameHeader(val retain: Boolean, val qos: SQoS.Value, val duplicate: Boolean,
    val messageType: SMessageType.Value, val messageLength: Int) {

  def marshalHeader(header: Array[Byte]): Int = {
    var pos = 0

    var flag = 0
    if (retain) {
      flag |= 0x01
    }
    flag |= (qos.id << 1)
    if (duplicate) {
      flag |= 0x08
    }

    flag |= (messageType.id << 4)
    header(pos) = flag.toByte

    var msgLen = messageLength
    do {
      pos += 1
      header(pos) = (msgLen % 128 & 0xFF).toByte
      msgLen /= 128
      if (msgLen > 0) {
        header(pos) = (header(pos) | 0x80).toByte
      }
    } while ((msgLen > 0) && (pos < (SFrameHeader.FIXED_HEADER_MAX_LENGTH - 1)))

    if (msgLen > 0) {
      throw new IllegalArgumentException("message length not allowed: " + messageLength)
    }
    return pos + 1
  }
}
