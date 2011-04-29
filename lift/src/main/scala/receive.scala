package org.salvero.lift

import org.zeromq.ZMQ
import net.liftweb.actor.LiftActor
import org.salvero.core.{ ZmqSocket, CaseClass, NonBlockingRead, FilterableNonBlockingRead, Deserialize, Connect }

sealed trait Message
case object Start
case object Receive
case object Stop

trait Run extends LiftActor {
  private var running = false

  override def messageHandler = {
    case Start =>
      running = true
      begin()
      this ! Receive
    case Receive => if (running) {
      step()
      this ! Receive
    }
    case Stop =>
      running = false
      end()
  }

  protected def begin() {}
  protected def step() {}
  protected def end() {}
}

trait ZmqRun extends Run {
  this: ZmqSocket =>

  def handler: LiftActor
  def readMsg(): Option[CaseClass]
  override def step() {
    readMsg() match {
      case Some(msg) => handler ! msg
      case _ => Thread.sleep(1) //prevents runaway thread
    }
  }
  override def end() {
    close()
  }
}

trait Receive
  extends ZmqRun with ZmqSocket with NonBlockingRead with Deserialize {
  def readMsg() = read() map { deserialize(_) }
}

trait FilterableReceive
  extends ZmqRun with ZmqSocket with FilterableNonBlockingRead with Deserialize {
  def readMsg() = read() map { deserialize(_) }
}

/** Mixin either the Bind or Connect trait when using Pull. */
class Pull(val endpoint: String, val handler: LiftActor) extends Receive {
  lazy val socketType = ZMQ.PULL
}

abstract class AbstractSubscribe(val endpoint: String, val handler: LiftActor, keys: Set[String] = Set(""))
  extends ZmqSocket with Connect {
  lazy val socketType = ZMQ.SUB
  override def config(s: ZMQ.Socket) {
    for (key <- keys) s.subscribe(key.getBytes)
  }
}

class Subscribe(endpoint: String, handler: LiftActor)
  extends AbstractSubscribe(endpoint, handler) with Receive

class FilterableSubscribe(endpoint: String, handler: LiftActor, keys: Set[String] = Set(""))
  extends AbstractSubscribe(endpoint, handler, keys) with FilterableReceive
