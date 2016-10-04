package models

import models.Tables._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by info on 15.06.2016.
  */
trait GraphAccess {
  self: DatabaseAccess =>

  /* DATABASE */

  import dbConfig.driver.api._

  def loadGraph(graphId: String): Option[PigQuery] = {
    val queryFuture = runQuerySingle(Tables.PigQueries.filter(_.id === graphId))
    Await.result(queryFuture, Duration.Inf)
  }

  def storeGraph(graphStore: PigQuery): Unit = {
    val insert = Tables.PigQueries.insertOrUpdate(graphStore)
    db.run(insert)
  }
}
