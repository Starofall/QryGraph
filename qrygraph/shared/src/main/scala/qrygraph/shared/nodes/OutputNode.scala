package qrygraph.shared.nodes

import qrygraph.shared.data._

import scala.util.Random

/**
  * Every graph has one single output node
  * it is used by the system to evaluate the node
  */
case class OutputNode(id: String = Random.nextInt.toString,
                      name: String = "storage",
                      position: NodePosition = NodePosition.randomPosition) extends Node {

  val inputs = List(Input(this, id + "-1", "Save"))

  val outputs = List()

  /**
    * this is just a dummy node so the compilation must return a valid type, but will not be used for the evaluation
    * as the execution engine will run "STORE $NAME"
    */
  def compileToPig(incomingNames: List[Option[String]]): List[String] = incomingNames.head match {
    case Some(i) => List(s"$name = LIMIT $i 1000000;--(dummy)")
    case _       => List("-- Query not valid")
  }

}
