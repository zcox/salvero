package org.salvero.core

import org.zeromq.ZMQ
import grizzled.slf4j.Logging

trait ZmqContext {
  val context: ZMQ.Context = ZMQ.context(1) //TODO parameter instead of 1?
}

object SocketType extends Enumeration {
  type SocketType = Value
  val PAIR, PUB, SUB, REQ, REP, XREQ, XREP, PULL, PUSH = Value
}

trait ZmqSocket extends ZmqContext {
  this: Logging => 

  def socketType: Int
  def socketTypeObj = SocketType(socketType)
  def endpoint: String
  def open(s: ZMQ.Socket) {
    throw new UnsupportedOperationException("You probably need to mixin either the Bind or Connect trait")
  }

  val socket: ZMQ.Socket = {
    val s = context.socket(socketType)
    open(s)
    config(s)
    s
  }

  def config(s: ZMQ.Socket) {}

  def close() {
    socket.close()
    context.term()
    debug("Closed " + socketTypeObj + " socket at " + endpoint)
  }
}

trait Connect {
  this: ZmqSocket with Logging =>

  override def open(s: ZMQ.Socket) {
    s.connect(endpoint)
    debug("Connected to " + socketTypeObj + " socket at " + endpoint)
  }
}

trait Bind {
  this: ZmqSocket with Logging =>

  override def open(s: ZMQ.Socket) {
    s.bind(endpoint)
    debug("Bound to " + socketTypeObj + " socket at " + endpoint)
  }
}
