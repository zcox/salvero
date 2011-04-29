package org.salvero.lift

import org.specs2.mutable._
import org.salvero.core.{TestMessage, Publish}
import net.liftweb.actor.LiftActor

class LiftPublishSubscribeSpec extends Specification {
  "Publish & Subscribe" should {
    "send & receive messages" in {
      val messages = (for (i <- 1 to 3) yield TestMessage(String.valueOf(i))).toList
      val handler = new LiftActor {
	var messages: List[TestMessage] = Nil
	override def messageHandler = {
	  case tm: TestMessage => messages = messages :+ tm
	}
      }

      val publish = new Publish("tcp://*:5556")
      val subscribe = new Subscribe("tcp://localhost:5556", handler)
      subscribe ! Start

      for (m <- messages) publish ! m
      Thread.sleep(1000)
      subscribe ! Stop
      publish.close()

      messages must_== handler.messages
    }
  }
}
