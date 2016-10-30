package qrygraph.shared.nodes

import qrygraph.shared.data._

import scala.util.Random

/**
  * a componentNode is a node that represents a stored component
  * it knows the componentId it represents and creates it's own input and output fields based on the component
  */
case class ComponentNode(id: String = Random.nextInt.toString,
                         name: String = NodeHelper.createNodeName(),
                         serverComponent: ServerComponent,
                         position: NodePosition = NodePosition.randomPosition) extends Node {

  /** to simplify the routing of edges to the component to the node inside, we keep a reference to the inputs */
  var inputRef = Map[String, Input]()
  /** to simplify the routing of edges to the component to the node inside, we keep a reference to the outputs */
  var outputRef = Map[String, Output]()

  /** for each open input in component create an input */
  val inputs = serverComponent.pigQueryGraph
    .openInputs
    .zipWithIndex // we have to generate new input, as the this reference must go to the visible node
    .map(i => {
    val newId = id + i._2
    // add it to references for simpler edge routing later
    inputRef += newId -> i._1
    Input(this, newId, i._1.parent.name)
  })

  /** if present use the open output */
  val outputs = serverComponent.pigQueryGraph
    .openOutputs
    .zipWithIndex // we have to generate new output, as the this reference must go to the visible node
    .map(o => {
    val newId = id + o._2
    // add it to references for simpler edge routing later
    outputRef += newId -> o._1
    Output(this, newId, o._1.parent.name)
  })

  /** creates a new edge that routes to the relevant node in the graph instead of the componentNode */
  def routeEdge(edge: Edge, isInput: Boolean): Edge = isInput match {
    case true  => Edge(edge.from, inputRef(edge.to.id))
    case false => Edge(outputRef(edge.from.id), edge.to)
  }

  /** hands over the compilation to @ComponentSubGraph */
  def compileToPig(incomingNames: List[Option[String]]): List[String] = {
    println("!!!!!DEPRICATED!!!!")
    List()
  }
  
}
