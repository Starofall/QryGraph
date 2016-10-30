package qrygraph.shared.compilation

import qrygraph.shared.data.PigQueryGraph

/** detecting cycles in graphs */
object CycleDetection {

  /** a graph has a cycle if the flattenDependency removed at least one node it */
  def graphHasCycle(newGraph: PigQueryGraph): Boolean = GraphFlatten.flattenDependency(newGraph).length < newGraph.nodes.length

}
