package org.salvero.akka

import org.specs2.mutable._
import org.salvero.core.{ TestMessage, Publish }
import akka.actor.Actor
import akka.actor.Actor.actorOf
import akka.dispatch.Future

//Handler actor is defined in PushPullSpec.scala

class AkkaPublishSubscribeSpec extends Specification {
  "Publish & Subscribe" should {
    "send & receive messages" in {
      val messages = (for (i <- 1 to 3) yield TestMessage(String.valueOf(i))).toList
      val handler = actorOf[Handler].start

      val publish = new Publish("tcp://*:5556")
      val subscribe = actorOf(new Subscribe("tcp://localhost:5556", handler)).start
      subscribe ! Start

      for (m <- messages) publish ! m
      Thread.sleep(1000)
      val future: Future[List[TestMessage]] = handler !!! "getMessages"
      future.await
      val messages2 = future.result.get
      subscribe ! Stop
      publish.close()

      messages2 must_== messages
    }
  }
}
