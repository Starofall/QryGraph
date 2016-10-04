package qrygraph.shared.compilation

import qrygraph.shared.data._
import qrygraph.shared.nodes.ComponentNode

/**
  * Dependency Resolution for a DAG
  * This class is used to flat a graph into a linear dependency list that can be compiled later
  * it also removes all instances of ComponentNodes and flattens them into the normal graph
  */
object GraphFlatten {

  //  LoggerConfig.factory = PrintLoggerFactory()

  //  def log = //println("YEAH")

  /** wrapper class for the dependency evaluation */
  private case class DepNode(value: Node, var outgoing: List[DepNode] = List(), var incoming: List[DepNode] = List()) {
    override def toString: String = s"Node(${value.id})"
  }

  /** this preprocessing functions removes components in a graph and replaces them with a normal representation */
  def removeComponentNodes(g: PigQueryGraph): PigQueryGraph = {
    def findAllEdges(g: PigQueryGraph): List[Edge] = g.nodes.flatMap {
      case n: ComponentNode => n.serverComponent.pigQueryGraph.edges ++ findAllEdges(n.serverComponent.pigQueryGraph) // also recursion
      case _                => List()
    }

    /** recursive function that converts a graph to a list of depNodes - also breaks out the nodes of components */
    def findAllNodes(q: PigQueryGraph): List[Node] = q.nodes.flatMap {
      case c: ComponentNode => findAllNodes(c.serverComponent.pigQueryGraph)
      case x                => List(x)
    }

    /**
      * having edges that connect to input/output of the component is a problem for flattening
      * therefor we need to find all edges that are connected to a component and create a copy of them that connects to the right sub-node aka route them
      */
    def routeComponentEdges(el: List[Edge]): List[Edge] = el.map { e =>
      // we look at the parents - if they are ComponentNodes, they know how to route so we ask them
      (e, e.from.parent, e.to.parent) match {
        case (edge, fromParent: ComponentNode, _) => fromParent.routeEdge(edge, isInput = false)
        case (edge, _, toParent: ComponentNode)   => toParent.routeEdge(edge, isInput = true)
        case (edge, _, _)                         => edge
      }
    }

    PigQueryGraph(
      nodes = findAllNodes(g),
      // find all edges in subgraphs add the normal one's and route the component edges
      edges = routeComponentEdges(findAllEdges(g) ++ g.edges)
    )
  }

  /**
    * as a pig query can be a graph and not just a tree, it is important to have a valid dependency resolution strategy
    * this function goes through the graph and finds a valid evaluation order and returns it as a list of nodes
    */
  def flattenDependency(g: PigQueryGraph): List[Node] = {

    //println("Prepair graph flattening")
    val flattenedComponents = g

    /** all nodes wrapped in a DepNode instance */
    val depNodeList = flattenedComponents.nodes.map(node => DepNode(node))

    /** at the end it contains the order in which the nodes have to be resolved */
    var resolvingOrder = List[DepNode]()

    /** we start with the nodes that have no incoming node */
    val startingNodes = depNodeList.filter(_.incoming.isEmpty)

    /** true if node was already evaluated */
    def isEvaluated(depNode: DepNode) = resolvingOrder.contains(depNode)

    /** true if all nodes in list are evaluated */
    def areEvaluated(list: List[DepNode]) = list.forall(isEvaluated)

    /** goes recursive through the nodes and adds the resolved to resolvingOrder */
    def runDepSearch(list: List[DepNode]): Unit = {
      // create new version of nextNodes
      var nextNodes = List[DepNode]()
      // go through the list of unevaluated nodes
      for (node <- list; if !isEvaluated(node)) {
        //println("Working on node: " + node.value.id)
        areEvaluated(node.incoming) match {
          // all incoming nodes are evaluated, add this to done
          case true =>
            //println(s"Node ${node.value.id} has all dependencies resolved in $resolvingOrder")
            resolvingOrder ::= node
            nextNodes ++= node.outgoing
          // not all nodes are already evaluated, do this later
          case false =>
            //println(s"Node ${node.value.id} has not yet all depenencies - skip for later")
            nextNodes ::= node
        }
      }
      // do the next nodes if there are some
      if (nextNodes.nonEmpty && !(list == nextNodes)) {
        runDepSearch(nextNodes)
      }
      // finish
    }

    //println("Apply edges to graph")
    // apply the dependencies to the DepNodes
    flattenedComponents.edges.foreach(edge => {
      val from = depNodeList.find(_.value == edge.from.parent)
      val to = depNodeList.find(_.value == edge.to.parent)
      (from, to) match {
        case (Some(f), Some(t)) =>
          f.outgoing ::= t
          t.incoming ::= f
        case _                  => // ERROR case
      }
    })

    //println("Start DepSearch")
    // run the search - starting with the startingNodes
    runDepSearch(startingNodes)

    //println("DepSearch finished - return result values")
    // return the result and revert it
    resolvingOrder.map(_.value).reverse
  }
}