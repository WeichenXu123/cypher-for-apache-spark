package org.opencypher.spark.impl.frame

import org.opencypher.spark.api.{CypherType, MaterialCypherType}
import org.opencypher.spark.impl.StdSlot
import org.opencypher.spark.impl.error.StdErrorInfo
import org.opencypher.spark.impl.util.Verification

import scala.reflect.runtime.universe.TypeTag
import scala.reflect.runtime.universe.typeOf

import scala.language.postfixOps

object FrameVerification {

  abstract class Error(detail: String)(implicit private val info: StdErrorInfo)
    extends Verification.Error(detail) {
    self: Product with Serializable =>
  }

  abstract class TypeError(msg: String)(implicit info: StdErrorInfo) extends Error(msg) {
    self: Product with Serializable =>
  }

  final case class IsNoSuperTypeOf(actualType: CypherType, baseType: CypherType)(implicit info: StdErrorInfo)
    extends TypeError(
      s"Supertype expected, but $actualType is not a supertype of $baseType"
    )(info)

  final case class IsNoSubTypeOf(actualType: CypherType, baseType: CypherType)(implicit info: StdErrorInfo)
    extends TypeError(
      s"Subtype expected, but $actualType is not a subtype of $baseType"
    )(info)

  final case class UnInhabitedMeetType(lhsType: CypherType, rhsType: CypherType)(implicit info: StdErrorInfo)
    extends TypeError(s"There is no value of both type $lhsType and $rhsType")(info)

  final case class FrameSignatureMismatch(msg: String)(implicit info: StdErrorInfo)
    extends Error(msg)(info)

  final case class SlotNotEmbeddable(key: Symbol)(implicit info: StdErrorInfo)
    extends Error(s"Cannot use slot $key that relies on a non-embedded representation")(info)

  final case class UnObtainable[A](thing: String, arg: A)(implicit info: StdErrorInfo)
    extends Error(s"Cannot obtain $thing '$arg'")
}

trait FrameVerification extends Verification with StdErrorInfo.Implicits {

  import FrameVerification._

  protected def requireInhabitedMeetType(lhsType: CypherType, rhsType: CypherType) =
    ifNot((lhsType meet rhsType).isInhabited.maybeTrue) failWith UnInhabitedMeetType(lhsType, rhsType)

  protected def requireIsSuperTypeOf(newType: CypherType, oldType: CypherType) =
    ifNot(newType `superTypeOf` oldType isTrue) failWith IsNoSuperTypeOf(newType, oldType)

  protected def requireEmbeddedRepresentation(lhsKey: Symbol, lhsSlot: StdSlot) =
    ifNot(lhsSlot.representation.isEmbedded) failWith SlotNotEmbeddable(lhsKey)

  protected def requireMateriallyIsSubTypeOf(actualType: CypherType, materialType: MaterialCypherType) =
    ifNot(actualType.material `subTypeOf` materialType isTrue) failWith IsNoSubTypeOf(actualType, materialType)

  protected def obtain[A, T : TypeTag](value: A => Option[T])(arg: A): T = {
    val thing = typeOf[T].termSymbol.name.toString
    ifMissing(value(arg)) failWith UnObtainable(thing, arg)
  }
}