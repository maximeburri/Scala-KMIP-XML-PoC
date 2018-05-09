package KMIP

case class KeyBlock(
                     val keyFormatType : KeyFormatType, // Todo Need to be an enumeration
                     val keyValue : Option[KeyValue], // Not required
                     val cryptographicLength : CryptographicLength // required but MAY be omitted only if this information is available from the Key Value
                     /*...*/
                   ) extends Tag[Structure] with Structure {
  val value = this

  // Fields List all values in List
  lazy val fields = this.productIterator.toList.filter(a => a != None).map( a =>
    a match {
      case Some(x) => x
      case default => default
    }
  ).asInstanceOf[List[Tag[Type]]]
}
