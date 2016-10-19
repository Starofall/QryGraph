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

/**
  * Created by info on 19.10.2016.
  */
class PigExecution(implicit val app: Application) extends DatabaseAccess with MetaDataAccess with PicklerImplicits {

  import dbConfig.driver.api._

  val settings = globalSetting
  val hdfsUrl = globalSetting.fsDefaultName
  //"hdfs://localhost:9001"
  val qryGraphFolder = globalSetting.qrygraphFolder //"/user/qrygraph/"

  def executePig(queryId: String) = {
    val x = Await.result(runQuerySingle(Tables.PigQueries.filter(_.id === queryId)), 4.seconds).get

    def qrygraph = Unpickle[PigQueryGraph].fromString(x.serializedDeployedQuerie.getOrElse("")).getOrElse(PigQueryGraph.outputOnly)

    db.run(Tables.PigQueries.insertOrUpdate(x.copy(executionStatus = DBEnums.RunRunning)))

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
        db.run(Tables.PigQueries.insertOrUpdate(x.copy(executionStatus = DBEnums.RunScheduled)))
      }
    })
  }
}
