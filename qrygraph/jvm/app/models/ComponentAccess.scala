package models

import models.Tables._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/** injects access to components */
trait ComponentAccess {
  self: DatabaseAccess =>

  import dbConfig.driver.api._

  /** returns the component for the given id */
  def loadComponent(graphId: String): Option[PigComponent] = {
    val queryFuture = runQuerySingle(Tables.PigComponents.filter(_.id === graphId))
    Await.result(queryFuture, Duration.Inf)
  }

  /** stores a pigcomponent in the database */
  def storeComponent(graphStore: PigComponent): Unit = {
    val insert = Tables.PigComponents.insertOrUpdate(graphStore)
    db.run(insert)
  }
}
