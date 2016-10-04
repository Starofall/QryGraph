package qrygraph.client.wrapper

import org.scalajs.jquery.JQuery

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSName, ScalaJSDefined}


/** Wrapper for the the Backbone Model instance */
@JSName("Backbone.Model")
@js.native
class BackboneModel extends js.Object {
  var id: String = js.native
  var cid: String = js.native

  def get(p: String): js.Dynamic = js.native

  def set(p: String, v: js.Any): Unit = js.native
}

/**
  * Wrapper for a Backbone Model
  *
  * @tparam T Type of the content
  */
@JSName("Backbone.Collection")
@js.native
class BackboneCollection[T] extends js.Object {
  def remove(id: Any): js.Dynamic = js.native

  def add(any: T): js.Dynamic = js.native

  def get(any: Any): T = js.native

  def head(): T = js.native

  def toArray(): js.Array[T] = js.native
}

/** Used in the UI for a JSOutput and JSInput connection */
@js.native
class SimpleEdge(self: js.Object) extends BackboneModel {
  val source: JSOutput = js.native
  val target: JSInput = js.native
  var attributes: js.Dynamic = js.native
}

/** defines a target of an edge with ids */
@ScalaJSDefined
trait EdgeTarget extends BackboneModel {
  var node: String
  var port: String
}

/** used to create a edge with extends in backbone.js */
@ScalaJSDefined
class EdgeInitializer(fromNodeId: String, toNodeId: String, fromPortId: String, toPortId: String) extends js.Object {
  val parentGraph = JSModules.jSGraph
  val source: EdgeTarget = new EdgeTarget {
    var node: String = fromNodeId
    var port: String = fromPortId
  }
  val target: EdgeTarget = new EdgeTarget {
    var node: String = toNodeId
    var port: String = toPortId
  }
}

/** a node used by the framework */
@js.native
class SimpleNode extends BackboneModel {
  @JSName("type")
  var typ: String = js.native
  val attributes: NodeAttributes = js.native
  val inputs: BackboneCollection[JSInput] = js.native
  val outputs: BackboneCollection[JSOutput] = js.native
}

/** attributes a jsNode can have */
@ScalaJSDefined
trait NodeAttributes extends js.Object {
  @JSName("type")
  var typ: String
  var description: String
  var label: String
  var resultTypes: String
  var selected: Boolean
  var x: Int
  var y: Int
  var w: Int
  var h: Int
}

/** a view instance of a node  */
@JSName("SimpleNodeView")
@js.native
class SimpleNodeView(self: ViewInitializer) extends js.Object {
  var parentGraph: js.Dynamic = js.native
  var model: SimpleNode = js.native
  var $inner: JQuery = js.native
}

/** the base node view instance */
@ScalaJSDefined
class BaseNodeView(var myModel: JSNode) extends SimpleNodeView(new ViewInitializer(myModel, JSModules.jSGraph.view, JSModules.jSGraph))

/** needed to start a view */
@ScalaJSDefined
class ViewInitializer(var model: JSNode, var graph: js.Dynamic, var parentGraph: js.Dynamic) extends js.Object {}