package org.salvero.core

import com.banno.salat.avro._
import global._
import java.io.ByteArrayOutputStream
import org.apache.avro.io.DecoderFactory
import org.apache.avro.io.EncoderFactory

trait Serialize {
  def serialize[A <: CaseClass: Manifest](message: A): TwoPartMessage = {
    (message.getClass.getName, asByteArray(message))
  }

  def serialize[A <: CaseClass: Manifest](key: String, message: A): FilterableMessage = {
    val (className, bytes) = serialize(message)
    (key, className, bytes)
  }

  private def asByteArray[A <: CaseClass: Manifest](message: A): Array[Byte] = {
    val baos = new ByteArrayOutputStream
    val binaryEncoder = EncoderFactory.get.binaryEncoder(baos, null)
    grater[A].serialize(message, binaryEncoder)
    baos.toByteArray()
  }
}

trait Deserialize {
  def deserialize(message: TwoPartMessage): CaseClass = {
    val (className, bytes) = message
    val grater = ctx.lookup_!(className).asInstanceOf[AvroGrater[_]]
    val binaryDecoder = DecoderFactory.get.binaryDecoder(bytes, null)

    grater.asObject(binaryDecoder).asInstanceOf[CaseClass]
  }

  def deserialize(message: FilterableMessage): CaseClass = {
    val (_, className, bytes) = message
    deserialize((className, bytes))
  }
}
