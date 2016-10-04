package controllers

import javax.inject._

import com.google.inject.Inject
import controllers.forms.LoginForm
import models.{DatabaseAccess, LoginAccess}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller, Cookie}
import util.Actions._
import util.FutureEnhancements._

import scala.concurrent.Future
import scala.util.{Failure, Success}


@Singleton
class Auth @Inject()(implicit val app: play.api.Application, val messagesApi: MessagesApi) extends Controller with DatabaseAccess with I18nSupport with LoginAccess {

  def loginGET = PublicAction(app) { implicit request =>
    Ok(views.html.login(LoginForm.form))
  }

  def loginPOST = PublicAction(app).async { implicit request =>
    LoginForm.form.bindFromRequest.fold(
      // Form errors
      formWithErrors => {
        Future(BadRequest(views.html.login(formWithErrors)))
      },
      // Correct form
      loginRequest => {
        tryLogin(loginRequest.email, loginRequest.password).mapAll {
          case Success(Some(result)) =>
            Logger.info(s"Correct login for email: " + result._1.email)
            Redirect(routes.Dashboard.index()).withCookies(Cookie("qgtoken", result._2.token))

          case Success(None) =>
            Logger.info(s"Login wrong for email: " + loginRequest.email)
            BadRequest(views.html.login(LoginForm.form.fill(loginRequest).withGlobalError("Login error - Check your username and password")))

          case Failure(e) =>
            Logger.error(s"Error while logging in: " + loginRequest.email + " - " + e.getMessage)
            BadRequest(views.html.login(LoginForm.form.fill(loginRequest).withGlobalError("Server Error - Please try again later")))
        }
      }
    )
  }

  def logoutGET = PublicAction(app) { implicit request =>
    Redirect(routes.Auth.loginGET()).withCookies(Cookie("qgtoken", ""))
  }

  def register() = Action {

    Ok("depricated")
  }
}



