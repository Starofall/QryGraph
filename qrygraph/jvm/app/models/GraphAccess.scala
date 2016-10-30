package models

import models.Tables._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/** injects access to graph on the database  */
trait GraphAccess {
  self: DatabaseAccess =>

  import dbConfig.driver.api._

  /** loads a pigquery based on the graphId */
  def loadGraph(graphId: String): Option[PigQuery] = {
    val queryFuture = runQuerySingle(Tables.PigQueries.filter(_.id === graphId))
    Await.result(queryFuture, Duration.Inf)
  }

  /** stores a graph in the database */
  def storeGraph(graphStore: PigQuery): Unit = {
    val insert = Tables.PigQueries.insertOrUpdate(graphStore)
    db.run(insert)
  }
}
