package org.salvero.lift

import org.specs2.mutable._
import org.salvero.core.{TestMessage, Push, Bind, Connect}
import net.liftweb.actor.LiftActor

class LiftPushPullSpec extends Specification {
  "Push & Pull" should {
    "send & receive messages" in {
      val messages = (for (i <- 1 to 3) yield TestMessage(String.valueOf(i))).toList
      val handler = new LiftActor {
        var messages: List[TestMessage] = Nil
	override def messageHandler = {
          case tm: TestMessage => messages = messages :+ tm
        }
      }

      val push = new Push("tcp://*:5555") with Bind
      val pull = new Pull("tcp://localhost:5555", handler) with Connect
      pull ! Start

      for (m <- messages) push ! m
      Thread.sleep(1000)
      pull ! Stop
      push.close()

      messages must_== handler.messages
    }
  }
}

