package qrygraph.shared.nodes

import qrygraph.shared.data._

import scala.util.Random

case class LoadNode(id: String = Random.nextInt.toString,
                    name: String = NodeHelper.createNodeName(),
                    source: QueryLoadSource,
                    position: NodePosition = NodePosition.randomPosition) extends Node {

  val inputs = List()
  val outputs = List(Output(this, id + "-1", name))

  def compileToPig(incomingNames: List[Option[String]]): List[String] = {
    List(s"$name = ${source.loadCommand};")
  }

  /** this node should change its value valueName to the newValue */
  override def applyValueChanges(valueName: String, newValue: String): Node = {
    valueName match {
      case "name"     => this.copy(name = newValue)
      case _          => this
    }
  }

}
