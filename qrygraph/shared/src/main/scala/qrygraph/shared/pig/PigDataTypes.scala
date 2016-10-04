package qrygraph.shared.pig

import prickle.{CompositePickler, Pickler}
import qrygraph.shared.pig.Primitives._

/** the typing of a result a node creates */
case class ResultType(name: String, fields: List[PField])

/** represents a typed field as a data type like (username:chararray) */
case class PField(var name: String, typ: DataType)

/** A PigDataTyp defines a type of a given column */
sealed trait DataType

/** All Pig primitives data types  */
object Primitives {

  case object PByteArray extends DataType

  case object PScalar extends DataType

  case object PInt extends DataType

  case object PLong extends DataType

  case object PFloat extends DataType

  case object PDouble extends DataType

  case object PArray extends DataType

  case object PCharArray extends DataType

  case object PComplex extends DataType

  //@todo
  case object PDateTime extends DataType
  case object PBigInteger extends DataType
  case object PBgDecimal extends DataType
}

/** Pig tuple data type */
case class PTuple(fields: List[PField]) extends DataType
/** Pig bag data type */
case class PBag(fields: List[PField]) extends DataType

case class PMap(value: DataType) extends DataType


/** helper to serialize PigDataTypes over webSocket */
object PigDataTypesPickler {

  // !!! as the data structure is recursive, we need to define the parent first
  implicit var pickler = CompositePickler[DataType]
  // then the recursive structure
  implicit val fieldPickler: Pickler[PField] = Pickler.materializePickler[PField]
  // then add all children
  pickler = pickler.concreteType[PByteArray.type]
    .concreteType[PScalar.type]
    .concreteType[PInt.type]
    .concreteType[PLong.type]
    .concreteType[PFloat.type]
    .concreteType[PDouble.type]
    .concreteType[PArray.type]
    .concreteType[PCharArray.type]
    .concreteType[PComplex.type]
    .concreteType[PMap]
    .concreteType[PTuple]
    .concreteType[PBag]
}
