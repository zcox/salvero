Makes it easy to send [Scala](http://scala-lang.org) case classes over [0MQ](http://zeromq.org) sockets, using [salat-avro](https://github.com/T8Webware/salat-avro) for serialization/deserialization.

Salvero is opinionated messaging.  Messages are assumed to be Scala case classes.  They are serialized to Avro byte arrays.  Messages are sent over 0MQ.

# Core

Provides simple wrappers around the basic 0MQ socket types:

 - Push & Pull
 - Publish & Subscribe (filtered & unfiltered)
 - Request & Reply (coming soon)

## Push/Pull

    ```scala
    val push = new Push("tcp://*:5555") with Bind
    ```

## Publish/Subscribe

## Request/Reply

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
