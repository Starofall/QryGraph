package qrygraph.client.actors

/** an internal event used by the client */
sealed trait InternalEvent
case class IConnectionEstablished() extends InternalEvent
case class InitConfiguration(authToken: String, queryId: String, componentId: String) extends InternalEvent

/** events coming from the ui */
sealed trait UIEvent
case class UICreateEdge(fromNodeId: String, toNodeId: String, fromPortId: String, toPortId: String) extends UIEvent
case class UIRemoveEdge(fromPortId: String, toPortId: String) extends UIEvent
case class UIRemoveNode(nodeId: String) extends UIEvent
case class UIUndoChange() extends UIEvent
case class UIRevertToDeployed() extends UIEvent
case class UIDeployGraph() extends UIEvent
case class UINodeActivated(nodeId: String) extends UIEvent
case class UINodesDeselected() extends UIEvent
case class UIUpdateFieldValue(nodeId: String, valueName: String, newValue: String) extends UIEvent
case class UINodeMoved(nodeId: String, x: Int, y: Int) extends UIEvent
case class UICreateNode(nodeName: String) extends UIEvent
case class UIRequestExamples() extends UIEvent
