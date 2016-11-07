package controllers

import javax.inject._

import actors.PigExecutorHandling
import com.google.inject.Inject
import models.{DatabaseAccess, LoginAccess, MetaDataAccess}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import util.HDFS

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

@Singleton
class Upload @Inject()(implicit val app: play.api.Application, val messagesApi: MessagesApi) extends Controller with DatabaseAccess with LoginAccess with I18nSupport with PigExecutorHandling with MetaDataAccess {

  def uploadGET = Action { request =>
    Ok(views.html.upload())
  }

  def uploadPOST = Action(parse.multipartFormData) { implicit request =>
    import util.FutureEnhancements._

    import scala.concurrent.ExecutionContext.Implicits.global

    Await.result(request.cookies.get("qgtoken").map(_.value) match {
      case Some(token) => checkToken(token).mapAll {
        case Failure(e) =>
          Logger.error("Failed checking user with database: " + e.getMessage)
          Unauthorized("not authed")

        case Success(None) =>
          Logger.info("Invalide token used")
          Unauthorized("not authed")

        case Success(Some(x)) =>
          request.body.file("file").map { f =>
            val filename = f.filename
            val contentType = f.contentType
            try {
              HDFS.writeFile(globalSetting, filename, f.ref.file.getAbsoluteFile)
              Ok("File uploaded to: " + globalSetting.qrygraphFolder + "/" + filename)
            } catch {
              case NonFatal(e) =>
                Logger.error("Error uploading file to hdfs", e)
                BadRequest("Upload failed - check your connection")
            }
          }.getOrElse {
            Redirect(routes.Upload.uploadGET()).flashing("error" -> "Missing file")
          }
      }
      case None        => Future(Unauthorized("not authed"))
    },100.seconds)
  }


}



