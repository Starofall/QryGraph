package actors

import akka.actor._
import models.Tables._
import models.{ComponentAccess, DatabaseAccess, MetaDataAccess}
import play.api.{Application, Logger}
import prickle.{Pickle, Unpickle}
import qrygraph.shared.SharedMessages._
import qrygraph.shared.data._
import services.PigTypeDetection

/** actor for editing components */
class ComponentEditingActor(componentId: String, val app: Application) extends AbstractCollaborationActor with DatabaseAccess with ComponentAccess with MetaDataAccess with PicklerImplicits {

  /** the state of the graph */
  var graphStore: PigComponent = loadComponent(componentId).get

  /** parsing the graph that is in the state */
  def parsedComponentGraph = Unpickle[PigQueryGraph].fromString(graphStore.serializedQuerie.getOrElse("")).getOrElse(PigQueryGraph.empty)

  def handleMessage = {
    case CRevertToDeployedRequest() =>
      // we copy the draft into the deployed field and set undeployedChanges to false
      graphStore = graphStore.copy(serializedQuerie = graphStore.serializedQuerie)
      storeComponent(graphStore)
      broadCast(SPigQueryQraphUpdate(parsedComponentGraph))

    case CDeployDraftRequest() =>
      // we copy the draft into the deployed field and set undeployedChanges to false
      graphStore = graphStore.copy(serializedQuerie = graphStore.serializedQuerie, published = true)
      storeComponent(graphStore)

    case CDraftGraphUpdate(graph) =>
      // client send us an updated graph, we have to save it and mark the query as changed
      graphStore = graphStore.copy(serializedQuerie = Some(Pickle.intoString(graph)))
      // sync in database
      storeComponent(graphStore)
      // update every other user about the change
      broadCast(SPigQueryQraphUpdate(graph), Some(sender()))
      // now check the graph
      val (types, errors) = PigTypeDetection.evaluateTypes(globalSetting, parsedComponentGraph)
      broadCast(SQueryTypes(types, errors))

    // Client Requested a Graph
    case CGraphDraftRequest() =>
      val currentSender = sender()
      // send the DataSources to the client using the configuration file
      sender ! SQueryMetaData(loadDataSources(), loadPublishedComponents())
      // send the DataSources to the client (using debug if not available atm)
      sender ! SPigQueryQraphUpdate(parsedComponentGraph)

    case x => Logger.warn("Unknown packet in ServerActor - content:" + x.toString)
  }
}

object ComponentEditingActor {
  def props(componentId: String, app: play.api.Application): Props = Props(new ComponentEditingActor(componentId, app))
}

