package codegen

import slick.model.Model
import slick.driver.H2Driver
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * This customizes the Slick code generator. We only do simple name mappings.
  * For a more advanced example see https://github.com/cvogt/slick-presentation/tree/scala-exchange-2013
  */
object CustomizedCodeGenerator {
  def main(args: Array[String]): Unit = {
    Await.ready(
      codegen.map(_.writeToFile(
        Config.slickProfile,
        args(0),
        "models",
        "Tables",
        "Tables.scala"
      )),
      1400.seconds // just to be sure
    )
  }

  /** db based on config */
  val db = H2Driver.api.Database.forURL(Config.url, driver = Config.jdbcDriver)

  /** code generation function*/
  val codegen = db.run(H2Driver.createModel(Some(H2Driver.defaultTables))).map { model =>
    new slick.codegen.SourceCodeGenerator(model) {
      // customize Scala entity name (case class, etc.)
      override def entityName = dbTableName => {
        dbTableName match {
          case "PIG_QUERIES" => "PigQuery"
          case "PIG_COMPONENTS" => "PigComponent"
          case dbTableName   => dbTableName.dropRight(1).toLowerCase.toCamelCase
        }
      }

      // customize Scala table name (table class, table values, ...)
      override def tableName = dbTableName => dbTableName match {
        //        case "COF_INVENTORY" => "CoffeeInventory"
        case dbTableName => dbTableName.toLowerCase.toCamelCase
      }
    }
  }
}
