package KMIP

import scala.language.implicitConversions

sealed trait Type {}

trait Tag[+A<:Type] {
  val value : A
}

trait Structure extends Type {
  def fields : List[Tag[Type]] = {
    this.asInstanceOf[Product].productIterator.toList
      .filter(a => a != None)
      .map( a =>
        a match {
        case Some(x) => x
        case default => default
      }
    ).asInstanceOf[List[Tag[Type]]]
  }
}

trait StructuredTag extends Tag[Structure] with Structure {
  val value = this
}

trait Enumeration extends Type
trait EnumeratedTag extends Tag[Enumeration] with Enumeration {
  val value = this
}

case class StringValue(val value : String) extends Type
case class IntegerValue(val value : Int) extends Type
case class ByteStringValue(val value : List[Byte]) extends Type
// TODO :
//Integer
//Long Integer
//Big Integer
//Enumeration
//Boolean
//Text String
//Byte String
//Date-Time
//Interval

object TypeImplicits {
  /* Implicit conversion for values, allow "Name" instead of StringValue("Name") */
  implicit def stringToStringValue(value: String): StringValue = StringValue(value)

  implicit def stringValueToString(value: StringValue): String = value.value;

  implicit def intToIntegerValue(value: Int): IntegerValue = IntegerValue(value)

  implicit def integerValueToInt(value: IntegerValue): Int = value.value;

  implicit def byteStringToByteStringValue(value: List[Byte]): ByteStringValue = ByteStringValue(value)

  implicit def byteStringValueToByteString(value: ByteStringValue): List[Byte] = value.value;
}

object TypeUtils {
  def tagToString(a:Tag[Type]) : String = a match {
    case _: KeyBlock => "KeyBlock"
    case _: KeyFormatTypeEnum => "KeyFormatType"
    case _: KeyValue => "KeyValue"
    case _: CryptographicLength => "CryptographicLength"
  }

  // HexBytes tools
  def hex2bytes(hex: String): List[Byte] = {
    hex.sliding(2, 2).map(Integer.parseInt(_, 16).toByte).toList
  }

  def bytes2hex(bytes: List[Byte]): String = {
    bytes.map("%02x".format(_)).mkString
  }
}






