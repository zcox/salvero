package org.salvero.core

import org.specs2.mutable._

class PushPullSpec extends Specification {
  "Push & Pull" should {
    "send & receive messages" in {
      val messages = (for (i <- 1 to 3) yield TestMessage(String.valueOf(i))).toList
      val handler = new Send {
        var messages: List[TestMessage] = Nil
        def ![A <: CaseClass: Manifest](msg: A) = msg match {
          case tm: TestMessage => messages = messages :+ tm
        }
      }

      val push = new Push("tcp://*:5555") with Bind
      val pull = new Pull("tcp://localhost:5555", handler) with Connect
      new Thread(pull).start

      for (m <- messages) push ! m
      Thread.sleep(1000)
      pull.stop()
      push.close()

      messages must_== handler.messages
    }
  }
}

