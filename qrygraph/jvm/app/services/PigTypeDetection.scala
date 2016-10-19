package services

import models.Tables.GlobalSetting
import org.apache.pig.impl.logicalLayer.FrontendException
import org.apache.pig.impl.logicalLayer.schema.Schema
import org.apache.pig.impl.plan.PlanValidationException
import org.apache.pig.{ExecType, PigServer}
import play.api.Logger
import qrygraph.shared.compilation.QueryCompiler
import qrygraph.shared.data._
import qrygraph.shared.pig._

import scala.collection.JavaConversions._

/**
  * here are functions that are used to determine the type of a query using the Pig toolSet
  */
object PigTypeDetection {

  /** this function returns the types of all aliases in the query */
  def evaluateTypes(settings: GlobalSetting,graph: PigQueryGraph): (Map[String, ResultType], Map[String, String]) = {
    Logger.info("Starting type evaluation")

    // On deployment, set the correct home dir
    if (!System.getProperty("user.dir").contains(":")) {
      Logger.info("Old user.dir: " + System.getProperty("user.dir"))
      System.setProperty("user.dir", "/app/")
      Logger.info("New user.dir: " + System.getProperty("user.dir"))
    }

    // create a new pigServer
    val pigServer = new PigServer(ExecType.LOCAL, PigProperties.generateProperties(settings))
    // create an empty map to store the resultTypes in
    var typesMap = Map[String, ResultType]()
    // here we store the errors
    var errorsMap = Map[String, String]()
    // remove components
    QueryCompiler
      .advancedCompile(graph)
      .foreach { case (node, pigCode) =>
        try {
          pigServer.registerQuery(pigCode)
        } catch {
          case f: FrontendException =>
            Logger.info(s"Error on node ${node.name} with msg: ${f.getCause.getMessage}")
            errorsMap += node.name -> f.getCause.getMessage
          case e: Throwable         =>
          //            e.printStackTrace()
        }
      }

    pigServer.getAliasKeySet.toList.foreach(alias => {
      try {
        val schema = pigServer.dumpSchema(alias)
        typesMap += alias -> schemaToScalaSchema(schema)
      } catch {
        case f: PlanValidationException =>
          Logger.info(s"Error on node ${alias} with msg: ${f.getCause.getMessage}")
          errorsMap += alias -> f.getCause.getMessage
        case e: Throwable               =>
          errorsMap += alias -> e.getCause.getMessage
        //            e.printStackTrace()
      }

    })

    // shutdown pigServer
    pigServer.shutdown()
    // return map
    (typesMap, errorsMap)
  }

  /** pig returns us a java version of a schema, we convert it to our internal schema representation */
  def schemaToScalaSchema(schema: Schema): ResultType = {
    import scala.collection.JavaConversions._
    val fields = schema.getFields.toList

    val scalaFields = fields.map(f => {
      // create scalaField for each
      f.`type` match {
        case 10 => PField(f.alias, Primitives.PInt)
        case 15 => PField(f.alias, Primitives.PLong)
        case 20 => PField(f.alias, Primitives.PFloat)
        case 25 => PField(f.alias, Primitives.PDouble)
        case 50 => PField(f.alias, Primitives.PByteArray)
        case 55 => PField(f.alias, Primitives.PCharArray)
        //@todo integrate
        //        case 100 => PField(f.alias, PMap(schemaToScalaSchema(f.schema))
        case 110 => PField(f.alias, PBag(schemaToScalaSchema(f.schema).fields))
        case 120 => PField(f.alias, PTuple(schemaToScalaSchema(f.schema).fields))
        case _   => PField(f.alias, Primitives.PCharArray)
      }
    })
    ResultType("dummy", scalaFields)
  }

}

