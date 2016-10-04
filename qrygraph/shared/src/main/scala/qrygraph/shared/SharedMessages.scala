package qrygraph.shared

import prickle.{CompositePickler, PicklerPair}
import qrygraph.shared.data._
import qrygraph.shared.nodes.{LoadNode, _}
import qrygraph.shared.pig.{PigDataTypesPickler, ResultType}

/**
  * Created by info on 11.04.2016.
  */
object SharedMessages {


  implicit var nodePickler = CompositePickler[Node]
  nodePickler = nodePickler
    .concreteType[LoadNode]
    .concreteType[SplitNode]
    .concreteType[FilterNode]
    .concreteType[JoinNode]
    .concreteType[GroupNode]
    .concreteType[OutputNode]
    .concreteType[DistinctNode]
    .concreteType[ComponentNode]
    .concreteType[CrossNode]
    .concreteType[CustomNode]
    .concreteType[COGroupNode]
    .concreteType[ForeachNode]
    .concreteType[LimitNode]
    .concreteType[OrderNode]
    .concreteType[SampleNode]
    .concreteType[UnionNode]

  implicit val pigTypesPickler = PigDataTypesPickler.pickler

  implicit var messagePickler: PicklerPair[NetworkMessage] = CompositePickler[NetworkMessage]

  trait PicklerImplicits {
    implicit var nodePickler = SharedMessages.nodePickler
    implicit var pigTypesPickler = SharedMessages.messagePickler
    implicit var messagePickler = SharedMessages.pigTypesPickler
  }

  //@formatter:off

  /** An NetworkMessage is something the server and the client exchange thought Akka/WebSocket */
  sealed trait NetworkMessage

  /** a message that only travels in the direction from the server to the client */
  trait ServerToClient extends NetworkMessage

  /** a message that only travels in the direction from the client to the server */
  trait ClientToServer extends NetworkMessage

  /** messages that are related to editing a graph */
  trait GraphEditing

  // Connection and keepAlive
  case class CKeepAlive()                                                   extends ClientToServer
  case class SKeepAlive()                                                   extends ServerToClient
  case class CUserDisconnected()                                            extends ClientToServer
  messagePickler = messagePickler.concreteType[CKeepAlive].concreteType[SKeepAlive].concreteType[CUserDisconnected]

  // Login
  case class CLoginRequest(requestedUsername: String)                       extends ClientToServer
  case class SLoginSuccessFul()                                             extends ServerToClient
  case class SLoginFailed(reason:String)                                    extends ServerToClient
  case class SNotAuthorized(reason:String)                                  extends ServerToClient
  messagePickler = messagePickler.concreteType[CLoginRequest].concreteType[SLoginSuccessFul].concreteType[SLoginFailed].concreteType[SNotAuthorized]

  // Deployment
  case class CDeployDraftRequest()                                       extends ClientToServer with GraphEditing
  case class CRevertToDeployedRequest()                                     extends ClientToServer with GraphEditing
  messagePickler = messagePickler.concreteType[CDeployDraftRequest].concreteType[CRevertToDeployedRequest]

  // Graph changes
  case class CDraftGraphUpdate(graph: PigQueryGraph)                           extends ClientToServer with GraphEditing
  messagePickler = messagePickler.concreteType[CDraftGraphUpdate]

  // Load from server
  case class CGraphDraftRequest()                                           extends ClientToServer with GraphEditing
  messagePickler = messagePickler.concreteType[CGraphDraftRequest]

  // Push update from server
  case class SPigQueryQraphUpdate(graph: PigQueryGraph)                                         extends ServerToClient
  case class SQueryMetaData(dataSources: List[DataSource], components: List[ServerComponent])                    extends ServerToClient
    {override def toString = s"SQueryMetaData(---)"}


  case class CQueryExamplesRequest()                       extends ClientToServer
  case class SQueryExamples(queryExamples: Map[String, List[List[String]]])             extends ServerToClient
    {override def toString = s"SQueryExamples(${queryExamples.keys}"}

  case class SQueryTypes(types: Map[String, ResultType],errors: Map[String, String])                    extends ServerToClient
    {override def toString = s"SQueryTypes(${types.keys.mkString("-")})"}

  messagePickler = messagePickler.concreteType[SPigQueryQraphUpdate].concreteType[SQueryMetaData].concreteType[SQueryExamples].concreteType[SQueryTypes].concreteType[CQueryExamplesRequest]

  //@formatter:on

}
