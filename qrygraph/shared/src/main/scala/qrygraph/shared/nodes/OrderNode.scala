package qrygraph.shared.nodes

import qrygraph.shared.data._

import scala.util.Random

/**
  * Created by info on 12.04.2016.
  */
case class OrderNode(var id: String = Random.nextInt.toString,
                     var name: String = NodeHelper.createNodeName(),
                     var orderBy: String = "",
                     var position: NodePosition = NodePosition.randomPosition) extends Node {

  val inputs = List(Input(this, id + "-1", "Order"))
  val outputs = List(Output(this, id + "-2", name))

  def compileToPig(incomingNames: List[Option[String]]): List[String] = incomingNames.head match {
    case Some(i) => List(s"$name = ORDER $i BY $orderBy;")
    case _       => List(s"-- Groupnode $name not valid")
  }

  /** this node should change its value valueName to the newValue */
  override def applyValueChanges(valueName: String, newValue: String): Node = {
    valueName match {
      case "name"    => this.copy(name = newValue)
      case "orderBy" => this.copy(orderBy = newValue)
      case _         => this
    }
  }

}
