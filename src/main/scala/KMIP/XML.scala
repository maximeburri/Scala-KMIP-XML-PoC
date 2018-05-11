package KMIP

import scala.reflect.runtime.universe
import scala.reflect.runtime.universe.{Type => UniverseType, typeOf => universeTypeOf, _}
import scala.xml.{Elem, Node, PCData}
import TypeUtils._

object XML {
  val m = universe.runtimeMirror(getClass.getClassLoader)

  /**
    * Merge parameters of a constructor and nodes into an real object value
    * TODO: Converts exception into Either/Option
    * TODO: Generalize in subfunction to reimplement in other parsers (JSON,..)
    * @param params Types List of constructor parameters
    * @param nodes HTML nodes
    * @param nodeToTag Recursive function to convert a node to a tag
    * @return a list of new objects value Any. It's not a Tag[Type] because it can return a Type itself
    */
  def mergeParamsNodesToValues(params : List[UniverseType], nodes : List[Node], nodeToTag : Node => Tag[Type]) : List[Any] = {
    // Check params state
    params match {
      case paramType :: restParams => {
        // Check nodes state
        nodes match {
          case Nil => throw new Exception(s"Not enough nodes for structure. Waiting for ${restParams}")
          case firstNode :: restNodes if firstNode.label == "#PCDATA" =>
            mergeParamsNodesToValues(params, nodes.tail, nodeToTag)
          case firstNode :: restNodes => {
            def continue(value : Any) = value :: mergeParamsNodesToValues(restParams, nodes, nodeToTag)
            def next(value : Any) = value :: mergeParamsNodesToValues(restParams, restNodes, nodeToTag)

            val nodeType = stringTagToType(firstNode.label) match {
              case Some(x) => x
              case None => throw new Exception(s"Node label ${firstNode.label} not recognized")
            }

            paramType match {
              case param if param <:< universeTypeOf[Option[_]] => {
                if(nodeType <:< paramType.typeArgs.head)
                  next(Some(nodeToTag(firstNode)))
                else
                  continue(None)
              }
              case param if !(param <:< nodeType) =>
                throw new Exception(s"Types not equal. Type expected : ${param}, type found : ${nodeType}")
              case param if !(param <:< universeTypeOf[Tag[_]]) =>
                throw new Exception(s"The parameters of case class must need Option, or a Tag. ${param} finded")
              case default => next(nodeToTag(firstNode))
            }
          }
        }
      }
      case Nil => {
        nodes match {
          case Nil => Nil // Okay : end of list
          case firstNode :: restNodes if firstNode.label == "#PCDATA" =>
            mergeParamsNodesToValues(params, nodes.tail, nodeToTag)
          case restNodes => // Too much nodes is it an error
            throw new Exception(s"Too much nodes for structure but found : ${restNodes}")
        }
      }
    }
  }

  /**
    * Convert a node XML to a Tag
    * @param a the node to convert
    * @return the tag of type
    */
  def fromXML(a:Node) : Tag[Type] = {
    val typeOfNode = stringTagToType(a.label) match {
      case Some(x) => x
      case None => throw new Exception(s"Node label ${a.label} not recognized")
    }

    val cm = m.reflectClass(typeOfNode.typeSymbol.asClass)
    val ctor = typeOfNode.decl(universe.termNames.CONSTRUCTOR).asMethod
    val ctorm = cm.reflectConstructor(ctor)
    val params = ctor.paramLists.map(_.map(_.typeSignature)).head  // List(KMIP.Values.KeyFormatType, KMIP.Values.KeyValue, KMIP.Values.CryptographicLength)

    typeOfNode match {
      case t if t <:< universeTypeOf[Tag[Structure]] => {
        val values = mergeParamsNodesToValues(params, a.child.toList, fromXML)
        ctorm(values: _*).asInstanceOf[Tag[Type]]
      }
      case t if t <:< universeTypeOf[Tag[Enumeration]] => {
        stringToEnum((a \ "@value").toString())
      }
      case _ => {
        val newValue: Type = toValue(a)
        try {
          ctorm(newValue).asInstanceOf[Tag[Type]]
        }catch {
          case e:Exception => throw new Exception(s"Cannot create ${a.label}. Given : ${newValue}, expected : ${params}" )
        }
      }
    }
  }

  /**
    * Convert a tag to XML element
    * @param a the tag
    * @return
    */
  // TODO: tailrec
  def toXML(a:Tag[Type]) : Elem = {
    val elem : Elem = a.value match {
      case b: StringValue => <ttlv type="String" value={b.value}/>
      case b: IntegerValue => <ttlv type="Integer" value={b.value.toString}/>
      case b: ByteStringValue => <ttlv type="ByteString" value={bytes2hex(b.value)}/>
      case b: Enumeration => <ttlv type="Enumeration" value={enumToString(b)}/>
      case b: Structure => <ttlv type="Structure">{b.fields.map(toXML(_))}</ttlv>
    }
    elem.copy(label = tagToString(a))
  }

  /**
    * Convert attributes of a nodes to a value
    * @param a
    * @return
    */
  def toValue(a:Node) : Type = {
    lazy val value = (a \ "@value").toString()
    val result : Type = (a \ "@type").toString() match {
      case "String" => StringValue(value)
      case "Integer" => IntegerValue(value.toInt)
      case "ByteString" => ByteStringValue(hex2bytes(value))
    }
    result
  }

  def enumToString(t : Enumeration) : String = {
    t match {
      case KeyFormatTypeEnum.Raw => "Raw"
      case KeyFormatTypeEnum.X509 => "X.509"
      case KeyFormatTypeEnum.PKCS8 => "PKCS8"
    }
  }

  def stringToEnum(t : String) : Tag[Enumeration] = {
    t match {
      case "Raw" => KeyFormatTypeEnum.Raw
      case "X.509" => KeyFormatTypeEnum.X509
      case "PKCS8" => KeyFormatTypeEnum.PKCS8
    }
  }

  /**
    * Convert a string name into a UniverseType
    * @param a
    * @return
    */
  def stringTagToType(a : String) : Option[UniverseType] =
    a match {
      case "KeyFormatType" => Some(universeTypeOf[KeyFormatTypeEnum])
      case "KeyBlock" => Some(universeTypeOf[KeyBlock])
      case "KeyValue" => Some(universeTypeOf[KeyValue])
      case "CryptographicLength" => Some(universeTypeOf[CryptographicLength])
      case _ => None
    }
}