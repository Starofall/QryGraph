package models

import models.Tables._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by info on 15.06.2016.
  */
trait ComponentAccess {
  self: DatabaseAccess =>

  /* DATABASE */

  import dbConfig.driver.api._

  def loadComponent(graphId: String): Option[PigComponent] = {
    val queryFuture = runQuerySingle(Tables.PigComponents.filter(_.id === graphId))
    Await.result(queryFuture, Duration.Inf)
  }

  def storeComponent(graphStore: PigComponent): Unit = {
    val insert = Tables.PigComponents.insertOrUpdate(graphStore)
    db.run(insert)
  }
}
