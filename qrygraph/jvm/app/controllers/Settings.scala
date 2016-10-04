package controllers

import javax.inject._

import com.google.inject.Inject
import controllers.forms.SettingsForm
import models.DatabaseAccess
import models.Tables._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import util.Actions._

import scala.concurrent.Future
import scala.util.Success

@Singleton
class Settings @Inject()(implicit val app: play.api.Application, val messagesApi: MessagesApi) extends Controller with I18nSupport with DatabaseAccess {

  import dbConfig.driver.api._
  import util.FutureEnhancements._

  def indexGET = AuthedAction(app).async { implicit request =>
    runQuerySingle(GlobalSettings.filter(_.id === "1")).mapAll {
      case Success(Some(value)) =>
        val form = SettingsForm(value.hadoopUser, value.qrygraphFolder, value.fsDefaultName, value.mapredJobTracker)
        Ok(views.html.settings(request.user, SettingsForm.form.fill(form)))
      case _                    =>
        Ok(views.html.settings(request.user, SettingsForm.form.fill(SettingsForm("", "", "", ""))))
    }
  }

  def indexPOST = AuthedAction(app).async { implicit request =>
    SettingsForm.form.bindFromRequest.fold(
      // Form errors
      formWithErrors => {
        Future(BadRequest(views.html.settings(request.user, formWithErrors)))
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

}



