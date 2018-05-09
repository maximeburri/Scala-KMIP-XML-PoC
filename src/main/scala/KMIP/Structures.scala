package KMIP

case class KeyBlock(
                     val keyFormatType : KeyFormatType, // Todo Need to be an enumeration
                     val keyValue : Option[KeyValue], // Not required
                     val cryptographicLength : CryptographicLength // required but MAY be omitted only if this information is available from the Key Value
                     /*...*/
                   ) extends StructuredTag
