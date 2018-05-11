package KMIP

case class KeyValue(val value: ByteStringValue) extends Tag[ByteStringValue]
case class CryptographicLength(val value: IntegerValue) extends Tag[IntegerValue]
