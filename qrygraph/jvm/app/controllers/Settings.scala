package controllers

import javax.inject._

import com.google.inject.Inject
import controllers.forms.{DBUserForm, DataSourceForm, SettingsForm}
import models.Tables._
import models.{DatabaseAccess, MetaDataAccess}
import org.mindrot.jbcrypt.BCrypt
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import util.Actions._

import scala.concurrent.Future
import scala.util.Success

@Singleton
class Settings @Inject()(implicit val app: play.api.Application, val messagesApi: MessagesApi) extends Controller with I18nSupport with DatabaseAccess with MetaDataAccess {

  import dbConfig.driver.api._
  import util.FutureEnhancements._

  def indexGET = AdminAction.async { implicit request =>
    runQuerySingle(GlobalSettings.filter(_.id === "1")).mapAll {
      case Success(Some(value)) =>
        val form = SettingsForm(value.hadoopUser, value.qrygraphFolder, value.fsDefaultName, value.mapredJobTracker)
        Ok(views.html.settings(request.user, SettingsForm.form.fill(form), loadDataSources(),loadUsers()))

      case _ =>
        Ok(views.html.settings(request.user, SettingsForm.form.fill(SettingsForm("", "", "", "")), loadDataSources(),loadUsers()))
    }
  }

  def indexPOST = AdminAction.async { implicit request =>
    SettingsForm.form.bindFromRequest.fold(
      // Form errors
      formWithErrors => {
        Future(BadRequest(views.html.settings(request.user, formWithErrors, loadDataSources(),loadUsers())))
      },
      // Correct form
      setupForm => {
        val gs = GlobalSetting("1", setupForm.hadoopUser, setupForm.qrygraphFolder, setupForm.fsDefaultName, setupForm.mapredJobTracker)
        runInsert(Tables.GlobalSettings.insertOrUpdate(gs)).mapAll { _ =>
          Redirect(routes.Settings.indexGET())
        }
      }
    )
  }


  ////////////////////////////////////
  // Sources

  def sourcesCreateGET = AdminAction(app) { request =>
    Ok(views.html.createDataSource(request.user, DataSourceForm.form))
  }

  def sourcesCreatePOST = AdminAction(app).async { implicit request =>
    DataSourceForm.form.bindFromRequest.fold(
      // Form errors
      formWithErrors => {
        Future(BadRequest(views.html.createDataSource(request.user, formWithErrors)))
      },
      // Correct form
      createRequest => {
        runInsert(Tables.DataSources +=
          DataSource(newUUID(), createRequest.name, createRequest.description, createRequest.loadCommand)
        ).mapAll { _ =>
          Redirect(routes.Settings.indexGET())
        }
      }
    )
  }

  def sourcesEditGET(id: String) = (AdminAction andThen ReadLoadSourceFromId(app, id)) { request =>
    Ok(views.html.editDataSource(request.user, id, DataSourceForm.fillFrom(request.dataSource)))
  }

  def sourcesEditPOST(id: String) = (AdminAction andThen ReadLoadSourceFromId(app, id)).async { implicit request =>
    DataSourceForm.form.bindFromRequest.fold(
      // Form errors
      formWithErrors => Future(BadRequest(views.html.createDataSource(request.user, formWithErrors))),
      // Correct form
      editRequest => {
        val newSource = request.dataSource.copy(name = editRequest.name, description = editRequest.description, loadcommand = editRequest.loadCommand)
        runInsert(Tables.DataSources.insertOrUpdate(newSource)).mapAll { _ =>
          Redirect(routes.Settings.indexGET())
        }
      }
    )
  }

  def sourcesDELETE(id: String) = AdminAction.async { implicit request =>
    db.run(Tables.DataSources.filter(_.id === id).delete)
      .map(i => Redirect(routes.Settings.indexGET()))
      .recover { case f => Unauthorized(f.toString) }
  }


  ////////////////////////////////////
  // USER


  def userCreateGET = AdminAction(app) { request =>
    Ok(views.html.createDBUser(request.user, DBUserForm.form))
  }

  def userCreatePOST = AdminAction(app).async { implicit request =>
    DBUserForm.form.bindFromRequest.fold(
      // Form errors
      formWithErrors => {
        Future(BadRequest(views.html.createDBUser(request.user, formWithErrors)))
      },
      // Correct form
      r => {
        runInsert(Tables.Users +=
          User(newUUID(), r.email, BCrypt.hashpw(r.password, BCrypt.gensalt()), r.firstName, r.lastName, r.userRole, None)
        ).mapAll { _ =>
          Redirect(routes.Settings.indexGET())
        }
      }
    )
  }


  def userEditGET(id: String) = (AdminAction andThen ReadDBUserFromId(app, id)) { request =>
    Ok(views.html.editDBUser(request.user, id, DBUserForm.fillFrom(request.dbUser)))
  }

  def userEditPOST(id: String) = (AdminAction andThen ReadDBUserFromId(app, id)).async { implicit request =>
    DBUserForm.form.bindFromRequest.fold(
      // Form errors
      formWithErrors => Future(BadRequest(views.html.createDBUser(request.user, formWithErrors))),
      // Correct form
      editRequest => {
        val newSource = request.dbUser.copy(
          firstName = editRequest.firstName,
          lastName = editRequest.lastName,
          email = editRequest.email,
          password = BCrypt.hashpw(editRequest.password, BCrypt.gensalt()),
          userRole = editRequest.userRole
        )
        runInsert(Tables.Users.insertOrUpdate(newSource)).mapAll { _ =>
          Redirect(routes.Settings.indexGET())
        }
      }
    )
  }

  def userDELETE(id: String) = AdminAction.async { implicit request =>
    db.run(Tables.Users.filter(_.id === id).delete)
      .map(i => Redirect(routes.Settings.indexGET()))
      .recover { case f => Unauthorized(f.toString) }
  }

}



