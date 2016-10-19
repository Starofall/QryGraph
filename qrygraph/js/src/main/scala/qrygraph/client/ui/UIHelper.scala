package qrygraph.client.ui

import qrygraph.shared.data.Edge
import qrygraph.shared.nodes.ComponentNode
import qrygraph.shared.pig.ResultType

/**
  * Created by info on 31.08.2016.
  */
object UIHelper {

  /** based on the state of the resultTypes of the source and the target this function returns a color index for an edge */
  def getColorCodeForEdge(e: Edge, resultTypes: Map[String, ResultType]): Int = {
    // in case of an join/cross we have a neighbour node that can be invalid
    val neighbourInputNodeOption = e.to.parent.inputs.find(_.id != e.to.id).map(_.parent.name)
    val neighbourHasNoTyping = neighbourInputNodeOption.exists(!resultTypes.contains(_))
    (resultTypes.get(e.from.parent.name).isDefined || e.from.parent.isInstanceOf[ComponentNode],
      resultTypes.get(e.to.parent.name).isDefined || e.to.parent.isInstanceOf[ComponentNode],
      neighbourHasNoTyping) match {
      case (true, true, _)     => 5 // valid node -> green
      case (false, false, _)   => 3 // both sides not working -> yellow
      case (true, false, true) => 3 // the to node is not valid but other neighbour also not -> yellow
      case (_, false, _)       => 1 // the to node is not valid -> red
      case (_, true, _)        => 4 // ERROR CASE
    }
  }
}
