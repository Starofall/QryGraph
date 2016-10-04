package qrygraph.client.wrapper

import scala.scalajs.js

/** Used to reference dynamic js contents and modules */
object JSModules{
  /** the dataflow libaray */
  val Dataflow = js.Dynamic.global.window.Dataflow
  /** a default node */
  val Node = Dataflow.prototype.module("node")
  /** the graph in js */
  val jSGraph = js.Dynamic.global.d.graph
  /** simple code in JS world */
  val ViewHelper = js.Dynamic.global.ViewHelper
}
