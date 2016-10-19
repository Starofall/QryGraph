package qrygraph.client.actors

import org.scalajs.jquery.jQuery
import qrygraph.client.ui.{UI, UIHelper}
import qrygraph.client.wrapper._
import qrygraph.shared.compilation.QueryCompiler
import qrygraph.shared.data._
import qrygraph.shared.pig.ResultType

/** The Graph keeps track of the nodes and edges from both JS and Scala and syncs the two on request */
class ClientState {

  /** private _graph object - private as we only want to allow changes through applyGraphUpdate */
  private var _graph = PigQueryGraph()

  /** a list of qrygraphs that are used for the back functionality */
  private var _graphHistory = List[PigQueryGraph]()

  /** results of the typ detection on the server */
  private var _resultTypes: Map[String, ResultType] = Map()

  /** error results of the typ detection on the server */
  private var _resultErrors: Map[String, String] = Map()

  /** a map of all exampleData for each node */
  private var _exampleData: Map[String, List[List[String]]] = Map()

  /** the currently selected node used to display the toolbar */
  var activatedNodeOption: Option[Node] = None

  /** returns the graph */
  def graph = _graph

  /** returns the resultTypes */
  def resultTypes = _resultTypes

  /** returns the resultTypes */
  def resultErrors = _resultErrors

  /** returns the exampleData */
  def exampleData = _exampleData

  /** returns a dataSources  */
  var queryContext = QueryContext()

  /** saves the dataSources */
  def updateQueryContext(newDataSources: List[QueryLoadSource], newComponents: List[ServerComponent]): Unit = {
    queryContext = QueryContext(newDataSources, newComponents)
    // @todo here we have to update the UI to show all data sources in the menu
  }

  /** saves the typResults */
  def applyTypeResults(newTypes: Map[String, ResultType], errors: Map[String, String]) = {
    _resultTypes = newTypes
    _resultErrors = errors
    // to apply the new colors to the bars we need to sync with JS
    syncToJavaScriptWorld()
    // if the nodes should not be updated this would be sufficent:
    //    val edgeList = JSGraph.jsEdges.toArray().map(_.id)
    //    JSGraph.jsEdges.remove(edgeList)
    //    graph.edges.foreach(e => JSGraph.jsEdges.add(JSEdge.fromEdge(e, getColorCodeForEdge(e))))
  }

  /** saves the example data */
  def applyExampleData(data: Map[String, List[List[String]]]) = {
    _exampleData = data
  }

  def undoChange(): Unit = _graphHistory.headOption match {
    case Some(x) =>
      // get the tail before we update the state
      val tail = _graphHistory.tail
      // apply the head of the history to the graph
      applyGraphUpdate(x)
      // set the tail as new history
      _graphHistory = tail
      if (tail.isEmpty) UI.UndoButton.disable()
    case None    =>
      UI.UndoButton.disable()
  }

  /**
    * applies the changes to the UI view
    * if it comes from the server, we remove the option to undo things
    */
  def applyGraphUpdate(newGraph: PigQueryGraph = _graph, serverUpdate: Boolean = false) = {
    if (!serverUpdate) {
      //add old Graph to history
      _graphHistory = _graph :: _graphHistory
      UI.UndoButton.enable()
    }
    // update local state
    _graph = newGraph
    // update JS view
    syncToJavaScriptWorld()
    jQuery("#compilation").html(QueryCompiler.compileHTML(graph))
  }

  /** once changes have been made to the graph - this functions can be used to sync the state to the JS world */
  private def syncToJavaScriptWorld(): Unit = {
    JSGraph.clear()
    graph.nodes.foreach(n => JSGraph.jsNodes.add(JSNode.fromNode(n, activatedNodeOption.exists(_.id == n.id))))
    graph.edges.foreach(e => JSGraph.jsEdges.add(JSEdge.fromEdge(e, UIHelper.getColorCodeForEdge(e,resultTypes))))
  }

}
