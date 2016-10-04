package qrygraph.shared.layouting

import qrygraph.shared.compilation.GraphFlatten
import qrygraph.shared.data.{Node, PigQueryGraph}

/**
  * This object contains functions to layout a given qrygraph into a good looking graph
  */
object GraphLayouter {

  private case class LayoutNode(node: Node, x: Int = 0, y: Int = 0)

  def layoutQrygraph(qrygraph: PigQueryGraph): PigQueryGraph = {
    val dependencies = GraphFlatten.flattenDependency(qrygraph).reverse
    val head = dependencies.head
    val layoutNodes = qrygraph.nodes.map(LayoutNode(_))
    //@todo missing here is applying changes
    qrygraph.copy(nodes = layoutNodes.map(_.node))
  }

}