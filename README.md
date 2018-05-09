# ScalaKMIP
[![Build Status](https://travis-ci.org/maximeburri/ScalaKMIP.svg?branch=master)](https://travis-ci.org/maximeburri/ScalaKMIP)

ScalaKMIP is an Scala implementation of [KMIP](https://www.oasis-open.org/committees/kmip/) (Key Management Interoperability Protocol). 
The current project is a PoC, a first architecture design and could lead to a full implementation of KMIP 1.4.

## Architecture
![diagram](images/diagram.png "Diagram")

A structure needs to extends from `Structure` and `Tag[Structure]`. 
The constructor is used for encoding/decoding with the Scala reflection API, 
and the signature and paramaters are used to encode and to check the decoding (cast into an object).
```scala
case class KeyBlock(
                     val keyFormatType : KeyFormatType, // Todo Need to be an enumeration
                     val keyValue : Option[KeyValue], // Not required
                     val cryptographicLength : CryptographicLength // required but MAY be omitted only if this information is available from the Key Value
                     /*...*/
                   ) extends StructuredTag
```
The fields can ba a 
- a class extends from `Tag[A <: Type]` (`Tag[Structure]` for structures, `Tag[StringValue]` for string field, etc..)
- a `Option[Tag[A <: Type]]` for optional field
Multiple fields and enumeration will be a future work.

## Examples
The XML encoding/decoding is implemented and the others (TTLV, JSON) will follow.
The following structures does not follow exactly the specification and fields.

Create the KeyBlock structure
```scala
import KMIP._
import KMIP.XML._

val keyBlock = KeyBlock(
    KeyFormatType(
      StringValue("X.509")
    ),
    Some( // Optional, can be None
      KeyValue(
        ByteStringValue(
          List[Byte](0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9 /*...*/)
        )
      )
    ),
    CryptographicLength(
      IntegerValue(10)
    )
  )
```

For simplicity, the use of implicit conversion for values e.g `String(value)` instead of `StringValue(String(value))`
```scala
import KMIP.TypeImplicits._

val keyBlock = KeyBlock(
    KeyFormatType("X.509"),
    Some( // Optional, can be None
      KeyValue(List[Byte](0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9 /*...*/))
    ),
    CryptographicLength(10)
  )
```

Convert it into XML
```scala
toXML(keyBlock)
```
```XML
<KeyBlock type="Structure">
  <KeyFormatType type="String" value="X.509"/>
  <KeyValue type="ByteString" value="00010203040506070809abcdef"/>
  <CryptographicLength type="Integer" value="16"/>
</KeyBlock>
```

And parse from XML
```scala
toXML(keyBlockXML)
```



