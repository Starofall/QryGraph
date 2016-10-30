package models

import play.api.db.slick.DatabaseConfigProvider
import slick.backend.DatabaseConfig
import slick.dbio
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend
import util.RuntimeRef

/** an abstraction for a controller that is using slick database */
trait DatabaseAccess extends RuntimeRef {

  val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfigProvider.get[JdbcProfile](this.app)
  implicit val myConfig: JdbcBackend#DatabaseDef = dbConfig.db

  import dbConfig.driver.api._

  val Tables = models.Tables

  val db: JdbcBackend#DatabaseDef = dbConfig.db

  import scala.language.higherKinds

  def newUUID() = java.util.UUID.randomUUID.toString

  def runQuery[F, T, D[_]](q: slick.lifted.Query[F, T, D]) = db.run(q.result)

  def runQuerySingle[F, T, D[_]](q: slick.lifted.Query[F, T, D]) = db.run(q.result.headOption)

  def runInsert[R, E <: Effect](q: dbio.DBIOAction[R, NoStream, E]) = db.run(q)

}
