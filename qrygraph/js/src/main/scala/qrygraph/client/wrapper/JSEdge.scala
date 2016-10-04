package qrygraph.client.wrapper

import qrygraph.shared.data.Edge

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.Random

/**
  * The JS representation of a Edge
  */
@ScalaJSDefined
class JSEdge(from: JSOutput, to: JSInput, newId: String = Random.nextInt().toString, routeColor: Int = 0)
  extends SimpleEdge(new EdgeInitializer(from.parentNode.id, to.parentNode.id, from.id, to.id)) {
  attributes.route = routeColor
  id = newId
  val view = js.Dynamic.global.SimpleEdgeView.extend({})
  view.parentGraph = JSModules.jSGraph
  view.model = this
}

object JSEdge {
  /** Converts a Edge to a JSEdge */
  def fromEdge(x: Edge, statusColor: Int) = {
    new JSEdge(
      JSOutput.fromOutput(JSNode.fromNode(x.from.parent,false), x.from),
      JSInput.fromInput(JSNode.fromNode(x.to.parent,false), x.to),
      x.id,
      statusColor
    )
  }
}
