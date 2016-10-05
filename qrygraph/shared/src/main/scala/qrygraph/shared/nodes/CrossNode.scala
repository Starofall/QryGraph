package qrygraph.shared.nodes

import qrygraph.shared.data._

import scala.util.Random

case class CrossNode(id: String = Random.nextInt.toString,
                     name: String = NodeHelper.createNodeName(),
                     position: NodePosition = NodePosition.randomPosition) extends Node {

  val inputs = List(
    Input(this, id + "-1", "Cross1"),
    Input(this, id + "-2", "Cross2")
  )
  val outputs = List(Output(this, id + "-3", name))

  def compileToPig(incomingNames: List[Option[String]]): List[String] = (incomingNames(0), incomingNames(1)) match {
    case (Some(i1), Some(i2)) => List(s"$name = CROSS $i1, $i2;")
    case _                    => List(s"-- CrossNode $name not valid")
  }

  /** this node should change its value valueName to the newValue */
  override def applyValueChanges(valueName: String, newValue: String): Node = {
    valueName match {
      case "name" => this.copy(name = newValue)
      case _      => this
    }
  }

}
