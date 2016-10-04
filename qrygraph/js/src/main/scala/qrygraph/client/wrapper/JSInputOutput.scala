package qrygraph.client.wrapper

import qrygraph.shared.data.{Input, Output}

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined


@ScalaJSDefined
class JSInput(val label: String, val `type`: String, val id: String, var parentNode: JSNode) extends js.Object {
  val multiple = false
}

object JSInput {

  def fromInput(jsParent: JSNode, i: Input) = {
    new JSInput(i.label, "result", i.id, jsParent) //options.toJSArray)
  }

}

@ScalaJSDefined
class JSOutput(val label: String, val `type`: String, val id: String, var parentNode: JSNode) extends js.Object {
  val multiple = true
}

object JSOutput {
  def fromOutput(jsParent: JSNode, o: Output) = {
    new JSOutput(o.label, "result", o.id, jsParent)
  }
}

