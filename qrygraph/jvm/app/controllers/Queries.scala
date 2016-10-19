package controllers

import javax.inject._

import actors.PigExecutorHandling
import com.google.inject.Inject
import controllers.forms.PigQueryForm
import models.Tables.PigQuery
import models.{DBEnums, DatabaseAccess, MetaDataAccess}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import services.PigExecution
import util.Actions._
import util.FutureEnhancements._
import util.HDFS

import scala.concurrent.Future

@Singleton
class Queries @Inject()(implicit val app: play.api.Application, val messagesApi: MessagesApi)
  extends Controller with DatabaseAccess with I18nSupport with PigExecutorHandling with MetaDataAccess {

  import dbConfig.driver.api._

  def index() = AuthedAction.async { implicit request =>
    runQuery(Tables.PigQueries.filter(_.creatorUserId === request.user.id)).map(q =>
      Ok(views.html.queries(request.user, q))
    )
  }

  def createGET = AuthedAction(app) { request =>
    Ok(views.html.createQuery(request.user, PigQueryForm.form))
  }

  def createPOST = AuthedAction.async { implicit request =>
    PigQueryForm.form.bindFromRequest.fold(
      // Form errors
      formWithErrors => {
        Future(BadRequest(views.html.createQuery(request.user, formWithErrors)))
      },
      // Correct form
      createRequest => {
        runInsert(Tables.PigQueries +=
          PigQuery(newUUID(), createRequest.name, createRequest.description, None, None, undeployedChanges = false, request.user.id, DBEnums.AuthApproved, "waiting", createRequest.cronjob)
        ).mapAll { _ =>
          syncQuerySchedules()
          Redirect(routes.Queries.index())
        }
      }
    )
  }


  def editGET(id: String) = (AuthedAction andThen ReadQueryFromId(app, id)) { request =>
    val pigQueryForm = PigQueryForm(request.pigQueriesRow.name, request.pigQueriesRow.description, request.pigQueriesRow.cronjob)
    Ok(views.html.editQuery(request.user, id, PigQueryForm.form.fill(pigQueryForm)))
  }

  def editPOST(id: String) = (AuthedAction andThen ReadQueryFromId(app, id)).async { implicit request =>
    PigQueryForm.form.bindFromRequest.fold(
      // Form errors
      formWithErrors => {
        Future(BadRequest(views.html.createQuery(request.user, formWithErrors)))
      },
      // Correct form
      editRequest => {
        val newQuery = request.pigQueriesRow.copy(name = editRequest.name, description = editRequest.description, cronjob = editRequest.cronjob)
        runInsert(Tables.PigQueries.insertOrUpdate(newQuery)).mapAll { _ =>
          syncQuerySchedules()
          Redirect(routes.Queries.index())
        }
      }
    )
  }


  def deleteQuery(id: String) = AuthedAction.async { implicit request =>
    val future = db.run(Tables.PigQueries
      .filter(_.id === id)
      .filter(_.creatorUserId === request.user.id)
      .delete)
    future
      .map(i => {
        syncQuerySchedules()
        Redirect(routes.Queries.index())
      })
      .recover { case f => Unauthorized(f.toString) }
  }


  def run(id: String) = AuthedAction(app) {
    // start actor to run the execution
    Future {
      new PigExecution().executePig(id)
    }
    Redirect(routes.Queries.index())
  }

  def results(id: String) = AuthedAction(app).async { implicit request =>
    HDFS.readResults(globalSetting, id)
      .map(i => Ok(views.html.results(request.user, i)))
      .recover { case f => Unauthorized(f.toString) }
  }

  def editor(id: String) = (AuthedAction andThen ReadQueryFromId(app, id)) { request =>
    Ok(views.html.editor(request.user, request.cookies, id, isComponent = false, loadDataSources(), loadPublishedComponents()))
  }

}



