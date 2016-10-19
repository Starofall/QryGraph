package models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.driver.H2Driver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.driver.JdbcProfile
  import profile.api._
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Array(DataSources.schema, GlobalSettings.schema, PigComponents.schema, PigQueries.schema, QueryAccessRights.schema, QueryExecutions.schema, Users.schema, UserTokens.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table DataSources
   *  @param id Database column ID SqlType(VARCHAR), PrimaryKey, Length(36,true)
   *  @param name Database column NAME SqlType(VARCHAR), Length(255,true)
   *  @param description Database column DESCRIPTION SqlType(VARCHAR), Length(255,true)
   *  @param loadcommand Database column LOADCOMMAND SqlType(VARCHAR), Length(1024,true) */
  case class DataSource(id: String, name: String, description: String, loadcommand: String)
  /** GetResult implicit for fetching DataSource objects using plain SQL queries */
  implicit def GetResultDataSource(implicit e0: GR[String]): GR[DataSource] = GR{
    prs => import prs._
    DataSource.tupled((<<[String], <<[String], <<[String], <<[String]))
  }
  /** Table description of table DATA_SOURCES. Objects of this class serve as prototypes for rows in queries. */
  class DataSources(_tableTag: Tag) extends Table[DataSource](_tableTag, "DATA_SOURCES") {
    def * = (id, name, description, loadcommand) <> (DataSource.tupled, DataSource.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name), Rep.Some(description), Rep.Some(loadcommand)).shaped.<>({r=>import r._; _1.map(_=> DataSource.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(VARCHAR), PrimaryKey, Length(36,true) */
    val id: Rep[String] = column[String]("ID", O.PrimaryKey, O.Length(36,varying=true))
    /** Database column NAME SqlType(VARCHAR), Length(255,true) */
    val name: Rep[String] = column[String]("NAME", O.Length(255,varying=true))
    /** Database column DESCRIPTION SqlType(VARCHAR), Length(255,true) */
    val description: Rep[String] = column[String]("DESCRIPTION", O.Length(255,varying=true))
    /** Database column LOADCOMMAND SqlType(VARCHAR), Length(1024,true) */
    val loadcommand: Rep[String] = column[String]("LOADCOMMAND", O.Length(1024,varying=true))
  }
  /** Collection-like TableQuery object for table DataSources */
  lazy val DataSources = new TableQuery(tag => new DataSources(tag))

  /** Entity class storing rows of table GlobalSettings
   *  @param id Database column ID SqlType(VARCHAR), PrimaryKey, Length(36,true)
   *  @param hadoopUser Database column HADOOP_USER SqlType(VARCHAR), Length(36,true)
   *  @param qrygraphFolder Database column QRYGRAPH_FOLDER SqlType(VARCHAR), Length(1023,true)
   *  @param fsDefaultName Database column FS_DEFAULT_NAME SqlType(VARCHAR), Length(1023,true)
   *  @param mapredJobTracker Database column MAPRED_JOB_TRACKER SqlType(VARCHAR), Length(1023,true) */
  case class GlobalSetting(id: String, hadoopUser: String, qrygraphFolder: String, fsDefaultName: String, mapredJobTracker: String)
  /** GetResult implicit for fetching GlobalSetting objects using plain SQL queries */
  implicit def GetResultGlobalSetting(implicit e0: GR[String]): GR[GlobalSetting] = GR{
    prs => import prs._
    GlobalSetting.tupled((<<[String], <<[String], <<[String], <<[String], <<[String]))
  }
  /** Table description of table GLOBAL_SETTINGS. Objects of this class serve as prototypes for rows in queries. */
  class GlobalSettings(_tableTag: Tag) extends Table[GlobalSetting](_tableTag, "GLOBAL_SETTINGS") {
    def * = (id, hadoopUser, qrygraphFolder, fsDefaultName, mapredJobTracker) <> (GlobalSetting.tupled, GlobalSetting.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(hadoopUser), Rep.Some(qrygraphFolder), Rep.Some(fsDefaultName), Rep.Some(mapredJobTracker)).shaped.<>({r=>import r._; _1.map(_=> GlobalSetting.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(VARCHAR), PrimaryKey, Length(36,true) */
    val id: Rep[String] = column[String]("ID", O.PrimaryKey, O.Length(36,varying=true))
    /** Database column HADOOP_USER SqlType(VARCHAR), Length(36,true) */
    val hadoopUser: Rep[String] = column[String]("HADOOP_USER", O.Length(36,varying=true))
    /** Database column QRYGRAPH_FOLDER SqlType(VARCHAR), Length(1023,true) */
    val qrygraphFolder: Rep[String] = column[String]("QRYGRAPH_FOLDER", O.Length(1023,varying=true))
    /** Database column FS_DEFAULT_NAME SqlType(VARCHAR), Length(1023,true) */
    val fsDefaultName: Rep[String] = column[String]("FS_DEFAULT_NAME", O.Length(1023,varying=true))
    /** Database column MAPRED_JOB_TRACKER SqlType(VARCHAR), Length(1023,true) */
    val mapredJobTracker: Rep[String] = column[String]("MAPRED_JOB_TRACKER", O.Length(1023,varying=true))
  }
  /** Collection-like TableQuery object for table GlobalSettings */
  lazy val GlobalSettings = new TableQuery(tag => new GlobalSettings(tag))

  /** Entity class storing rows of table PigComponents
   *  @param id Database column ID SqlType(VARCHAR), PrimaryKey, Length(36,true)
   *  @param name Database column NAME SqlType(VARCHAR), Length(45,true)
   *  @param description Database column DESCRIPTION SqlType(VARCHAR), Length(255,true)
   *  @param serializedQuerie Database column SERIALIZED_QUERIE SqlType(VARCHAR)
   *  @param published Database column PUBLISHED SqlType(BOOLEAN)
   *  @param creatorUserId Database column CREATOR_USER_ID SqlType(VARCHAR), Length(36,true) */
  case class PigComponent(id: String, name: String, description: String, serializedQuerie: Option[String], published: Boolean, creatorUserId: String)
  /** GetResult implicit for fetching PigComponent objects using plain SQL queries */
  implicit def GetResultPigComponent(implicit e0: GR[String], e1: GR[Option[String]], e2: GR[Boolean]): GR[PigComponent] = GR{
    prs => import prs._
    PigComponent.tupled((<<[String], <<[String], <<[String], <<?[String], <<[Boolean], <<[String]))
  }
  /** Table description of table PIG_COMPONENTS. Objects of this class serve as prototypes for rows in queries. */
  class PigComponents(_tableTag: Tag) extends Table[PigComponent](_tableTag, "PIG_COMPONENTS") {
    def * = (id, name, description, serializedQuerie, published, creatorUserId) <> (PigComponent.tupled, PigComponent.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name), Rep.Some(description), serializedQuerie, Rep.Some(published), Rep.Some(creatorUserId)).shaped.<>({r=>import r._; _1.map(_=> PigComponent.tupled((_1.get, _2.get, _3.get, _4, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(VARCHAR), PrimaryKey, Length(36,true) */
    val id: Rep[String] = column[String]("ID", O.PrimaryKey, O.Length(36,varying=true))
    /** Database column NAME SqlType(VARCHAR), Length(45,true) */
    val name: Rep[String] = column[String]("NAME", O.Length(45,varying=true))
    /** Database column DESCRIPTION SqlType(VARCHAR), Length(255,true) */
    val description: Rep[String] = column[String]("DESCRIPTION", O.Length(255,varying=true))
    /** Database column SERIALIZED_QUERIE SqlType(VARCHAR) */
    val serializedQuerie: Rep[Option[String]] = column[Option[String]]("SERIALIZED_QUERIE")
    /** Database column PUBLISHED SqlType(BOOLEAN) */
    val published: Rep[Boolean] = column[Boolean]("PUBLISHED")
    /** Database column CREATOR_USER_ID SqlType(VARCHAR), Length(36,true) */
    val creatorUserId: Rep[String] = column[String]("CREATOR_USER_ID", O.Length(36,varying=true))
  }
  /** Collection-like TableQuery object for table PigComponents */
  lazy val PigComponents = new TableQuery(tag => new PigComponents(tag))

  /** Entity class storing rows of table PigQueries
   *  @param id Database column ID SqlType(VARCHAR), PrimaryKey, Length(36,true)
   *  @param name Database column NAME SqlType(VARCHAR), Length(45,true)
   *  @param description Database column DESCRIPTION SqlType(VARCHAR), Length(255,true)
   *  @param serializedDraftQuerie Database column SERIALIZED_DRAFT_QUERIE SqlType(VARCHAR)
   *  @param serializedDeployedQuerie Database column SERIALIZED_DEPLOYED_QUERIE SqlType(VARCHAR)
   *  @param undeployedChanges Database column UNDEPLOYED_CHANGES SqlType(BOOLEAN)
   *  @param creatorUserId Database column CREATOR_USER_ID SqlType(VARCHAR), Length(36,true)
   *  @param authorizationStatus Database column AUTHORIZATION_STATUS SqlType(VARCHAR), Length(45,true)
   *  @param executionStatus Database column EXECUTION_STATUS SqlType(VARCHAR), Length(45,true)
   *  @param cronjob Database column CRONJOB SqlType(VARCHAR), Length(45,true) */
  case class PigQuery(id: String, name: String, description: String, serializedDraftQuerie: Option[String], serializedDeployedQuerie: Option[String], undeployedChanges: Boolean, creatorUserId: String, authorizationStatus: String, executionStatus: String, cronjob: String)
  /** GetResult implicit for fetching PigQuery objects using plain SQL queries */
  implicit def GetResultPigQuery(implicit e0: GR[String], e1: GR[Option[String]], e2: GR[Boolean]): GR[PigQuery] = GR{
    prs => import prs._
    PigQuery.tupled((<<[String], <<[String], <<[String], <<?[String], <<?[String], <<[Boolean], <<[String], <<[String], <<[String], <<[String]))
  }
  /** Table description of table PIG_QUERIES. Objects of this class serve as prototypes for rows in queries. */
  class PigQueries(_tableTag: Tag) extends Table[PigQuery](_tableTag, "PIG_QUERIES") {
    def * = (id, name, description, serializedDraftQuerie, serializedDeployedQuerie, undeployedChanges, creatorUserId, authorizationStatus, executionStatus, cronjob) <> (PigQuery.tupled, PigQuery.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name), Rep.Some(description), serializedDraftQuerie, serializedDeployedQuerie, Rep.Some(undeployedChanges), Rep.Some(creatorUserId), Rep.Some(authorizationStatus), Rep.Some(executionStatus), Rep.Some(cronjob)).shaped.<>({r=>import r._; _1.map(_=> PigQuery.tupled((_1.get, _2.get, _3.get, _4, _5, _6.get, _7.get, _8.get, _9.get, _10.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(VARCHAR), PrimaryKey, Length(36,true) */
    val id: Rep[String] = column[String]("ID", O.PrimaryKey, O.Length(36,varying=true))
    /** Database column NAME SqlType(VARCHAR), Length(45,true) */
    val name: Rep[String] = column[String]("NAME", O.Length(45,varying=true))
    /** Database column DESCRIPTION SqlType(VARCHAR), Length(255,true) */
    val description: Rep[String] = column[String]("DESCRIPTION", O.Length(255,varying=true))
    /** Database column SERIALIZED_DRAFT_QUERIE SqlType(VARCHAR) */
    val serializedDraftQuerie: Rep[Option[String]] = column[Option[String]]("SERIALIZED_DRAFT_QUERIE")
    /** Database column SERIALIZED_DEPLOYED_QUERIE SqlType(VARCHAR) */
    val serializedDeployedQuerie: Rep[Option[String]] = column[Option[String]]("SERIALIZED_DEPLOYED_QUERIE")
    /** Database column UNDEPLOYED_CHANGES SqlType(BOOLEAN) */
    val undeployedChanges: Rep[Boolean] = column[Boolean]("UNDEPLOYED_CHANGES")
    /** Database column CREATOR_USER_ID SqlType(VARCHAR), Length(36,true) */
    val creatorUserId: Rep[String] = column[String]("CREATOR_USER_ID", O.Length(36,varying=true))
    /** Database column AUTHORIZATION_STATUS SqlType(VARCHAR), Length(45,true) */
    val authorizationStatus: Rep[String] = column[String]("AUTHORIZATION_STATUS", O.Length(45,varying=true))
    /** Database column EXECUTION_STATUS SqlType(VARCHAR), Length(45,true) */
    val executionStatus: Rep[String] = column[String]("EXECUTION_STATUS", O.Length(45,varying=true))
    /** Database column CRONJOB SqlType(VARCHAR), Length(45,true) */
    val cronjob: Rep[String] = column[String]("CRONJOB", O.Length(45,varying=true))
  }
  /** Collection-like TableQuery object for table PigQueries */
  lazy val PigQueries = new TableQuery(tag => new PigQueries(tag))

  /** Entity class storing rows of table QueryAccessRights
   *  @param id Database column ID SqlType(VARCHAR), PrimaryKey, Length(36,true)
   *  @param rightLevel Database column RIGHT_LEVEL SqlType(VARCHAR), Length(45,true)
   *  @param userId Database column USER_ID SqlType(VARCHAR), Length(36,true)
   *  @param queryId Database column QUERY_ID SqlType(VARCHAR), Length(36,true) */
  case class QueryAccessRight(id: String, rightLevel: Option[String], userId: String, queryId: String)
  /** GetResult implicit for fetching QueryAccessRight objects using plain SQL queries */
  implicit def GetResultQueryAccessRight(implicit e0: GR[String], e1: GR[Option[String]]): GR[QueryAccessRight] = GR{
    prs => import prs._
    QueryAccessRight.tupled((<<[String], <<?[String], <<[String], <<[String]))
  }
  /** Table description of table QUERY_ACCESS_RIGHTS. Objects of this class serve as prototypes for rows in queries. */
  class QueryAccessRights(_tableTag: Tag) extends Table[QueryAccessRight](_tableTag, "QUERY_ACCESS_RIGHTS") {
    def * = (id, rightLevel, userId, queryId) <> (QueryAccessRight.tupled, QueryAccessRight.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), rightLevel, Rep.Some(userId), Rep.Some(queryId)).shaped.<>({r=>import r._; _1.map(_=> QueryAccessRight.tupled((_1.get, _2, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(VARCHAR), PrimaryKey, Length(36,true) */
    val id: Rep[String] = column[String]("ID", O.PrimaryKey, O.Length(36,varying=true))
    /** Database column RIGHT_LEVEL SqlType(VARCHAR), Length(45,true) */
    val rightLevel: Rep[Option[String]] = column[Option[String]]("RIGHT_LEVEL", O.Length(45,varying=true))
    /** Database column USER_ID SqlType(VARCHAR), Length(36,true) */
    val userId: Rep[String] = column[String]("USER_ID", O.Length(36,varying=true))
    /** Database column QUERY_ID SqlType(VARCHAR), Length(36,true) */
    val queryId: Rep[String] = column[String]("QUERY_ID", O.Length(36,varying=true))
  }
  /** Collection-like TableQuery object for table QueryAccessRights */
  lazy val QueryAccessRights = new TableQuery(tag => new QueryAccessRights(tag))

  /** Entity class storing rows of table QueryExecutions
   *  @param id Database column ID SqlType(VARCHAR), PrimaryKey, Length(36,true)
   *  @param resultStatus Database column RESULT_STATUS SqlType(VARCHAR), Length(45,true)
   *  @param queriesId Database column QUERIES_ID SqlType(VARCHAR), Length(36,true)
   *  @param resultLog Database column RESULT_LOG SqlType(VARCHAR)
   *  @param executionTime Database column EXECUTION_TIME SqlType(TIMESTAMP) */
  case class QueryExecution(id: String, resultStatus: String, queriesId: String, resultLog: Option[String], executionTime: Option[java.sql.Timestamp])
  /** GetResult implicit for fetching QueryExecution objects using plain SQL queries */
  implicit def GetResultQueryExecution(implicit e0: GR[String], e1: GR[Option[String]], e2: GR[Option[java.sql.Timestamp]]): GR[QueryExecution] = GR{
    prs => import prs._
    QueryExecution.tupled((<<[String], <<[String], <<[String], <<?[String], <<?[java.sql.Timestamp]))
  }
  /** Table description of table QUERY_EXECUTIONS. Objects of this class serve as prototypes for rows in queries. */
  class QueryExecutions(_tableTag: Tag) extends Table[QueryExecution](_tableTag, "QUERY_EXECUTIONS") {
    def * = (id, resultStatus, queriesId, resultLog, executionTime) <> (QueryExecution.tupled, QueryExecution.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(resultStatus), Rep.Some(queriesId), resultLog, executionTime).shaped.<>({r=>import r._; _1.map(_=> QueryExecution.tupled((_1.get, _2.get, _3.get, _4, _5)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(VARCHAR), PrimaryKey, Length(36,true) */
    val id: Rep[String] = column[String]("ID", O.PrimaryKey, O.Length(36,varying=true))
    /** Database column RESULT_STATUS SqlType(VARCHAR), Length(45,true) */
    val resultStatus: Rep[String] = column[String]("RESULT_STATUS", O.Length(45,varying=true))
    /** Database column QUERIES_ID SqlType(VARCHAR), Length(36,true) */
    val queriesId: Rep[String] = column[String]("QUERIES_ID", O.Length(36,varying=true))
    /** Database column RESULT_LOG SqlType(VARCHAR) */
    val resultLog: Rep[Option[String]] = column[Option[String]]("RESULT_LOG")
    /** Database column EXECUTION_TIME SqlType(TIMESTAMP) */
    val executionTime: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("EXECUTION_TIME")
  }
  /** Collection-like TableQuery object for table QueryExecutions */
  lazy val QueryExecutions = new TableQuery(tag => new QueryExecutions(tag))

  /** Entity class storing rows of table Users
   *  @param id Database column ID SqlType(VARCHAR), PrimaryKey, Length(36,true)
   *  @param email Database column EMAIL SqlType(VARCHAR), Length(255,true)
   *  @param password Database column PASSWORD SqlType(VARCHAR), Length(255,true)
   *  @param firstName Database column FIRST_NAME SqlType(VARCHAR), Length(45,true)
   *  @param lastName Database column LAST_NAME SqlType(VARCHAR), Length(45,true)
   *  @param userRole Database column USER_ROLE SqlType(VARCHAR), Length(45,true)
   *  @param createTime Database column CREATE_TIME SqlType(TIMESTAMP) */
  case class User(id: String, email: String, password: String, firstName: String, lastName: String, userRole: String, createTime: Option[java.sql.Timestamp])
  /** GetResult implicit for fetching User objects using plain SQL queries */
  implicit def GetResultUser(implicit e0: GR[String], e1: GR[Option[java.sql.Timestamp]]): GR[User] = GR{
    prs => import prs._
    User.tupled((<<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<?[java.sql.Timestamp]))
  }
  /** Table description of table USERS. Objects of this class serve as prototypes for rows in queries. */
  class Users(_tableTag: Tag) extends Table[User](_tableTag, "USERS") {
    def * = (id, email, password, firstName, lastName, userRole, createTime) <> (User.tupled, User.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(email), Rep.Some(password), Rep.Some(firstName), Rep.Some(lastName), Rep.Some(userRole), createTime).shaped.<>({r=>import r._; _1.map(_=> User.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(VARCHAR), PrimaryKey, Length(36,true) */
    val id: Rep[String] = column[String]("ID", O.PrimaryKey, O.Length(36,varying=true))
    /** Database column EMAIL SqlType(VARCHAR), Length(255,true) */
    val email: Rep[String] = column[String]("EMAIL", O.Length(255,varying=true))
    /** Database column PASSWORD SqlType(VARCHAR), Length(255,true) */
    val password: Rep[String] = column[String]("PASSWORD", O.Length(255,varying=true))
    /** Database column FIRST_NAME SqlType(VARCHAR), Length(45,true) */
    val firstName: Rep[String] = column[String]("FIRST_NAME", O.Length(45,varying=true))
    /** Database column LAST_NAME SqlType(VARCHAR), Length(45,true) */
    val lastName: Rep[String] = column[String]("LAST_NAME", O.Length(45,varying=true))
    /** Database column USER_ROLE SqlType(VARCHAR), Length(45,true) */
    val userRole: Rep[String] = column[String]("USER_ROLE", O.Length(45,varying=true))
    /** Database column CREATE_TIME SqlType(TIMESTAMP) */
    val createTime: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("CREATE_TIME")
  }
  /** Collection-like TableQuery object for table Users */
  lazy val Users = new TableQuery(tag => new Users(tag))

  /** Entity class storing rows of table UserTokens
   *  @param token Database column TOKEN SqlType(VARCHAR), PrimaryKey, Length(36,true)
   *  @param userId Database column USER_ID SqlType(VARCHAR), Length(36,true)
   *  @param creationTime Database column CREATION_TIME SqlType(TIMESTAMP) */
  case class UserToken(token: String, userId: String, creationTime: Option[java.sql.Timestamp])
  /** GetResult implicit for fetching UserToken objects using plain SQL queries */
  implicit def GetResultUserToken(implicit e0: GR[String], e1: GR[Option[java.sql.Timestamp]]): GR[UserToken] = GR{
    prs => import prs._
    UserToken.tupled((<<[String], <<[String], <<?[java.sql.Timestamp]))
  }
  /** Table description of table USER_TOKENS. Objects of this class serve as prototypes for rows in queries. */
  class UserTokens(_tableTag: Tag) extends Table[UserToken](_tableTag, "USER_TOKENS") {
    def * = (token, userId, creationTime) <> (UserToken.tupled, UserToken.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(token), Rep.Some(userId), creationTime).shaped.<>({r=>import r._; _1.map(_=> UserToken.tupled((_1.get, _2.get, _3)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column TOKEN SqlType(VARCHAR), PrimaryKey, Length(36,true) */
    val token: Rep[String] = column[String]("TOKEN", O.PrimaryKey, O.Length(36,varying=true))
    /** Database column USER_ID SqlType(VARCHAR), Length(36,true) */
    val userId: Rep[String] = column[String]("USER_ID", O.Length(36,varying=true))
    /** Database column CREATION_TIME SqlType(TIMESTAMP) */
    val creationTime: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("CREATION_TIME")
  }
  /** Collection-like TableQuery object for table UserTokens */
  lazy val UserTokens = new TableQuery(tag => new UserTokens(tag))
}
