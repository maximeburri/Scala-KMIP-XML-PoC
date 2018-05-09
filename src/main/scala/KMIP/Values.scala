package KMIP

case class KeyFormatType(val value: StringValue) extends Tag[StringValue]
case class KeyValue(val value: ByteStringValue) extends Tag[ByteStringValue]
case class CryptographicLength(val value: IntegerValue) extends Tag[IntegerValue]
