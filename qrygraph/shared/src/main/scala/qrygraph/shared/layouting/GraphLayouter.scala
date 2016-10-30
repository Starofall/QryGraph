package qrygraph.shared.layouting

import qrygraph.shared.data.{Node, NodePosition, PigQueryGraph}
import qrygraph.shared.nodes.NodeHelper

import scala.util.Random

/**
  * This object contains functions to layout a given qrygraph into a good looking graph
  */
object GraphLayouter {

  /** contains a node and a mutable position that will be applied to the node */
  private case class LayoutNode(node: Node, var x: Int = 0, var y: Int = 0)

  def layoutQrygraph(qrygraph: PigQueryGraph): PigQueryGraph = {
    // first we wrap the nodes into layout nodes with mutable positions
    val layoutNodes = qrygraph.nodes.map(LayoutNode(_))


    //@todo missing here is applying changes
    layoutNodes.foreach(n => {
      n.x = Random.nextInt(500)
      n.y = Random.nextInt(500)
    })


    // now we apply the layoutNode positions to the nodes
    val finalLayout = layoutNodes.map(n => {
      NodeHelper.nodePositionHelper(n.node, NodePosition(n.x, n.y))
    })
    // return a new graph using the layouted nodes
    qrygraph.copy(nodes = finalLayout)
  }

}