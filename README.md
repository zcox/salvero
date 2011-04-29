Makes it easy to send Scala case classes over 0MQ sockets, using salat-avro for serialization/deserialization.

# Core

Provides simple wrappers around the basic 0MQ socket types:
* Push & Pull
* Publish & Subscribe (filtered & unfiltered)
* Request & Reply (coming soon)

# Akka

Provides simple Akka actors for basic 0MQ socket types:
* Pull
* Subscribe (filtered & unfiltered)
* Request & Reply (coming soon)

These actors send the messages they receive to another Akka actor.

# Lift

Provides simple Lift actors for basic 0MQ socket types:
* Pull
* Subscribe (filtered & unfiltered)
* Request & Reply (coming soon)

These actors send the messages they receive to another Lift actor.
