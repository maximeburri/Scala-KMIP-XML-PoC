package KMIP
import KMIP.XML._

object Tests {
  def main(args: Array[String]): Unit = {
    val completeKeyBlock = KeyBlock(
      KeyFormatTypeEnum.PKCS8,
      Some(
        KeyValue(
          ByteStringValue(
            List[Byte](0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9,
              0xab.toByte, 0xcd.toByte, 0xef.toByte)
          )
        )
      ),
      CryptographicLength(
        IntegerValue(16)
      )
    )

    val xml = toXML(completeKeyBlock)

    val result = fromXML(xml)

    val explanation = result.asInstanceOf[KeyBlock].keyFormatType match {
      case KeyFormatTypeEnum.PKCS8 => "It's a PKCS8"
      case KeyFormatTypeEnum.X509 => "It's a X509 certificate"
      case _ => "Unknow"
    }
    println(explanation)


  }
}
