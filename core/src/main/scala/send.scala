package org.salvero.core

import org.zeromq.ZMQ
import grizzled.slf4j.Logging

trait SendMultipart {
  this: ZmqSocket =>

  protected def send(message: TwoPartMessage) {
    socket.send(message._1.getBytes, ZMQ.SNDMORE)
    socket.send(message._2, 0)
  }
}

trait FilterableSendMultipart {
  this: ZmqSocket =>

  protected def send(message: FilterableMessage) {
    socket.send(message._1.getBytes, ZMQ.SNDMORE)
    socket.send(message._2.getBytes, ZMQ.SNDMORE)
    socket.send(message._3, 0)
  }
}

trait Send {
  def ![A <: CaseClass: Manifest](msg: A): Unit
}

trait FilterableSend {
  def ![A <: CaseClass: Manifest](t: (String, A)): Unit
}

trait SalveroSend extends Send with SendMultipart with Serialize {
  this: ZmqSocket with Logging =>

  def ![A <: CaseClass: Manifest](msg: A) {
    send(serialize(msg))
    debug("Sent " + msg)
  }
}

trait FilterableSalveroSend
  extends FilterableSend with FilterableSendMultipart with Serialize {
  this: ZmqSocket with Logging =>

  def ![A <: CaseClass: Manifest](t: (String, A)) {
    send(serialize(t._1, t._2))
    debug("Sent " + t)
  }
}

abstract class AbstractPublish(val endpoint: String)
  extends ZmqSocket with Bind with Logging {
  lazy val socketType = ZMQ.PUB
}

class Publish(endpoint: String)
  extends AbstractPublish(endpoint) with SalveroSend

class FilterablePublish(endpoint: String)
  extends AbstractPublish(endpoint) with FilterableSalveroSend

/** Mixin either the Bind or Connect trait when using Push. */
class Push(val endpoint: String) extends ZmqSocket with SalveroSend with Logging {
  lazy val socketType = ZMQ.PUSH
}
