package KMIP

case class KeyBlock(
                     val keyFormatType : KeyFormatTypeEnum, // Todo Need to be an enumeration
                     val keyValue : Option[KeyValue], // Not required
                     val cryptographicLength : CryptographicLength // required but MAY be omitted only if this information is available from the Key Value
                     /*...*/
                   ) extends StructuredTag


sealed abstract class KeyFormatTypeEnum extends EnumeratedTag
object KeyFormatTypeEnum {
  case object Raw extends KeyFormatTypeEnum
  case object X509 extends KeyFormatTypeEnum
  case object PKCS8 extends KeyFormatTypeEnum
}