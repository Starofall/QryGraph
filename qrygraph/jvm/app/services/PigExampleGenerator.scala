package services

import java.security.PrivilegedAction

import models.Tables.GlobalSetting
import org.apache.hadoop.security.SaslRpcServer.AuthMethod
import org.apache.hadoop.security.UserGroupInformation
import org.apache.pig.impl.logicalLayer.FrontendException
import org.apache.pig.{ExecType, PigServer}
import play.api.Logger
import qrygraph.shared.compilation.{GraphFlatten, QueryCompiler}
import qrygraph.shared.data._

import scala.collection.JavaConversions._
import scala.concurrent.{Future, Promise}


/** The PigExecution object handle the execution of a qrygraph on the Hadoop cluster */
object PigExampleGenerator {

  /** generates examples of a given pigQuery */
  def generateExamples(settings: GlobalSetting, qrygraph: PigQueryGraph): Future[Map[String, List[List[String]]]] = {
    Logger.info(s"Using setting for execution of Query: $settings")

    val promise = Promise[Map[String, List[List[String]]]]()
    UserGroupInformation.createRemoteUser(settings.hadoopUser, AuthMethod.SIMPLE).doAs(new PrivilegedAction[Unit] {
      override def run(): Unit = {
        Logger.error("Starting example generation")
        // create an empty map to store the resultTypes in
        var map = Map[String, List[List[String]]]()
        // create a pig server
        val pigServer = new PigServer(ExecType.MAPREDUCE, PigProperties.generateProperties(settings))
        // add compiled Pig code to PigServer
        QueryCompiler
          .compile(qrygraph)
          .foreach(pigServer.registerQuery)
        // calc the type for each node
        for (node <- GraphFlatten.flattenDependency(qrygraph)) {
          try {
            // load data from pigServer

            val x = pigServer.openIterator(node.name).toStream.take(20).toList
            val resultFormat = x.map(_.toList.map(_.toString))
            map += node.name -> resultFormat

          } catch {
            case f: FrontendException =>
              println(f.getMessage)
              f.printStackTrace()
            case e: Throwable         =>
              println(e.getMessage)
              e.printStackTrace()
          }
        }
        pigServer.shutdown()
        promise.success(map)
      }
    })
    promise.future
  }

}
