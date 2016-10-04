package controllers

import javax.inject._

import actors.PigExecutorHandling
import com.google.inject.Inject
import models.{DatabaseAccess, MetaDataAccess}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import util.HDFS

@Singleton
class Upload @Inject()(implicit val app: play.api.Application, val messagesApi: MessagesApi) extends Controller with DatabaseAccess with I18nSupport with PigExecutorHandling with MetaDataAccess {

  def uploadGET = Action { request =>
    Ok(views.html.upload())
  }

  def uploadPOST = Action(parse.multipartFormData) { implicit request =>
    request.body.file("file").map { f =>
      val filename = f.filename
      val contentType = f.contentType
      HDFS.writeFile(globalSetting, filename, f.ref.file.getAbsoluteFile)
      Ok("File uploaded")
    }.getOrElse {
      Redirect(routes.Upload.uploadGET()).flashing("error" -> "Missing file")
    }
  }


}



