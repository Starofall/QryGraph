package services

import java.net.URI
import java.security.PrivilegedAction

import models.{DBEnums, DatabaseAccess, MetaDataAccess}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.security.SaslRpcServer.AuthMethod
import org.apache.hadoop.security.UserGroupInformation
import org.apache.pig.{ExecType, PigServer}
import play.api.{Application, Logger}
import prickle.Unpickle
import qrygraph.shared.SharedMessages.PicklerImplicits
import qrygraph.shared.compilation.QueryCompiler
import qrygraph.shared.data.PigQueryGraph

import scala.concurrent.Await
import scala.concurrent.duration._

/** async execution of a pig query on the hadoop system */
class PigExecution(implicit val app: Application) extends DatabaseAccess with MetaDataAccess with PicklerImplicits {

  import dbConfig.driver.api._

  // simplifiers
  private val settings = globalSetting
  private val hdfsUrl = globalSetting.fsDefaultName
  //"hdfs://localhost:9001"
  private val qryGraphFolder = globalSetting.qrygraphFolder //"/user/qrygraph/"

  /** executes a pigQuery based on a queryId */
  def executePig(queryId: String) = {
    // load query from database
    val query = Await.result(runQuerySingle(Tables.PigQueries.filter(_.id === queryId)), 4.seconds).get

    // unserialized query
    def qrygraph = Unpickle[PigQueryGraph].fromString(query.serializedDeployedQuerie.getOrElse("")).getOrElse(PigQueryGraph.outputOnly)

    // set database to running
    db.run(Tables.PigQueries.insertOrUpdate(query.copy(executionStatus = DBEnums.RunRunning)))
    //Start executing pig query
    UserGroupInformation.createRemoteUser(settings.hadoopUser, AuthMethod.SIMPLE).doAs(new PrivilegedAction[Unit] {
      override def run(): Unit = {

        val configuration = new Configuration()
        configuration.set("dfs.client.use.datanode.hostname", "true")
        val hdfs = FileSystem.get(new URI(hdfsUrl), configuration)
        val file = new Path(hdfsUrl + s"${globalSetting.qrygraphFolder}/results/$queryId")
        if (hdfs.exists(file)) {
          Logger.info("Remove old execution folder")
          hdfs.delete(file, true)
        }

        Logger.info("Starting pig execution")
        val pigServer = new PigServer(ExecType.MAPREDUCE, PigProperties.generateProperties(settings))
        // add compiled Pig code to PigServer
        QueryCompiler
          .compile(qrygraph)
          .foreach(pigServer.registerQuery)
        pigServer.registerQuery(s"STORE storage INTO '${globalSetting.qrygraphFolder}/results/$queryId' using PigStorage(',');")
        pigServer.shutdown()
        // finish
        db.run(Tables.PigQueries.insertOrUpdate(query.copy(executionStatus = DBEnums.RunScheduled)))
      }
    })
  }
}
