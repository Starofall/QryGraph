package qrygraph.client.wrapper

import qrygraph.client.Main
import qrygraph.client.actors._

import scala.scalajs.js.annotation.{JSExport, JSExportAll}

/**
  * JS wide accessible object that is used to handle incoming messages from the framework
  * and forwards them to the main.clientActor
  */
@JSExport("UIListener")
@JSExportAll
object UIListener {

  def createEdge(fromNodeId: String, toNodeId: String, fromPortId: String, toPortId: String) = Main.clientActor ! UICreateEdge(fromNodeId, toNodeId, fromPortId, toPortId)

  def nodeMovedEvent(nodeId: String, x: Int, y: Int) = Main.clientActor ! UINodeMoved(nodeId, x, y)

  def nodesDeselected() = Main.clientActor ! UINodesDeselected()

  def updateFieldValue(nodeId: String, valueName: String, newValue: String) = Main.clientActor ! UIUpdateFieldValue(nodeId, valueName, newValue)

  def removeEdge(fromPortId: String, toPortId: String) = Main.clientActor ! UIRemoveEdge(fromPortId, toPortId)

  def removeNode(nodeId: String) = Main.clientActor ! UIRemoveNode(nodeId)

  def deployGraph() = Main.clientActor ! UIDeployGraph()

  def revertToDeployed() = Main.clientActor ! UIRevertToDeployed()

  def undoChange() = Main.clientActor ! UIUndoChange()

  def nodeActivated(nodeId: String) = Main.clientActor ! UINodeActivated(nodeId)

  def createNode(nodeName: String) = Main.clientActor ! UICreateNode(nodeName)

  def requestExamples() = Main.clientActor ! UIRequestExamples()

  def layoutGraph() = Main.clientActor ! UILayoutGraph()
}

