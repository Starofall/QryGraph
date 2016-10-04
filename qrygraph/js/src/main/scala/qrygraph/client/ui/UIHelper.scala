package qrygraph.client.ui

import qrygraph.shared.data.Edge
import qrygraph.shared.pig.ResultType

/**
  * Created by info on 31.08.2016.
  */
object UIHelper {

  /** based on the state of the resultTypes of the source and the target this function returns a color index for an edge */
  def getColorCodeForEdge(e: Edge,resultTypes: Map[String, ResultType]): Int = {
    // in case of an join/cross we have a neighbour node that can be invalid
    val neighbourInputNodeOption = e.to.parent.inputs.find(_.id != e.to.id).map(_.parent.name)
    val neighbourHasNoTyping = neighbourInputNodeOption.exists(!resultTypes.contains(_))
    (resultTypes.get(e.from.parent.name), resultTypes.get(e.to.parent.name), neighbourHasNoTyping) match {
      case (Some(_), Some(_), _) => 5 // valid node -> green
      case (None, None, _)       => 3 // both sides not working -> yellow
      case (Some(_), None, true) => 3 // the to node is not valid but other neighbour also not -> yellow
      case (_, None, _)          => 1 // the to node is not valid -> red
      case (_, _, _)             => 0 // ERROR CASE should not happen -> grey
    }
  }
}
