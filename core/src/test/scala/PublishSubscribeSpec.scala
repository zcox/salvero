package org.salvero.core

import org.specs2.mutable._

class PublishSubscribeSpec extends Specification {
  "Publish & Subscribe" should {
    "send & receive messages" in {
      val messages = (for (i <- 1 to 3) yield TestMessage(String.valueOf(i))).toList
      val handler = new Send {
	var messages: List[TestMessage] = Nil
	def ![A <: CaseClass: Manifest](msg: A) = msg match {
	  case tm: TestMessage => messages = messages :+ tm
	}
      }

      val publish = new Publish("tcp://*:5556")
      val subscribe = new Subscribe("tcp://localhost:5556", handler)
      new Thread(subscribe).start

      for (m <- messages) publish ! m
      Thread.sleep(1000)
      subscribe.stop()
      publish.close()

      messages must_== handler.messages
    }
  }
}
