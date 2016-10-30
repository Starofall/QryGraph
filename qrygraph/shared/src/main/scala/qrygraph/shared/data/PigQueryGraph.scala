package qrygraph.shared.data

import qrygraph.shared.compilation.CycleDetection
import qrygraph.shared.nodes.{ComponentNode, OutputNode}

/** a qrygraph consists of a list of nodes and edges */
case class PigQueryGraph(nodes: List[Node] = List(), edges: List[Edge] = List()) {

  /** applies the function f to the node with the nodeId and returns a new qrygraph */
  def applyToNodeId(nodeId: String, f: (Node) => Node): PigQueryGraph = {
    // return a copy with a new nodes list
    def newNodes: List[Node] = nodes.map(n => {
      // apply f to the correct n
      if (n.id == nodeId) {
        // correct node, apply function here
        f(n)
      } else {
        // if it is a componentNode, we apply the function to the graph in the component
        n match {
          case c: ComponentNode => c.copy(serverComponent = c.serverComponent.copy(pigQueryGraph = c.serverComponent.pigQueryGraph.applyToNodeId(nodeId, f)))
          case _                => n
        }
      }
    })

    // for those edges who were linked to the old node, we need to adjust it
    val newEdges = edges.flatMap(e => {
      // for each edge...
      if (e.from.parent.id == nodeId) {
        // OUTPUT CASE
        // edge was linked to old from, create a new edge
        newNodes.find(_.id == nodeId).flatMap(_.outputs.find(_.id == e.from.id)) match {
          case Some(x) => Some(e.copy(from = x))
          case None    => None // edge no longer available, so we remove the edge
        }
      } else if (e.to.parent.id == nodeId) {
        // INPUT CASE
        // edge was linked to old to, create a new edge
        newNodes.find(_.id == nodeId).flatMap(_.inputs.find(_.id == e.to.id)) match {
          case Some(x) => Some(e.copy(to = x))
          case None    => None // edge no longer available, so we remove the edge
        }
      } else {
        Some(e)
      }
    })
    this.copy(nodes = newNodes, edges = newEdges)
  }

  /** returns an option to a node that is coming into this input */
  def incomingNode(input: Input): Option[Node] = {
    edges.find(_.to.id == input.id).map(_.from.parent)
  }

  def outgoingNode(output: Output): Option[Node] = {
    edges.find(_.from.id == output.id).map(_.to.parent)
  }

  /** returns an option to a node that is coming into this input */
  def incomingOutput(input: Input): Option[Output] = {
    edges.find(_.to.id == input.id).map(_.from)
  }

  def getNodeById(nodeId: String): Option[Node] = {
    nodes.find(_.id == nodeId)
  }

  def getInputById(inputId: String): Option[Input] = {
    // go through all nodes
    nodes.flatMap(n => {
      // filter for id is correct
      n.inputs.filter(o => {
        o.id == inputId
      })
      // find the defined element
    }).headOption
  }

  def getOutputById(outputId: String): Option[Output] = {
    // go through all nodes
    nodes.flatMap(n => {
      // filter for id is correct
      n.outputs.filter(o => {
        o.id == outputId
      })
      // find the defined element
    }).headOption
  }

  /** remove an edge from the graph */
  def removeEdge(fromPortId: String, toPortId: String): PigQueryGraph = {
    // create a new graph without the edge to remove
    this.copy(edges = edges.filter(e => {
      !(e.from.id == fromPortId && e.to.id == toPortId)
    }))
  }

  /** removes a node from the graph */
  def removeNode(nodeId: String): PigQueryGraph = {
    // create a new graph without the node to remove
    this.copy(
      // filter nodes
      nodes = nodes.filter(nodeId != _.id),
      // also remove all edges that were linked to this node
      edges = edges.filter(e =>
        e.from.parent.id != nodeId &&
          e.to.parent.id != nodeId
      )
    )
  }

  /** add an edge on the graph */
  def addEdge(fromNodeId: String, toNodeId: String, fromPortId: String, toPortId: String): PigQueryGraph = {
    val fromNode = nodes.find(_.id == fromNodeId)
    val toNode = nodes.find(_.id == toNodeId)
    val fromPort = fromNode.get.outputs.find(_.id == fromPortId)
    val toPort = toNode.get.inputs.find(_.id == toPortId)
    // add new edge to the graph
    val newGraph = this.copy(edges = Edge(fromPort.get, toPort.get) :: edges)
    // check for cycles and prevent them
    CycleDetection.graphHasCycle(newGraph) match {
      case true  => println("WARNUNG: CYCLE DETECTED");this
      case false => newGraph
    }
  }

  /** adds a node to the graph */
  def addNode(node: Node): PigQueryGraph = {
    this.copy(nodes = node :: nodes)
  }

  /** finds the input nodes out of a sub-graph */
  def openInputs: List[Input] = {
    nodes.map(_.inputs).flatMap(inputList => {
      inputList.filter(input => {
        incomingNode(input).isEmpty
      })
    })
  }

  /** finds output node of the sub-graph */
  def openOutputs: List[Output] = {
    nodes.map(_.outputs).flatMap(maybeOutput => {
      maybeOutput.filter(output => {
        outgoingNode(output).isEmpty
      })
    })
  }

  /** simplify the tostring for debugging */
  //  override def toString: String = s"qrygraph(${nodes.length},${edges.length})"
}

object PigQueryGraph {
  val outputOnly = PigQueryGraph(List(OutputNode()), List())
  val empty = PigQueryGraph(List(), List())
}