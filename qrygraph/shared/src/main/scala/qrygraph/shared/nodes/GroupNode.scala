package qrygraph.shared.nodes

import qrygraph.shared.data._

import scala.util.Random

/**
  * Created by info on 12.04.2016.
  */
case class GroupNode(var id: String = Random.nextInt.toString,
                     var name: String = NodeHelper.createNodeName(),
                     var groupBy: String = "",
                     var position: NodePosition = NodePosition.randomPosition) extends Node {

  val inputs = List(Input(this, id + "-1", "Group"))
  val outputs = List(Output(this, id + "-2", name))

  def compileToPig(incomingNames: List[Option[String]]): List[String] = incomingNames.head match {
    case Some(i) => if(groupBy == "all"){
      List(s"$name = GROUP $i $groupBy;")
    } else{
      List(s"$name = GROUP $i BY $groupBy;")
    }
    case _       => List(s"-- Groupnode $name not valid")
  }

  /** this node should change its value valueName to the newValue */
  override def applyValueChanges(valueName: String, newValue: String): Node = {
    valueName match {
      case "name"    => this.copy(name = newValue)
      case "groupBy" => this.copy(groupBy = newValue)
      case _         => this
    }
  }

}
