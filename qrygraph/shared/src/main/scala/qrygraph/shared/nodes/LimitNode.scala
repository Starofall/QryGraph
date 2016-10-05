package qrygraph.shared.nodes

import qrygraph.shared.data._

import scala.util.Random

case class LimitNode(id: String = Random.nextInt.toString,
                     name: String = NodeHelper.createNodeName(),
                     limitCount: String = "",
                     position: NodePosition = NodePosition.randomPosition) extends Node {

  val inputs = List(Input(this, id + "-1", "Limit"))
  val outputs = List(Output(this, id + "-2", name))

  def compileToPig(incomingNames: List[Option[String]]): List[String] = incomingNames.head match {
    case Some(i) => List(s"$name = LIMIT $i $limitCount;")
    case _       => List(s"-- LimitNode $name not valid")
  }

  /** this node should change its value valueName to the newValue */
  override def applyValueChanges(valueName: String, newValue: String): Node = {
    valueName match {
      case "name"       => this.copy(name = newValue)
      case "limitCount" => this.copy(limitCount = newValue)
      case _            => this
    }
  }

}

