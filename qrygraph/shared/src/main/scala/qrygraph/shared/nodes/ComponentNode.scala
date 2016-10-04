package qrygraph.shared.nodes

import qrygraph.shared.compilation.GraphFlatten
import qrygraph.shared.data._

import scala.util.Random

/**
  * a componentNode is a node that represents a stored component
  * it knows the componentId it represents and creates it's own input and output fields based on the component
  */
case class ComponentNode(var id: String = Random.nextInt.toString,
                         var name: String = NodeHelper.createNodeName(),
                         var serverComponent: ServerComponent,
                         var position: NodePosition = NodePosition.randomPosition) extends Node {

  /** to simplify the routing of edges to the component to the node inside, we keep a reference to the inputs */
  var inputRef = Map[String, Input]()
  /** to simplify the routing of edges to the component to the node inside, we keep a reference to the outputs*/
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
    // in order to compile a component, we need to adjust special cases
    // 1) if the edge is (left) outside of the graph, we can not look into the graph itself
    // 2) if the edge is inside the graph, a normal compilation can be done

    // As we only get the a list of incoming names for the component, we need to match dem to the inputs we know that are open
    // so that when we have two open inputs, the two incoming names map to those inputs
    val incomingNamesForInputs: Map[Input, Option[String]] = serverComponent.pigQueryGraph.openInputs.zip(incomingNames).toMap

    // function to compile nodes that either are connected from the outside or within the componentgraph
    def compileSubNodes(node: Node): List[String] = {
      node.compileToPig(node.inputs.map(input => {
        // first we try to map the input to the open inputs
        val incomingInput = incomingNamesForInputs.get(input)
        // if this input is open to the outside, we use the result
        if (incomingInput.isDefined) {
          incomingInput.get
        } else {
          // if it is not defined, we look inside the graph itself to find the edge
          serverComponent.pigQueryGraph.incomingOutput(input).map(output => output.label)
        }
      }))
    }

    // next flatten the dependency
    val flattendGraph = GraphFlatten.flattenDependency(serverComponent.pigQueryGraph)
    // compile subnodes on the flattend graph
    flattendGraph.flatMap(compileSubNodes)
  }

}
