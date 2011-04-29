Makes it easy to send [Scala](http://scala-lang.org) case classes over [0MQ](http://zeromq.org) sockets, using [salat-avro](https://github.com/T8Webware/salat-avro) for serialization/deserialization.

# Core

Provides simple wrappers around the basic 0MQ socket types:

 - Push & Pull
 - Publish & Subscribe (filtered & unfiltered)
 - Request & Reply (coming soon)
 


# Akka

Provides simple Akka actors for basic 0MQ socket types:

 - Pull
 - Subscribe (filtered & unfiltered)
 - Request & Reply (coming soon)

These actors send the messages they receive to another Akka actor.

# Lift

Provides simple Lift actors for basic 0MQ socket types:

 - Pull
 - Subscribe (filtered & unfiltered)
 - Request & Reply (coming soon)

These actors send the messages they receive to another Lift actor.
