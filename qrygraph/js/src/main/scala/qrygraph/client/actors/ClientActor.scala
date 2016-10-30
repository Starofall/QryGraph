package qrygraph.client.actors

import akka.actor.{Actor, Props}
import qrygraph.client.ui.UI
import qrygraph.shared.SharedMessages._
import qrygraph.shared.data.PigQueryGraph
import qrygraph.shared.layouting.GraphLayouter
import qrygraph.shared.nodes.{ComponentNode, NodeHelper}

/**
  * This is the main handler for anything that happens on the client
  * every incoming message passes this gateway
  */
class ClientActor extends Actor {

  /** encapsulate the whole state */
  val state = new ClientState()

  /** our clients creates a child actor that handles the webSocket communication */
  var networkIO = context.actorOf(Props(new ClientConnectionActor(self)), name = "ClientConnectionActor")

  /** if the actor starts or dies, this method is called to rebuild the UI in de DOM */
  override def preStart(): Unit = {}

  /** dummy used to prevent infinite loops in error state */
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    networkIO = null
  }

  /** apply a new pigQueryGraph to the system, the state and the UI */
  def updateAndUpload(qrygraph: PigQueryGraph): Unit = {
    // update local state
    state.applyGraphUpdate(qrygraph)
    // update right bar and  mark the validationState as processing
    UI.DeployButton.enable()
    UI.updateRightEditBar(state)
    UI.QueryStatusIcon.setChecking()
    // send an update to the server
    self ! CDraftGraphUpdate(state.graph)
  }

  /** describes how the actor handles a UIEvent message */
  def handleUIEvents: PartialFunction[UIEvent, Unit] = {

    case UINodeActivated(nodeId) =>
      UI.RightToolbar.show()
      state.graph.getNodeById(nodeId).map(n => {
        // save to state
        state.activatedNodeOption = Some(n)
        UI.updateRightEditBar(state)
      })

    case UIUndoChange() =>
      state.undoChange()
      UI.updateRightEditBar(state)
      self ! CDraftGraphUpdate(state.graph)
      UI.QueryStatusIcon.setChecking()

    case UIRequestExamples() =>
      UI.UpdateExamplesButton.requestStarted()
      self ! CQueryExamplesRequest()

    case UICreateNode(nodeName) => updateAndUpload(
      state.graph.addNode(NodeHelper.createNewNode(nodeName, state.queryContext))
    )

    case UICreateEdge(fromNodeId, toNodeId, fromPortId, toPortId) => updateAndUpload(
      state.graph.addEdge(fromNodeId, toNodeId, fromPortId, toPortId)
    )

    case UIUpdateFieldValue(nodeId, valueName, newValue) => updateAndUpload(valueName match {
      case "name" => state.graph.applyToNodeId(nodeId, _.applyValueChanges("name", NodeHelper.assureValidNodeName(newValue, state.graph.nodes.map(_.name))))
      case _      => state.graph.applyToNodeId(nodeId, _.applyValueChanges(valueName, newValue.replaceAll(";", "")

        /** prevent pig injection */))
    })

    case UINodeMoved(nodeId, newX, newY)    => updateAndUpload(state.graph.applyToNodeId(nodeId, n => NodeHelper.nodePositionHelper(n, n.position.copy(x = newX, y = newY))))
    case UIRemoveEdge(fromPortId, toPortId) => updateAndUpload(state.graph.removeEdge(fromPortId, toPortId))
    case UIRemoveNode(nodeId)               => updateAndUpload(state.graph.removeNode(nodeId))
    case UILayoutGraph()                    => updateAndUpload(GraphLayouter.layoutQrygraph(state.graph))
    case UIDeployGraph()                    => UI.DeployButton.disable(); self ! CDeployDraftRequest()
    case UIRevertToDeployed()               => self ! CRevertToDeployedRequest()
    case UINodesDeselected()                => state.activatedNodeOption = None; UI.RightToolbar.hide()
  }

  /** describes how the actor handles an InternalEvent message */
  def handleInternalEvents: PartialFunction[InternalEvent, Unit] = {
    case e: InitConfiguration     => networkIO ! e;
    case IConnectionEstablished() => self ! CGraphDraftRequest()
  }

  /** describes how the actor handles an ServerToClient message */
  def handleServerMessages: PartialFunction[ServerToClient, Unit] = {

    // An update of the graph on the server, so we update the view
    case SPigQueryQraphUpdate(graph) =>
      state.applyGraphUpdate(graph, serverUpdate = true) // serverUpdate means no undo option on this

    // execution results from the server
    case SQueryExamples(data) =>
      UI.UpdateExamplesButton.requestDone()
      state.applyExampleData(data)
      UI.updateRightEditBar(state)

    // the server sends us all dataSources that are available for our queries
    case SQueryMetaData(dataSources, components) =>
      state.updateQueryContext(dataSources, components)

    // graph type updates from the server
    case SQueryTypes(types, errors) =>
      state.applyTypeResults(types, errors)
      UI.updateRightEditBar(state)
      state.graph.nodes.flatMap(_.outputs).forall(n => types.get(n.label).isDefined || n.parent.isInstanceOf[ComponentNode]) match {
        case true  => UI.QueryStatusIcon.setValid()
        case false => UI.QueryStatusIcon.setWrong()
      }
  }

  /** default actor behavior to an incoming message */
  def receive = {
    case in: InternalEvent => handleInternalEvents(in)
    case x: ServerToClient => handleServerMessages(x)
    case ui: UIEvent       => handleUIEvents(ui)
    case x: ClientToServer => networkIO ! x // ClientToServer are send over networkIO
    case _                 => println("ClientActor got something undefined")
  }
}


