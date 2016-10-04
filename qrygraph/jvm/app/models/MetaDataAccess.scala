package models

import play.api.Logger
import prickle.Unpickle
import qrygraph.shared.SharedMessages
import qrygraph.shared.data.{Column, DataSource, PigQueryGraph, ServerComponent}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by info on 15.06.2016.
  */
trait MetaDataAccess {
  self: DatabaseAccess =>

  /* DATABASE */

  import dbConfig.driver.api._
  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  import util.FutureEnhancements._

  def loadDataSources(): List[DataSource] = {
    Await.result(runQuery(Tables.DataSources).mapAll {
      case Success(x) =>
        x.map(s => {
          val columns = Await.result(
            runQuery(Tables.DataSourcesColumns.filter(_.dataSourcesId === s.id)), 5.seconds)
              .sortBy(_.ordering).map(x => Column(x.name, x.`type`)).toList
          DataSource(s.id, s.name, s.description, s.hdfspath, columns)
        }).toList
      case Failure(e) =>
        Logger.error("Failed loading dataSources: " + e.getMessage)
        List()
    }, 20.seconds)
  }

  def loadComponents(): List[ServerComponent] = {
    implicit val nodePickler = SharedMessages.nodePickler
    Await.result(runQuery(Tables.PigComponents).mapAll {
      case Success(x) => x.map(i => {
          ServerComponent(i.id,i.name,i.description,Unpickle[PigQueryGraph].fromString(i.serializedQuerie.getOrElse("")).getOrElse(PigQueryGraph()))
        }).toList
      case Failure(e) => Logger.error("Failed loading components: " + e.getMessage); List()
    }, 20.seconds)
  }

  /** load the global setting blocking */
  val globalSetting = Await.result(runQuerySingle(Tables.GlobalSettings), 5.seconds).get

}
