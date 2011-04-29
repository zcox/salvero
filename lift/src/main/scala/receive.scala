package org.salvero.lift

import org.zeromq.ZMQ
import net.liftweb.actor.LiftActor
import org.salvero.core.{ ZmqSocket, CaseClass, NonBlockingRead, FilterableNonBlockingRead, Deserialize, Connect }
import grizzled.slf4j.Logging

sealed trait Message
case object Start
case object Receive
case object Stop

trait Run extends LiftActor {
  this: Logging =>

  private var running = false

  override def messageHandler = {
    case Start =>
      running = true
      debug("Running...")
      begin()
      this ! Receive
    case Receive => if (running) {
      step()
      this ! Receive
    }
    case Stop =>
      debug("Stopping...")
      running = false
      end()
  }

  protected def begin() {}
  protected def step() {}
  protected def end() {}
}

trait ZmqRun extends Run {
  this: ZmqSocket with Logging =>

  def handler: LiftActor
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
class Pull(val endpoint: String, val handler: LiftActor) extends Receive {
  lazy val socketType = ZMQ.PULL
}

abstract class AbstractSubscribe(val endpoint: String, val handler: LiftActor, keys: Set[String] = Set(""))
  extends ZmqSocket with Connect with Logging {
  lazy val socketType = ZMQ.SUB
  override def config(s: ZMQ.Socket) {
    for (key <- keys) s.subscribe(key.getBytes)
    debug("Filtering on " + keys)
  }
}

class Subscribe(endpoint: String, handler: LiftActor)
  extends AbstractSubscribe(endpoint, handler) with Receive

class FilterableSubscribe(endpoint: String, handler: LiftActor, keys: Set[String] = Set(""))
  extends AbstractSubscribe(endpoint, handler, keys) with FilterableReceive
