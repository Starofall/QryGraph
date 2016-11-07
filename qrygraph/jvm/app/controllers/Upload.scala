package controllers

import javax.inject._

import actors.PigExecutorHandling
import com.google.inject.Inject
import models.{DatabaseAccess, MetaDataAccess}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import util.HDFS

import scala.util.control.NonFatal

@Singleton
class Upload @Inject()(implicit val app: play.api.Application, val messagesApi: MessagesApi) extends Controller with DatabaseAccess with I18nSupport with PigExecutorHandling with MetaDataAccess {

  def uploadGET = Action { request =>
    Ok(views.html.upload())
  }

  def uploadPOST = Action(parse.multipartFormData)  { implicit request =>
    request.body.file("file").map { f =>
      val filename = f.filename
      val contentType = f.contentType
      try {
        HDFS.writeFile(globalSetting, filename, f.ref.file.getAbsoluteFile)
        Ok("File uploaded to: " + globalSetting.qrygraphFolder + "/" + filename)
      } catch {
        case NonFatal(e) =>
          Logger.error("Error uploading file to hdfs",e)
          BadRequest("Upload failed - check your connection")
      }
    }.getOrElse {
      Redirect(routes.Upload.uploadGET()).flashing("error" -> "Missing file")
    }
  }


}



