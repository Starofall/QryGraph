package qrygraph.shared.nodes

import qrygraph.shared.data.{NodePosition, _}

import scala.util.Random

case class DistinctNode(id: String = Random.nextInt.toString,
                        name: String = NodeHelper.createNodeName(),
                        position: NodePosition = NodePosition.randomPosition) extends Node {

  val inputs = List(Input(this, id + "-1", "Distinct"))
  val outputs = List(Output(this, id + "-2", name))

  def compileToPig(incomingNames: List[Option[String]]): List[String] = incomingNames.head match {
    case Some(i) => List(s"$name = DISTINCT $i;")
    case _       => List(s"-- DistinctNode $name not valid")
  }

  /** this node should change its value valueName to the newValue */
  override def applyValueChanges(valueName: String, newValue: String): Node = valueName match {
    case "name" => this.copy(name = newValue)
    case _      => this
  }

}
