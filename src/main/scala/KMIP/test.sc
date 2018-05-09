import KMIP._

val array = Array[Int](0, 1)

case class Bytes(val value: Array[Int])

val bytes = Bytes(array)
bytes.value