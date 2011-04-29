package org.salvero.core

import org.zeromq.ZMQ

trait ZmqContext {
  val context: ZMQ.Context = ZMQ.context(1) //TODO parameter instead of 1?
}

trait ZmqSocket extends ZmqContext {
  def socketType: Int
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
  }
}

trait Connect {
  this: ZmqSocket =>

  override def open(s: ZMQ.Socket) {
    s.connect(endpoint)
  }
}

trait Bind {
  this: ZmqSocket =>

  override def open(s: ZMQ.Socket) {
    s.bind(endpoint)
  }
}
