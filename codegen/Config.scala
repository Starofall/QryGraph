package codegen

/** The configuration for slick automatic code generation */
object Config {

  /** connection info for a pre-populated throw-away, in-memory db for this demo, which is freshly initialized on every run */
  val url = "jdbc:h2:mem:codegen;INIT=runscript from 'qrygraph/jvm/conf/evolutions/default/full.sql'"

  /** selected driver for database */
  val jdbcDriver = "org.h2.Driver"

  /** selected profile for slick * */
  val slickProfile = "slick.driver.H2Driver"

}