package org.salvero.akka

import org.zeromq.ZMQ
import akka.actor.{ Actor, ActorRef }
import org.salvero.core.{ ZmqSocket, CaseClass, NonBlockingRead, FilterableNonBlockingRead, Deserialize, Connect }
import grizzled.slf4j.Logging

sealed trait Message
case object Start
case object Receive
case object Stop

trait Run extends Actor {
  this: Logging =>

  private var running = false

  def receive = {
    case Start =>
      running = true
      debug("Running...")
      begin()
      self ! Receive
    case Receive => if (running) {
      step()
      self ! Receive
    }
    case Stop =>
      debug("Stopping...")
      running = false
      end()
      self.stop()
  }

  protected def begin() {}
  protected def step() {}
  protected def end() {}
}

trait ZmqRun extends Run {
  this: ZmqSocket with Logging =>

  def handler: ActorRef
  def readMsg(): Option[CaseClass]
  override def step() {
    readMsg() match {
      case Some(msg) =>
        debug("Received " + msg)
        handler ! msg
      case _ => Thread.sleep(1) //prevents runaway thread
    }
  }
  override def end() {
    close()
  }
}

trait Receive
  extends ZmqRun with ZmqSocket with NonBlockingRead with Deserialize with Logging {
  def readMsg() = read() map { deserialize(_) }
}

trait FilterableReceive
  extends ZmqRun with ZmqSocket with FilterableNonBlockingRead with Deserialize with Logging {
  def readMsg() = read() map { deserialize(_) }
}

/** Mixin either the Bind or Connect trait when using Pull. */
class Pull(val endpoint: String, val handler: ActorRef) extends Receive {
  lazy val socketType = ZMQ.PULL
}

abstract class AbstractSubscribe(val endpoint: String, val handler: ActorRef, keys: Set[String] = Set(""))
  extends ZmqSocket with Connect with Logging {
  lazy val socketType = ZMQ.SUB
  override def config(s: ZMQ.Socket) {
    for (key <- keys) s.subscribe(key.getBytes)
    debug("Filtering on " + keys)
  }
}

class Subscribe(endpoint: String, handler: ActorRef)
  extends AbstractSubscribe(endpoint, handler) with Receive

class FilterableSubscribe(endpoint: String, handler: ActorRef, keys: Set[String] = Set(""))
  extends AbstractSubscribe(endpoint, handler, keys) with FilterableReceive
