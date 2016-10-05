package qrygraph.shared.compilation

import qrygraph.shared.data.PigQueryGraph

/**
  * Created by info on 05.10.2016.
  */
object CycleDetection {

  /** a graph has a cycle if the flattenDependency removed at least one node it */
  def graphHasCycle(newGraph: PigQueryGraph): Boolean = GraphFlatten.flattenDependency(newGraph).length < newGraph.nodes.length

}
