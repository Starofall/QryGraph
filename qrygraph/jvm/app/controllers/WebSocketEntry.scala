package controllers

import java.util.concurrent.TimeUnit
import javax.inject._

import actors.{ComponentEditingActor, GraphEditingActor, ServerConnectionActor}
import akka.actor._
import akka.stream.Materializer
import akka.util.Timeout
import com.google.inject.Inject
import models.Tables._
import models.{DatabaseAccess, LoginAccess}
import play.api.Logger
import play.api.libs.streams.ActorFlow
import play.api.mvc._

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import scala.concurrent.duration._

// enhance future
import util.FutureEnhancements.FutureExtensions

/** Handles the WebSocket connection and the ServerActor */
@Singleton
class WebSocketEntry @Inject()(implicit system: ActorSystem, materializer: Materializer, val app: play.api.Application) extends Controller with DatabaseAccess with LoginAccess {

  implicit val timeout = Timeout(FiniteDuration(1, TimeUnit.SECONDS))

  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  private def getOrCreateEditingChannel(id: String, isQuery: Boolean): ActorRef = {

    val prop = if (isQuery) {
      GraphEditingActor.props(id, app)
    } else {
      ComponentEditingActor.props(id, app)
    }

    val actorName = if (isQuery) {
      "qryeditor-" + id
    } else {
      "componenteditor-" + id
    }
    // we synchronize here to prevent double creation of actors
    synchronized(
      Await.result(system.actorSelection("/user/" + actorName).resolveOne().mapAll {
        case Success(actorRef) => Logger.debug("Reusing existing actor: " + actorName); actorRef
        case Failure(ex)       => Logger.debug("Creating new editor: " + actorName); system.actorOf(prop, name = actorName)
      }, 1.seconds)
    )
  }

  /**
    * this is the header entry for the webSocket connection
    * the clients connects to this and the session is connected to the ServerConnectionActor
    * this node will than handle the connection to the user
    */
  def webSocketChatEntry = WebSocket.acceptOrResult[String, String] { request =>

    import dbConfig.driver.api._
    import util.FutureEnhancements._

    val token = request.getQueryString("qgtoken").getOrElse("")
    val queryId = request.getQueryString("queryId").getOrElse("")
    val componentId = request.getQueryString("componentId").getOrElse("")
    checkToken(token).flatMapAll {
      // Login Successful
      case Success(Some(user)) =>
        // Check if query exists and user has access
        if (queryId != "") {
          // edit a query
          val query = for {
            queryFuture <- runQuerySingle(PigQueries.filter(_.id === queryId))
            accessFuture <- runQuerySingle(QueryAccessRights.filter(_.queryId === queryId).filter(_.userId === user.id))
          } yield (queryFuture, accessFuture)
          query.mapAll {
            case Success((Some(qry), Some(_)))                              => Right(ActorFlow.actorRef(out => ServerConnectionActor.props(out, user, getOrCreateEditingChannel(queryId, true))))
            case Success((Some(qry), None)) if qry.creatorUserId == user.id => Right(ActorFlow.actorRef(out => ServerConnectionActor.props(out, user, getOrCreateEditingChannel(queryId, true))))
            case Success((Some(qry), None))                                 => Left(Results.Unauthorized("No access to this query for the logged in user"))
            case Success((None, _))                                         => Left(Results.NotFound("Could not find query in database"))
            case Failure(e)                                                 => Left(Results.InternalServerError("Could not load query"))
          }
        } else {
          // edit a component
          val query = for {
            componentFuture <- runQuerySingle(PigComponents.filter(_.id === componentId))
          } yield componentFuture
          query.mapAll {
            case Success(Some(qry)) => Right(ActorFlow.actorRef(out => ServerConnectionActor.props(out, user, getOrCreateEditingChannel(componentId, false))))
            case Success(None) => Left(Results.NotFound("Could not find component in database"))
            case Failure(e)         => Left(Results.InternalServerError("Could not load component"))
          }
        }

      // Auth failed
      case _ => Future(Left(Results.Unauthorized("No authentication provided")))
    }
  }

}



