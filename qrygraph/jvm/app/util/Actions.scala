package util

import akka.stream.Materializer
import controllers.routes
import models.Tables._
import models.{DBEnums, DatabaseAccess, LoginAccess}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{ActionTransformer, _}
import play.api.routing.Router.Tags
import play.api.{Application, Logger}

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.{Failure, Success}

/**
  * Created by info on 11.07.2016.
  */
object Actions {

  /** A action for a public available url with user checking and logging */
  def PublicAction(app: Application): ActionBuilder[UserOptionalRequest] = {
    CheckSetup(app) andThen CheckTokenAction(app) andThen LogAction
  }

  /** a private action only available to logged in users */
  def AuthedAction(implicit app: Application) = {
    CheckSetup(app) andThen CheckTokenAction(app) andThen LogAction andThen UserOnlyAction
  }

  /** a private action only available to logged in users */
  def AdminAction(implicit app: Application) = {
    CheckSetup(app) andThen CheckTokenAction(app) andThen LogAction andThen UserOnlyAction andThen AdminOnlyAction
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
  case class ReadDBUserFromId(app: Application, userId: String) extends ActionRefiner[UserRequest, DBUserRequest] with DatabaseAccess {

    def refine[A](request: UserRequest[A]): Future[Either[Result, DBUserRequest[A]]] = {
      import dbConfig.driver.api._
      import models.Tables._
      import util.FutureEnhancements._

      runQuerySingle(Users.filter(_.id === userId)).mapAll {
        case Success(Some(dbUser)) if request.user.userRole == DBEnums.RoleAdmin => Right(DBUserRequest(dbUser, request))
        case Success(Some(dbUser))                                               => Left(Results.Unauthorized("No access to this query for the logged in user"))
        case Success(None)                                                       => Left(Results.NotFound("Could not find query in database"))
        case Failure(e)                                                          => Left(Results.InternalServerError("Could not load query"))
      }
    }
  }
  /** Reads a query from the database and appends it */
  case class ReadLoadSourceFromId(app: Application, loadSourceId: String) extends ActionRefiner[UserRequest, DataSourceRequest] with DatabaseAccess {
    def refine[A](request: UserRequest[A]): Future[Either[Result, DataSourceRequest[A]]] = {
      import dbConfig.driver.api._
      import util.FutureEnhancements._

      runQuerySingle(Tables.DataSources.filter(_.id === loadSourceId)).mapAll {
        case Success(Some(loadSource)) if request.user.userRole == DBEnums.RoleAdmin => Right(DataSourceRequest(loadSource, request))
        case Success(Some(loadSource))                                               => Left(Results.Unauthorized("No access to this query for the logged in user"))
        case Success(None)                                                           => Left(Results.NotFound("Could not find query in database"))
        case Failure(e)                                                              => Left(Results.InternalServerError("Could not load query"))
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
  /** request with a dataSource */
  case class DataSourceRequest[A](dataSource: DataSource, request: UserRequest[A]) extends WrappedRequest[A](request) {
    def user = request.user
  }
  /** request with a dbUser */
  case class DBUserRequest[A](dbUser: User, request: UserRequest[A]) extends WrappedRequest[A](request) {
    def user = request.user
  }
  /** request with a query */
  case class QueryRequest[A](pigQueriesRow: PigQuery, request: UserRequest[A]) extends WrappedRequest[A](request) {
    def user = request.user
  }
  /** request with a component */
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
  /** logs the user/request informations to the console */
  object AdminOnlyAction extends ActionFilter[UserRequest] {
    protected def filter[B](request: UserRequest[B]): Future[Option[Result]] = {
      request.user.userRole match {
        case DBEnums.RoleAdmin => Future(None)
        case DBEnums.RoleUser  => Future(Some(Results.Redirect(routes.Auth.loginGET())))
        case _                 => Future(Some(Results.Redirect(routes.Auth.loginGET())))
      }
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
  case class CheckSetup(app: Application, continueOnDone: Boolean = true) extends ActionBuilder[Request] with ActionRefiner[Request, Request] with DatabaseAccess {

    import util.FutureEnhancements._

    def refine[A](request: Request[A]): Future[Either[Result, Request[A]]] = {
      import dbConfig.driver.api._
      continueOnDone match {
        case true =>
          // aka process if config is set, else goto setup
          runQuerySingle(Users.filter(_.id === "1")).mapAll {
            case Success(Some(value)) => Right(request)
            case _                    => Left(Results.Redirect(routes.Setup.indexGET()))
          }

        case false =>
          // aka process if config is NOT set, else goto setup
          runQuerySingle(Users.filter(_.id === "1")).mapAll {
            case Success(Some(value)) => Left(Results.Redirect(routes.Queries.indexGET()))
            case _                    => Right(request)
          }
      }
    }
  }

}
