package controllers

import javax.inject._

import com.google.inject.Inject
import controllers.forms.PigComponentForm
import models.Tables.PigComponent
import models.{DatabaseAccess, MetaDataAccess}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import util.Actions._
import util.FutureEnhancements._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class Components @Inject()(implicit val app: play.api.Application, val messagesApi: MessagesApi) extends Controller with DatabaseAccess with I18nSupport with MetaDataAccess {

  import dbConfig.driver.api._

  def indexGET() = AuthedAction(app).async { implicit request =>
    runQuery(Tables.PigComponents).map(q =>
      Ok(views.html.components(request.user, q))
    )
  }

  def createGET = AuthedAction(app) { request =>
    Ok(views.html.createComponent(request.user, PigComponentForm.form))
  }

  def createPOST = AuthedAction(app).async { implicit request =>
    PigComponentForm.form.bindFromRequest.fold(
      // Form errors
      formWithErrors => {
        Future(BadRequest(views.html.createComponent(request.user, formWithErrors)))
      },
      // Correct form
      createRequest => {
        runInsert(Tables.PigComponents +=
          PigComponent(newUUID(), createRequest.name, createRequest.description, None, published = false, request.user.id)
        ).mapAll(_ =>
          Redirect(routes.Components.indexGET()))
      }
    )
  }


  def editGET(id: String) = (AuthedAction(app) andThen ReadComponentFromId(app, id)) { request =>
    val pigQueryForm = PigComponentForm(request.componentRow.name, request.componentRow.description)
    Ok(views.html.editComponent(request.user, id, PigComponentForm.form.fill(pigQueryForm)))
  }

  def editPOST(id: String) = (AuthedAction(app) andThen ReadComponentFromId(app, id)).async { implicit request =>
    PigComponentForm.form.bindFromRequest.fold(
      // Form errors
      formWithErrors => {
        Future(BadRequest(views.html.createComponent(request.user, formWithErrors)))
      },
      // Correct form
      editRequest => {
        val newQuery = request.componentRow.copy(name = editRequest.name, description = editRequest.description)
        runInsert(Tables.PigComponents.insertOrUpdate(newQuery)).mapAll { _ =>
          Redirect(routes.Components.indexGET())
        }
      }
    )
  }


  def deleteComponent(id: String) = AuthedAction(app).async { implicit request =>
    val future = db.run(Tables.PigComponents
      .filter(_.id === id)
      .filter(_.creatorUserId === request.user.id)
      .delete)
    future
      .map(i => {
        Redirect(routes.Components.indexGET())
      })
      .recover { case f => Unauthorized(f.toString) }
  }

  def editor(id: String) = (AuthedAction(app) andThen ReadComponentFromId(app, id)) { request =>
    Ok(views.html.editor(request.user, request.cookies, id, isComponent = true, loadDataSources(), loadPublishedComponents()))
  }

}




