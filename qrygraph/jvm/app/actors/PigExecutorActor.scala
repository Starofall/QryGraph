package actors

import java.util.concurrent.TimeUnit

import actors.PigExecutorActor.{ExecutionScheduledTask, RequestSyncScheduling}
import akka.actor.{Actor, Cancellable, Props}
import akka.util.Timeout
import models.Tables.{QueryExecution}
import models.{DBEnums, DatabaseAccess}
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import us.theatr.akka.quartz._
import util.FutureEnhancements._

import scala.concurrent.Await
import scala.concurrent.duration.{FiniteDuration, _}
import scala.util.{Failure, Success}

/**
  * Execution of Pig querys in the database
  */

class PigExecutorActor(val app: play.api.Application) extends Actor with DatabaseAccess {
  implicit val timeout = Timeout(FiniteDuration(1, TimeUnit.SECONDS))


  /** keeps track of jobs based on cron informations */
  val quartzActor = context.actorOf(Props[QuartzActor])

  /** a list of jobs submitted, used to cancle them for syncing */
  var cancelAbles = List[Cancellable]()

  def syncScheduling(): Unit = {
    Logger.debug("Resyncing PigExecution due to changes in the query list")
    import dbConfig.driver.api._
    // Remove all queries (we might have deleted one)
    cancelAbles.foreach(c => quartzActor ! RemoveJob(c))
    // Add every query again
    runQuery(Tables.PigQueries.filter(_.authorizationStatus === DBEnums.AuthApproved)).mapAll {
      case Success(list)  =>
        list.foreach(row => {
          quartzActor ! AddCronSchedule(self, "*/30 * * * * ?", ExecutionScheduledTask(row.id), reply = true)
        })
      case Failure(error) =>
        Logger.error("Failed loading queries while syncing: " + error)
    }
  }

  def receive: Receive = {
    case AddCronScheduleFailure(error)   => error.printStackTrace()
    case AddCronScheduleSuccess(cancel)  => cancelAbles ::= cancel
    case RequestSyncScheduling           => syncScheduling()
    case ExecutionScheduledTask(queryId) =>
      import dbConfig.driver.api._
      val executionId = newUUID()
      runInsert(Tables.QueryExecutions += QueryExecution(executionId, DBEnums.ExecFailed, queryId, None, None))
//      Logger.error("I WOULD EXECUTE:" + queryId)
  }
}


object PigExecutorActor {
  def props(app: play.api.Application): Props = Props(new PigExecutorActor(app))
  object RequestSyncScheduling
  case class ExecutionScheduledTask(queryId: String)
}


trait PigExecutorHandling {
  val app: play.api.Application
  implicit val materializer = app.materializer
  val system = app.actorSystem

  implicit val timeout = Timeout(FiniteDuration(1, TimeUnit.SECONDS))

  // init at the beginning of the launch
  synchronized(
    Await.result(system.actorSelection("/user/" + "query-executor").resolveOne().mapAll {
      case Success(actorRef) => // do not init again
      case _                 => system.actorOf(PigExecutorActor.props(app), name = "query-executor") ! RequestSyncScheduling
    }, 1.seconds)
  )


  def syncQuerySchedules() = {
    synchronized(
      Await.result(system.actorSelection("/user/" + "query-executor").resolveOne().mapAll {
        case Success(actorRef) =>
          actorRef ! RequestSyncScheduling
        case Failure(ex)       =>
          val actor = system.actorOf(PigExecutorActor.props(app), name = "query-executor")
          actor ! RequestSyncScheduling
      }, 1.seconds)
    )
  }
}