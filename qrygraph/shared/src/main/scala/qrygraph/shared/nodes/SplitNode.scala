package qrygraph.shared.nodes

import qrygraph.shared.data.{Output, _}

import scala.util.Random

case class SplitNode(id: String = Random.nextInt.toString,
                     name: String = NodeHelper.createNodeName(),
                     splitBy1: String = "",
                     splitBy2: String = "",
                     position: NodePosition = NodePosition.randomPosition) extends Node {

  val inputs = List(Input(this, id + "-1", "Split"))
  val outputs = List(
    Output(this, id + "-2", name+"1"),
    Output(this, id + "-3", name+"2")
  )

  def compileToPig(incomingNames: List[Option[String]]): List[String] = incomingNames.head match {
    case Some(i) => List(s"SPLIT $i INTO ${outputs(0).label} IF $splitBy1, ${outputs(1).label} IF $splitBy2;")
    case _       => List(s"-- FilterNode $name not valid")
  }

  /** this node should change its value valueName to the newValue */
  override def applyValueChanges(valueName: String, newValue: String): Node = {
    valueName match {
      case "name"      => this.copy(name = newValue)
      case "splitBy1" => this.copy(splitBy1 = newValue)
      case "splitBy2" => this.copy(splitBy2 = newValue)
      case _           => this
    }
  }
}

