package actors

import akka.actor._
import models.Tables._
import models.{DatabaseAccess, GraphAccess, MetaDataAccess}
import play.api.Application
import prickle.{Pickle, Unpickle}
import qrygraph.shared.SharedMessages.{CDeployDraftRequest, CDraftGraphUpdate, CGraphDraftRequest, CQueryExamplesRequest, CRevertToDeployedRequest, PicklerImplicits, SPigQueryQraphUpdate, SQueryExamples, SQueryMetaData, SQueryTypes}
import qrygraph.shared.data._
import services.{PigExampleGenerator, PigTypeDetection}

import scala.concurrent.Future

/**
  * This actor is created to allow the editing of a node with multiple users at the same time
  * the graphId is guaranteed to be valid
  */
class GraphEditingActor(graphId: String, val app: Application) extends AbstractCollaborationActor with DatabaseAccess with GraphAccess with MetaDataAccess with PicklerImplicits {
  import scala.concurrent.ExecutionContext.Implicits.global

  /* STATE */
  var graphStore: PigQuery = loadGraph(graphId).get

  def parsedQrygraphDeployed = Unpickle[PigQueryGraph].fromString(graphStore.serializedDeployedQuerie.getOrElse("")).getOrElse(PigQueryGraph.outputOnly)

  def parsedQrygraphDraft = Unpickle[PigQueryGraph].fromString(graphStore.serializedDraftQuerie.getOrElse("")).getOrElse(PigQueryGraph.outputOnly)

  def handleMessage = {
    case CRevertToDeployedRequest() =>
      // we copy the draft into the deployed field and set undeployedChanges to false
      graphStore = graphStore.copy(
        serializedDraftQuerie = graphStore.serializedDeployedQuerie,
        undeployedChanges = false
      )
      storeGraph(graphStore)
      broadCast(SPigQueryQraphUpdate(parsedQrygraphDraft))
      val (types, errors) = PigTypeDetection.evaluateTypes(globalSetting,parsedQrygraphDraft)
      broadCast(SQueryTypes(types, errors))

    case CDeployDraftRequest() =>
      // we copy the draft into the deployed field and set undeployedChanges to false
      graphStore = graphStore.copy(
        serializedDeployedQuerie = graphStore.serializedDraftQuerie,
        undeployedChanges = false
      )
      storeGraph(graphStore)
    // notify user?

    case CQueryExamplesRequest() =>
      val currentSender = sender()
      Future {
        PigExampleGenerator.generateExamples(globalSetting, parsedQrygraphDraft).map({
          currentSender ! SQueryExamples(_)
        })
      }

    case CDraftGraphUpdate(graph) =>
      // client send us an updated graph, we have to save it and mark the query as changed
      graphStore = graphStore.copy(
        serializedDraftQuerie = Some(Pickle.intoString(graph)),
        undeployedChanges = true
      )
      // sync in database
      storeGraph(graphStore)
      // update every other user about the change
      broadCast(SPigQueryQraphUpdate(graph), Some(sender()))
      // now check the graph
      val (types, errors) = PigTypeDetection.evaluateTypes(globalSetting,parsedQrygraphDraft)
      broadCast(SQueryTypes(types, errors))

    // Client Requested a Graph
    case CGraphDraftRequest() =>
      val currentSender = sender()
      // send the DataSources to the client using the configuration file
      currentSender ! SQueryMetaData(loadDataSources(), loadPublishedComponents())
      // send the DataSources to the client (using debug if not available atm)
      Future {
        currentSender ! SPigQueryQraphUpdate(parsedQrygraphDraft)
      }
      // evaluate the type system and send the update to the client
      Future {
        val (types, errors) = PigTypeDetection.evaluateTypes(globalSetting,parsedQrygraphDraft)
        currentSender ! SQueryTypes(types, errors)
      }
      // send results to client

  }
}

object GraphEditingActor {
  def props(graphId: String, app: play.api.Application): Props = Props(new GraphEditingActor(graphId, app))
}

