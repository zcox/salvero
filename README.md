Makes it easy to send [Scala](http://scala-lang.org) case classes over [0MQ](http://zeromq.org) sockets, using [salat-avro](https://github.com/T8Webware/salat-avro) for serialization/deserialization.

Salvero is opinionated messaging.  Messages are assumed to be Scala case classes.  They are serialized to Avro byte arrays.  Messages are sent over 0MQ.

# Core

Provides simple wrappers around the basic 0MQ socket types:

 - Push & Pull
 - Publish & Subscribe (filtered & unfiltered)
 - Request & Reply (coming soon)
 
Sending a message is just the ! operator followed by a case class.

Receiving a message involves a callback.

### Messages

They are Scala case classes, yo.

``` scala
case class Message(text: String)
```

### Push/Pull

The Push class wraps a PUSH socket.  Just new one up and mixin either Bind or Connect, based on what you need.  Then ! messages at it all day long.

``` scala
val push = new Push("tcp://*:5555") with Bind
push ! Message("Avro rulez")
```

The Pull class wraps a PULL socket and implements Runnable.  Just new one up, mixin either Bind or Connect and throw it in a Thread.  It will receive messages and send them to the handler until the cows come home.

Receive handlers in core extend the Send trait and implement the ! method.  

``` scala
//handlers extend the Send trait and the ! method receives the message
val handler = new Send {
  def ![A <: CaseClass: Manifest](msg: A) = msg match {
    case Message(text) => println("Hells yeah: " + text)
  }
}

val pull = new Pull("tcp://localhost:5555", handler) with Connect
new Thread(pull).start

pull.stop() //when the cows have all come home
```

### Publish/Subscribe

The Publish class wraps a PUB socket.  Just new one up and ! the hell out of it.

``` scala
val publish = new Publish("tcp://*:5556")
publish ! Message("Salat rulez")
```

The Subscribe class wraps a SUB socket and implements Runnable.  It does not filter any messages.  It will receive messages and send them to a handler. w00t

``` scala
val handler = //you know what to do
val subscribe = new Subscribe("tcp://localhost:5556", handler)
new Thread(subscribe).start

subscribe.stop() //after w00t ceases
```

0MQ also provides a simple way for SUB sockets to [filter out messages](http://zguide.zeromq.org/page:all#toc43) they don't want.  So does Salvero.  You need to use FilterablePublish, FilterableSubscribe and send messages with keys.

``` scala
val publish = new FilterablePublish("tcp://*:5556")
publish ! ("wack", "node.js rulez")
publish ! ("1337", "0MQ rulez")

val subscribe = new FilterableSubscribe("tcp://localhost:5556", handler, Set("1337"))
new Thread(subscribe).start

subscribe.stop() //sometime later...
```

The subscriber's handler will only receive messages sent with the "1337" key.

### Request/Reply

(coming soon)

# Akka

Provides simple [Akka](http://akka.io) actors for basic 0MQ socket types:

 - Pull
 - Subscribe (filtered & unfiltered)
 - Request & Reply (coming soon)

These actors send the messages they receive to another Akka actor.

# Lift

Provides simple [Lift](http://liftweb.net) actors for basic 0MQ socket types:

 - Pull
 - Subscribe (filtered & unfiltered)
 - Request & Reply (coming soon)

These actors send the messages they receive to another Lift actor.
