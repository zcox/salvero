package org.salvero.core

import org.zeromq.ZMQ
import grizzled.slf4j.Logging

class MultipartSocket(socket: ZMQ.Socket) {
  def recvMore(flags: Int) = if (socket.hasReceiveMore)
    socket.recv(flags)
  else
    throw new RuntimeException("Expected more")
}

object MultipartSocket {
  implicit def wrap(s: ZMQ.Socket) = new MultipartSocket(s)
}

import MultipartSocket.wrap

trait BlockingRead {
  this: ZmqSocket =>

  def read(): TwoPartMessage = {
    val className = new String(socket.recv(0))
    val msg = socket.recvMore(0)

    (className, msg)
  }
}

trait FilterableBlockingRead {
  this: ZmqSocket =>

  def read(): FilterableMessage = {
    val key = new String(socket.recv(0))
    val className = new String(socket.recvMore(0))
    val msg = socket.recvMore(0)

    (key, className, msg)
  }
}

trait NonBlockingRead {
  this: ZmqSocket =>

  def read(): Option[TwoPartMessage] =
    Option(socket.recv(ZMQ.NOBLOCK)) map { bs =>
      val className = new String(bs)
      val msg = socket.recvMore(0)
      (className, msg)
    }
}

trait FilterableNonBlockingRead {
  this: ZmqSocket =>

  def read(): Option[FilterableMessage] =
    Option(socket.recv(ZMQ.NOBLOCK)) map { bs =>
      val key = new String(bs)
      val className = new String(socket.recvMore(0))
      val msg = socket.recvMore(0)
      (key, className, msg)
    }
}

trait Run extends Runnable {
  this: Logging => 

  @volatile
  private var running = false
  def stop() {
    running = false
  }

  def run() {
    begin()
    running = true
    debug("Running...")
    while (running) {
      step()
    }
    debug("Stopping...")
    end()
  }

  def begin() {}
  def step() {}
  def end() {}
}

trait ZmqRun extends Run {
  this: ZmqSocket with Logging =>

  def handler: Send
  def readMsg(): CaseClass
  override def step() {
    handler ! readMsg()
  }
  override def end() {
    close()
  }
}

trait Receive
  extends ZmqRun with ZmqSocket with BlockingRead with Deserialize with Logging {
  def readMsg() = {
    val msg = deserialize(read())
    debug("Received " + msg)
    msg
  }
}

trait FilterableReceive
  extends ZmqRun with ZmqSocket with FilterableBlockingRead with Deserialize with Logging {
  def readMsg() = {
    val msg = deserialize(read())
    debug("Received " + msg)
    msg
  }
}

/** Mixin either the Bind or Connect trait when using Pull. */
class Pull(val endpoint: String, val handler: Send) extends Receive {
  lazy val socketType = ZMQ.PULL
}

abstract class AbstractSubscribe(val endpoint: String, val handler: Send, keys: Set[String] = Set(""))
  extends ZmqSocket with Connect with Logging {
  lazy val socketType = ZMQ.SUB
  override def config(s: ZMQ.Socket) {
    for (key <- keys) s.subscribe(key.getBytes)
    debug("Filtering on " + keys)
  }
}

class Subscribe(endpoint: String, handler: Send)
  extends AbstractSubscribe(endpoint, handler) with Receive

class FilterableSubscribe(endpoint: String, handler: Send, keys: Set[String] = Set(""))
  extends AbstractSubscribe(endpoint, handler, keys) with FilterableReceive
