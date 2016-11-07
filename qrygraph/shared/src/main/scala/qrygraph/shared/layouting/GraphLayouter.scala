package qrygraph.shared.layouting

import qrygraph.shared.data.{Node, NodePosition, PigQueryGraph}
import qrygraph.shared.nodes.{NodeHelper, OutputNode}

/**
  * This object contains functions to layout a given qrygraph into a good looking graph
  */
object GraphLayouter {

  /** Contains a node and a mutable position that will be applied to the node */
  private case class LayoutNode(node: Node, var x: Int = 0, var y: Int = 0)

  private object LayoutNode {

    def findByName(node: Node, layoutNodes: List[LayoutNode]): LayoutNode = {
      layoutNodes.find(layoutNode => layoutNode.node.name == node.name).get
    }

  }

  /* This is where the Output node will be placed */
  val initial_x = 770
  val initial_y = 200
  /* How far apart are the different levels should be */
  val step_x = 250
  val step_y = 100
  /* assumed maximum number of immediate predecessors of a node */
  val max_fanout = 10

  /**
    * First arrange nodes vertically, then horizontally
    *
    * @param qrygraph the original graph
    * @return the layouted graph
    */
  def layoutQrygraph(qrygraph: PigQueryGraph): PigQueryGraph = {
    layoutQrygraphVertically(layoutQrygraphHorizontally(qrygraph))
  }

  /**
    * Creates a new graph where nodes are arranged horizontally starting from the output.
    *
    * @param qrygraph the original graph
    * @return the horizontally layouted graph
    */
  def layoutQrygraphHorizontally(qrygraph: PigQueryGraph): PigQueryGraph = {
    // first we wrap the nodes into layout nodes with mutable positions
    val layoutNodes = qrygraph.nodes.map(LayoutNode(_))
    // find the output node
    val outputNode = qrygraph.nodes.find(_.isInstanceOf[OutputNode]).get
    // pass it to the recursive function
    horizontalLayoutStep(outputNode, qrygraph, layoutNodes)
    // now we apply the layoutNode positions to the nodes
    val finalLayout = layoutNodes.map(n => {
      NodeHelper.nodePositionHelper(n.node, NodePosition(n.x, n.y))
    })
    // return a new graph using the layouted nodes
    qrygraph.copy(nodes = finalLayout)
  }

  /**
    * Recursively goes through all nodes in the graph, from Output to leaves.
    * Sets their x's, and prepares provides information for setting the y's at later stages.
    *
    * @param node            the node whose position has to be changed
    * @param qrygraph
    * @param layoutNodes
    * @param step_count      the multiplier of the x offset of the node
    * @param branching_order which input the node corresponds to w.r.t its parent
    * @param parent_y_offset
    */
  def horizontalLayoutStep(node: Node, qrygraph: PigQueryGraph, layoutNodes: List[LayoutNode], step_count: Int = 0, branching_order: Int = 0, parent_y_offset: Int = 0): Unit = {

    val layoutNode = LayoutNode.findByName(node, layoutNodes)
    layoutNode.x = initial_x - step_count * step_x
    // y_offset will guide the vertical layout at later stages
    // the higher the y_offset, the lower in the graph the node should be placed
    // (since we do DFS, the y_offset could be simplified to an global counter, incremented at each node)
    val y_offset = max_fanout * parent_y_offset + branching_order
    layoutNode.y = y_offset

    node.inputs.foreach(i => {
      val incomingNode = qrygraph.incomingNode(i)

      if (incomingNode.isDefined) {
        val order = i.id.reverse.substring(0, 1).toInt
        horizontalLayoutStep(incomingNode.get, qrygraph, layoutNodes, step_count + 1, order, y_offset)
      }
    })

  }

  /**
    * Creates a new graph where nodes, grouped by same x's, are arranged vertically .
    *
    * @param qrygraph the original graph
    * @return the vertically layouted graph
    */
  def layoutQrygraphVertically(qrygraph: PigQueryGraph): PigQueryGraph = {
    // first we wrap the nodes into layout nodes with mutable positions
    val layoutNodes = qrygraph.nodes.map(LayoutNode(_))
    // start recursion
    verticalLayoutStep(qrygraph, layoutNodes)
    // now we apply the layoutNode positions to the nodes
    val finalLayout = layoutNodes.map(n => {
      NodeHelper.nodePositionHelper(n.node, NodePosition(n.x, n.y))
    })
    // return a new graph using the layouted nodes
    qrygraph.copy(nodes = finalLayout)
  }

  /**
    * Recursively goes through all nodes in the graph, from right-most to left-most ones.
    * Sets their y's for each group according to the order provided by the nodes' y offsets.
    *
    * @param qrygraph
    * @param layoutNodes
    * @param step_count
    */
  def verticalLayoutStep(qrygraph: PigQueryGraph, layoutNodes: List[LayoutNode], step_count: Integer = 0): Unit = {
    // find nodes at position x
    val x = initial_x - step_count * step_x
    val nodes = qrygraph.nodes.filter(_.position.x == x)

    if (nodes.isEmpty) {
      // end of recursion, nothing to do
      return
    }
    // sort them according to their y coordinate (prepared from horizontalLayoutStep() function)
    val sortedNodes = nodes.sortWith(_.position.y < _.position.y)
    val size = sortedNodes.size

    size match {

      case even if size % 2 == 0 =>
        val upperList = sortedNodes.slice(0, size / 2)
        setNodesAscending(upperList, layoutNodes, x, true)
        val lowerList = sortedNodes.slice(size / 2, size)
        setNodesDescending(lowerList, layoutNodes, x, true)

      case odd =>
        val upperList = sortedNodes.slice(0, size / 2)
        setNodesAscending(upperList, layoutNodes, x, false)
        val middleNode = sortedNodes(size / 2)
        val layoutNode = LayoutNode.findByName(middleNode, layoutNodes)
        layoutNode.x = x
        layoutNode.y = initial_y
        val lowerList = sortedNodes.slice(size / 2 + 1, size)
        setNodesDescending(lowerList, layoutNodes, x, false)
    }

    verticalLayoutStep(qrygraph, layoutNodes, step_count + 1)
  }

  def setNodesAscending(nodes: List[Node], layoutNodes: List[LayoutNode], x: Int, isEven: Boolean, index: Int = 0): Unit = {
    if (index < nodes.size) {
      val node = nodes(index)
      val step_count = index + 1
      val layoutNode = LayoutNode.findByName(node, layoutNodes)
      layoutNode.x = x
      var offset = step_count * step_y
      if (isEven && index == 0) {
        // so that we do not leave double space around the initial_y
        offset = step_count * step_y / 2
      }
      layoutNode.y = initial_y - offset
      setNodesAscending(nodes, layoutNodes, x, isEven, step_count)
    }
  }

  def setNodesDescending(nodes: List[Node], layoutNodes: List[LayoutNode], x: Int, isEven: Boolean, index: Int = 0): Unit = {
    if (index < nodes.size) {
      val node = nodes(index)
      val step_count = index + 1
      val layoutNode = LayoutNode.findByName(node, layoutNodes)
      layoutNode.x = x
      var offset = step_count * step_y
      if (isEven && index == 0) {
        // so that we do not leave double space around the initial_y
        offset = step_count * step_y / 2
      }
      layoutNode.y = initial_y + offset
      setNodesDescending(nodes, layoutNodes, x, isEven, step_count)
    }
  }

}