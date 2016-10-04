package qrygraph.client.wrapper

import qrygraph.shared.data.Node
import qrygraph.shared.nodes._

import scala.scalajs.js.annotation.{JSName, ScalaJSDefined}

@ScalaJSDefined
class JSNode(newTyp: String) extends SimpleNode {
  //set type
  typ = newTyp
  val parentGraph = JSModules.jSGraph

  //@todo new nodes need higher height then old nodes
  @JSName("view")
  private val tempView = new BaseNodeView(this) //Modules.Dataflow.prototype.node("base-resizable").View.extend({})
}

object JSNode {

  def fromNode(n: Node, selectedNode: Boolean) = {
    // create a node with the name of the subclass name
    val node = new JSNode(n.getClass.getSimpleName)

    // apply the parentNode to all inputs
    for (input <- n.inputs) {
      val jsInput = JSInput.fromInput(node, input)
      jsInput.parentNode = node
      node.inputs.add(jsInput)
    }

    for (output <- n.outputs) {
      val jsOutput: JSOutput = JSOutput.fromOutput(node, output)
      jsOutput.parentNode = node
      node.outputs.add(jsOutput)
    }

    // apply data
    node.id = n.id
    node.typ = n.getClass.getSimpleName.replace("Node", "")
    node.attributes.typ = n.getClass.getSimpleName.replace("Node", "")
    node.attributes.label = n match {
      //@todo add more meta information
      case LoadNode(id, name, source, position)               => source.name
      case ComponentNode(id, name, serverComponent, position) => serverComponent.name
      case FilterNode(_, _, filter, _)                        => filter
      case _                                                  => ""
    }
    node.attributes.selected = selectedNode
    node.attributes.x = n.position.x
    node.attributes.y = n.position.y
    node.attributes.w = n.position.width
    node.attributes.h = n.position.height
    node
  }
}
