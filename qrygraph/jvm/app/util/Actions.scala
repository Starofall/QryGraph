package util

import controllers.routes
import models.{DatabaseAccess, LoginAccess}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{ActionTransformer, _}
import play.api.{Application, Logger}
import models.Tables._
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by info on 11.07.2016.
  */
object Actions {

  /** A action for a public available url with user checking and logging */
  def PublicAction(app: Application): ActionBuilder[UserOptionalRequest] = {
    CheckSetupDone(app) andThen CheckTokenAction(app) andThen LogAction
  }

  /** a private action only available to logged in users */
  def AuthedAction(app: Application) = {
    CheckSetupDone(app) andThen CheckTokenAction(app) andThen LogAction andThen UserOnlyAction
  }


  /** Reads a query from the database and appends it */
  case class ReadQueryFromId(app: Application, queryId: String) extends ActionRefiner[UserRequest, QueryRequest] with DatabaseAccess {

    def refine[A](request: UserRequest[A]): Future[Either[Result, QueryRequest[A]]] = {
      import dbConfig.driver.api._
      import models.Tables._
      import util.FutureEnhancements._

      val query = for {
        queryFuture <- runQuerySingle(PigQueries.filter(_.id === queryId))
        accessFuture <- runQuerySingle(QueryAccessRights.filter(_.queryId === queryId).filter(_.userId === request.user.id))
      } yield (queryFuture, accessFuture)

      query.mapAll {
        case Success((Some(qry), Some(_)))        => Right(QueryRequest(qry, request))
        case Success((Some(qry), None))
          if qry.creatorUserId == request.user.id => Right(QueryRequest(qry, request))
        case Success((Some(qry), None))           => Left(Results.Unauthorized("No access to this query for the logged in user"))
        case Success((None, _))                   => Left(Results.NotFound("Could not find query in database"))
        case Failure(e)                           => Left(Results.InternalServerError("Could not load query"))
      }
    }
  }

  /** Reads a query from the database and appends it */
  case class ReadComponentFromId(app: Application, queryId: String) extends ActionRefiner[UserRequest, ComponentRequest] with DatabaseAccess {

    def refine[A](request: UserRequest[A]): Future[Either[Result, ComponentRequest[A]]] = {
      import dbConfig.driver.api._
      import models.Tables._
      import util.FutureEnhancements._

      val query = for {
        queryFuture <- runQuerySingle(PigComponents.filter(_.id === queryId))
      } yield queryFuture

      query.mapAll {
        case Success(Some(component)) => Right(ComponentRequest(component, request))
        case Success(None)            => Left(Results.NotFound("Could not find query in database"))
        case Failure(e)               => Left(Results.InternalServerError("Could not load query"))
      }
    }
  }


  /** a request that can hold a user but might be called anonymously */
  case class UserOptionalRequest[A](user: Option[User], request: Request[A]) extends WrappedRequest[A](request)
  /** a request called in a privat area where there must be a user present */
  case class UserRequest[A](user: User, request: Request[A]) extends WrappedRequest[A](request)

  case class QueryRequest[A](pigQueriesRow: PigQuery, request: UserRequest[A]) extends WrappedRequest[A](request) {
    def user = request.user
  }
  case class ComponentRequest[A](componentRow: PigComponent, request: UserRequest[A]) extends WrappedRequest[A](request) {
    def user = request.user
  }


  /** logs the user/request informations to the console */
  object LogAction extends ActionFilter[UserOptionalRequest] {
    protected def filter[B](request: UserOptionalRequest[B]): Future[Option[Result]] = {
      val user = request.user.map(_.email).getOrElse("anonymous")
      Logger.debug(s"User: $user / Request: $request")
      Future(None)
    }
  }

  /** throw an error if the user is not valid at this point */
  object UserOnlyAction extends ActionRefiner[UserOptionalRequest, UserRequest] {

    def refine[A](request: UserOptionalRequest[A]): Future[Either[Result, UserRequest[A]]] = {
      Future(if (request.user.isEmpty) {
        Left(Results.Redirect(routes.Auth.loginGET()))
      } else {
        Right(UserRequest(request.user.get, request))
      })
    }
  }


  /** checks the token from the request to the token database and either forwards a user or a none and logs errors */
  case class CheckTokenAction(app: Application) extends ActionBuilder[UserOptionalRequest] with ActionTransformer[Request, UserOptionalRequest] with LoginAccess {

    import util.FutureEnhancements._

    def transform[A](request: Request[A]) = request.cookies.get("qgtoken").map(_.value) match {
      case Some(token) => checkToken(token).mapAll {
        case Failure(e) =>
          Logger.error("Failed checking user with database: " + e.getMessage)
          UserOptionalRequest(None, request)

        case Success(None) =>
          Logger.info("Invalide token used")
          UserOptionalRequest(None, request)

        case Success(Some(x)) =>
          // Logger.debug("Successfully checked token of: " + x.email)
          UserOptionalRequest(Some(x), request)
      }

      case None =>
        Future(UserOptionalRequest(None, request))
    }
  }


  /** checks if the initial setup has been done */
  case class CheckSetupDone(app: Application) extends ActionBuilder[Request] with ActionRefiner[Request, Request] with DatabaseAccess {

    import util.FutureEnhancements._

    def refine[A](request: Request[A]): Future[Either[Result, Request[A]]] = {
      import dbConfig.driver.api._
      runQuerySingle(Users.filter(_.id === "1")).mapAll {
        case Success(value) => if (value.isEmpty) {
          Left(Results.Redirect(routes.Setup.indexGET()))
        } else {
          Right(request)
        }
        case Failure(error) => Left(Results.Redirect(routes.Setup.indexGET()))
      }
    }
  }

}