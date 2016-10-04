package qrygraph.client.wrapper

import scala.scalajs.js

/** to access the graph in the JS world this object contains functions to alter it */
object JSGraph {

  /** represents the js node collection - used to push our data into JS */
  def jsNodes = js.Dynamic.global.window.d.graph.nodes.asInstanceOf[BackboneCollection[JSNode]]

  /** represents the js edge collection - used to push our data into JS */
  def jsEdges = js.Dynamic.global.window.d.graph.edges.asInstanceOf[BackboneCollection[JSEdge]]

  /** clears the js graph from all nodes and edges */
  def clear(): Unit = {
    // clear nodes
    val nodeList = jsNodes.toArray().map(_.id)
    jsNodes.remove(nodeList)
    // clear edges
    val edgeList = jsEdges.toArray().map(_.id)
    jsEdges.remove(edgeList)
  }
}
