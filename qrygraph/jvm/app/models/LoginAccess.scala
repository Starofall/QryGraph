package models

import scala.concurrent.Future
import scala.util.{Failure, Success}
import org.mindrot.jbcrypt.BCrypt

/**
  * Created by info on 15.06.2016.
  */
trait LoginAccess extends DatabaseAccess{

  // database import
  import Tables._
  import dbConfig.driver.api._

  // enhance future
  import util.FutureEnhancements.FutureExtensions
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  /** adds a new user to the database */
  def addUser(email: String, password: String,firstName:String,lastName:String): Future[Either[String, Unit]] = {
    val newUser = User(newUUID(), email.toLowerCase(), BCrypt.hashpw(password, BCrypt.gensalt()), firstName, lastName, "user", None)
    runInsert(Users += newUser).mapAll {
      case Success(s) => Right(())
      case Failure(e) => Left("Username taken")
    }
  }

  /** try the login of a user using BCrypt */
  def tryLogin(email: String, password: String): Future[Option[(User, UserToken)]] = {
    runQuerySingle(Users.filter(_.email === email.toLowerCase)).map {
      case Some(user) => BCrypt.checkpw(password, user.password) match {
        case true  =>
          val token = UserToken(newUUID(), user.id, None)
          runInsert(UserTokens += token)
          Some(user, token)
        case false => None
      }
      case None       => None
    }
  }

  def checkToken(token: String): Future[Option[User]] = {
    val registeredUser = for {
      userId <- UserTokens.filter(_.token === token).map(_.userId)
      u <- Users.filter(_.id === userId)
    } yield u

    runQuerySingle(registeredUser)
  }
}
