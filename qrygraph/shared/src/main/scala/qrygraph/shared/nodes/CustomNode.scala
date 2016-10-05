package qrygraph.shared.nodes

import qrygraph.shared.data._

import scala.util.Random
import scala.util.matching.Regex

case class CustomNode(id: String = Random.nextInt.toString,
                      name: String = NodeHelper.createNodeName(),
                      queryString: String = "",
                      position: NodePosition = NodePosition.randomPosition) extends Node {

  // @todo changing the inputs when nodes are attached crushes the system

  val pattern = new Regex("<\\d>")
  val numberOfIncomingNodes = pattern.findAllMatchIn(queryString).length

  val inputs = 0.until(numberOfIncomingNodes).toList.map(n => Input(this, id + n, name + "-" + n))
  val outputs = List(Output(this, id + "+1", name))

  def compileToPig(incomingNames: List[Option[String]]): List[String] = {
    val replacedVersion = incomingNames.zipWithIndex.foldRight(queryString)((tuple: (Option[String], Int), s: String) => {
      val (string, index) = tuple
      val adjustedIntex = index+1 //easier for users to start with 1
      s.replaceAll(s"<$adjustedIntex>", string.getOrElse("<0>"))
    })
    if(pattern.findFirstIn(replacedVersion).isDefined || queryString == ""){
      List(s"-- CustomNode $name not valid")
    }else{
      List(s"$name  = $replacedVersion;")
    }
  }

  /** this node should change its value valueName to the newValue */
  override def applyValueChanges(valueName: String, newValue: String): Node = valueName match {
    case "name"        => this.copy(name = newValue)
    case "queryString" => this.copy(queryString = newValue)
    case _             => this
  }

}

