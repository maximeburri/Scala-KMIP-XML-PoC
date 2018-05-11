package KMIPTestsSuites

import org.scalatest._
import Matchers._

import KMIP._
import KMIP.XML._
import org.scalatest.FunSuite

class XMLTestsSuite  extends FunSuite{

  val completeKeyBlock = KeyBlock(
    KeyFormatTypeEnum.X509,
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

  val keyBlockWithoutKeyValue = KeyBlock(
    KeyFormatTypeEnum.X509,
    None,
    CryptographicLength(
      IntegerValue(16)
    )
  )

  test("Decoding XML integer value should be good") {
    fromXML(<CryptographicLength type="Integer" value="3"/>) should be (CryptographicLength(IntegerValue(3)))
  }

  test("Decoding XML good tag with bad type should fail") {
    the [Exception] thrownBy fromXML(<CryptographicLength type="String" value="3"/>)
  }

  test("Encoding and decoding an enumeration") {
    val struct = KeyFormatTypeEnum.X509
    val xml = <KeyFormatType type="Enumeration" value="X.509"/>
    toXML(struct) should be (xml)
    fromXML(xml) should be (struct)
  }

  test("Decoding an XML KeyBlock should be a KeyBlock") {
    val keyBlockXML = <KeyBlock type="Structure">
      <KeyFormatType type="Enumeration" value="X.509"/>
      <KeyValue type="ByteString" value="00010203040506070809abcdef"/>
      <CryptographicLength type="Integer" value="16"/>
    </KeyBlock>

    val result = fromXML(keyBlockXML)
    result shouldBe a [KeyBlock]
    result should be(completeKeyBlock)
  }

  test("Decoding an optional field should be accepted") {
    val keyBlockXML = <KeyBlock type="Structure">
      <KeyFormatType type="Enumeration" value="X.509"/>
      <CryptographicLength type="Integer" value="16"/>
    </KeyBlock>

    val result : KeyBlock = fromXML(keyBlockXML).asInstanceOf[KeyBlock]
    result.keyValue should be (None)
  }

  test("Decoding a missing required field should fail") {
    val keyBlockXML = <KeyBlock type="Structure">
      <KeyValue type="ByteString" value="00010203040506070809abcdef"/>
      <CryptographicLength type="Integer" value="16"/>
    </KeyBlock>

    the [Exception] thrownBy fromXML(keyBlockXML) should have message
      "Types not equal. Type expected : KMIP.KeyFormatTypeEnum, type found : KMIP.KeyValue"
  }

  test("Decoding an XML with an unknown structure should fail") {
    val keyBlockXML = <KeyBlock type="Structure">
      <KeyFormatType type="Enumeration" value="X.509"/>
      <ACustomKeyValue type="ByteString" value="00010203040506070809abcdef"/>
      <CryptographicLength type="Integer" value="16"/>
    </KeyBlock>

    the [Exception] thrownBy fromXML(keyBlockXML) should have message
      "Node label ACustomKeyValue not recognized"
  }

  test("Decoding an XML with a bad type should fail") {
    val keyBlockXML = <KeyBlock type="Structure">
      <KeyFormatType type="Enumeration" value="X.509"/>
      <KeyFormatType type="String" value="X.509"/>
      <CryptographicLength type="Integer" value="16"/>
    </KeyBlock>

    the [Exception] thrownBy fromXML(keyBlockXML) should have message
      "Types not equal. Type expected : KMIP.CryptographicLength, type found : KMIP.KeyFormatTypeEnum"
  }

  test("Decoding an XML with too many fields") {
    val keyBlockXML = <KeyBlock type="Structure">
      <KeyFormatType type="Enumeration" value="X.509"/>
      <KeyValue type="ByteString" value="00010203040506070809abcdef"/>
      <CryptographicLength type="Integer" value="16"/>
      <CryptographicLength type="Integer" value="17"/>
    </KeyBlock>

    the [Exception] thrownBy fromXML(keyBlockXML) should have message
      "Too much nodes for structure but found : List(<CryptographicLength type=\"Integer\" value=\"17\"/>, \n    )"
  }

  test("Encoding a complete KeyBlock should be a XML KeyBlock") {
    val result = <KeyBlock type="Structure"><KeyFormatType type="Enumeration" value="X.509"/><KeyValue type="ByteString" value="00010203040506070809abcdef"/><CryptographicLength type="Integer" value="16"/></KeyBlock>

    toXML(completeKeyBlock) should be (result)
  }

  test("Encoding a KeyBlock with missing optional field be a XML KeyBlock") {
    val result = <KeyBlock type="Structure"><KeyFormatType type="Enumeration" value="X.509"/><CryptographicLength type="Integer" value="16"/></KeyBlock>

    toXML(keyBlockWithoutKeyValue) should be (result)
  }

  test("Encoding and decoding a complete KeyBlock should be identical") {
    fromXML(toXML(completeKeyBlock)) should be (completeKeyBlock)
  }

  test("Encoding and decoding a KeyBlock with optional field should be identical") {
    fromXML(toXML(keyBlockWithoutKeyValue)) should be (keyBlockWithoutKeyValue)
  }

}
