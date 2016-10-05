package qrygraph.shared.nodes

import qrygraph.shared.data._

import scala.util.Random

/** COGroup Pig Node */
case class COGroupNode(id: String = Random.nextInt.toString,
                       name: String = NodeHelper.createNodeName(),
                       by1: String = "",
                       by2: String = "",
                       position: NodePosition = NodePosition.randomPosition) extends Node {

  val inputs = List(
    Input(this, id + "-1", "COGroup1"),
    Input(this, id + "-2", "COGroup2")
  )
  val outputs = List(Output(this, id + "-3", name))

  def compileToPig(incomingNames: List[Option[String]]) = (incomingNames(0), incomingNames(1)) match {
    case (Some(i1), Some(i2)) => List(s"${outputs.head.label} = COGROUP $i1 BY $by1, $i2 BY $by2;")
    case _                    => List(s"-- CoGroupNode $name not valid")
  }

  /** this node should change its value valueName to the newValue */
  override def applyValueChanges(valueName: String, newValue: String): Node = {
    valueName match {
      case "name" => this.copy(name = newValue)
      case "by1"  => this.copy(by1 = newValue)
      case "by2"  => this.copy(by2 = newValue)
      case _      => this
    }
  }

}