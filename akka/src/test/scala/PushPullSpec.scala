package org.salvero.akka

import org.specs2.mutable._
import org.salvero.core.{ TestMessage, Push, Bind, Connect }
import akka.actor.Actor
import akka.actor.Actor.actorOf
import akka.dispatch.Future

class Handler extends Actor {
  var messages: List[TestMessage] = Nil
  def receive = {
    case tm: TestMessage => messages = messages :+ tm
    case "getMessages" => self reply messages
  }
}

class AkkaPushPullSpec extends Specification {
  "Push & Pull" should {
    "send & receive messages" in {
      val messages = (for (i <- 1 to 3) yield TestMessage(String.valueOf(i))).toList
      val handler = actorOf[Handler].start

      val push = new Push("tcp://*:5555") with Bind
      val pull = actorOf(new Pull("tcp://localhost:5555", handler) with Connect).start
      pull ! Start

      for (m <- messages) push ! m
      Thread.sleep(1000)
      val future: Future[List[TestMessage]] = handler !!! "getMessages"
      future.await
      val messages2 = future.result.get
      pull ! Stop
      push.close()

      messages2 must_== messages
    }
  }
}

