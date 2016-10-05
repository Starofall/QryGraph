package qrygraph.shared.nodes

import qrygraph.shared.data._

import scala.util.Random

case class LoadNode(id: String = Random.nextInt.toString,
                    name: String = NodeHelper.createNodeName(),
                    source: DataSource,
                    position: NodePosition = NodePosition.randomPosition) extends Node {

  val inputs = List()
  val outputs = List(Output(this, id + "-1", name))

  def compileToPig(incomingNames: List[Option[String]]): List[String] = {
    val fields = source.columns.map(x => s"${x.name}:${x.typeValue}").mkString(",")
    List(s"${outputs.head.label} = LOAD '${source.link}' USING PigStorage('${source.separator}') AS ($fields);")
  }

  /** this node should change its value valueName to the newValue */
  override def applyValueChanges(valueName: String, newValue: String): Node = {
    valueName match {
      case "name"     => this.copy(name = newValue)
      case _          => this
    }
  }

}
