package qrygraph.shared.nodes

import qrygraph.shared.data._

import scala.util.Random
import scala.util.matching.Regex

case class ForeachNode(id: String = Random.nextInt.toString,
                       name: String = NodeHelper.createNodeName(),
                       foreachQuery: String = "",
                       position: NodePosition = NodePosition.randomPosition) extends Node {


  val pattern = new Regex("<\\d>")
  val numberOfIncomingNodes =  1 + pattern.findAllMatchIn(foreachQuery).length

  val inputs = 0.until(numberOfIncomingNodes).toList.map(n => Input(this, id + n, "Foreach-" + n))
  val outputs = List(Output(this, id + "-2",name))

  def compileToPig(incomingNames: List[Option[String]]): List[String] = incomingNames.head match {
    case Some(i) => List(s"$name = FOREACH $i GENERATE $foreachQuery;")
    case _       => List(s"-- ForeachNode $name not valid")
  }

  /** this node should change its value valueName to the newValue */
  override def applyValueChanges(valueName: String, newValue: String): Node = {
    valueName match {
      case "name"         => this.copy(name = newValue)
      case "foreachQuery" => this.copy(foreachQuery = newValue)
      case _              => this
    }
  }

}

