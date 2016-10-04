package qrygraph.shared.nodes

import qrygraph.shared.data._

import scala.util.Random

/**
  * Created by info on 12.04.2016.
  */
case class JoinNode(var id: String = Random.nextInt.toString,
                    var name: String = NodeHelper.createNodeName(),
                    var by1: String = "",
                    var by2: String = "",
                    var position: NodePosition = NodePosition.randomPosition) extends Node {

  val inputs = List(
    Input(this, id + "-1", "Join1"),
    Input(this, id + "-2", "Join2")
  )
  val outputs = List(Output(this, id + "-2", name))

  def compileToPig(incomingNames: List[Option[String]]): List[String] = (incomingNames(0), incomingNames(1)) match {
    case (Some(i1), Some(i2)) => List(s"${outputs.head.label} = JOIN $i1 BY $by1, $i2 BY $by2;")
    case _                    => List(s"-- JoinNode $name not valid")
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

  def applyPositionChange(newPosition: NodePosition): Node = {
    this.copy(position = newPosition)
  }


}
