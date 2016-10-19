package models

import models.Tables.User
import play.api.Logger
import prickle.Unpickle
import qrygraph.shared.SharedMessages
import qrygraph.shared.data._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by info on 15.06.2016.
  */
trait MetaDataAccess {
  self: DatabaseAccess =>

  /* DATABASE */

  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  import util.FutureEnhancements._

  /** we transform the backend stored source into a QueryLoadSource so that the client can use it  */
  def loadDataSources(): List[QueryLoadSource] = {
    Await.result(runQuery(Tables.DataSources).mapAll {
      case Success(x) => x.map(s => QueryLoadSource(s.id, s.name, s.description, s.loadcommand)).toList
      case Failure(e) => Logger.error("Failed loading dataSources: " + e.getMessage); List()
    }, 20.seconds)
  }

  /** we transform the backend stored source into a QueryLoadSource so that the client can use it  */
  def loadUsers(): List[User] = {
    Await.result(runQuery(Tables.Users).mapAll {
      case Success(x) => x.toList
      case Failure(e) => Logger.error("Failed loading dataSources: " + e.getMessage); List()
    }, 20.seconds)
  }

  def loadPublishedComponents(): List[ServerComponent] = {
    import dbConfig.driver.api._
    implicit val nodePickler = SharedMessages.nodePickler
    Await.result(runQuery(Tables.PigComponents.filter(_.published === true)).mapAll {
      case Success(x) => x.map(i => {
        ServerComponent(i.id, i.name, i.description, Unpickle[PigQueryGraph].fromString(i.serializedQuerie.getOrElse("")).getOrElse(PigQueryGraph()))
      }).toList
      case Failure(e) => Logger.error("Failed loading components: " + e.getMessage); List()
    }, 20.seconds)
  }

  /** load the global setting blocking */
  val globalSetting = Await.result(runQuerySingle(Tables.GlobalSettings), 5.seconds).get

}
