package controllers.forms

import play.api.data.Form
import play.api.data.Forms._
/**
  * Created by info on 11.07.2016.
  */
case class LoginForm(email: String, password: String)

object LoginForm {
  def form = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText
    )(LoginForm.apply)(LoginForm.unapply)
  )
}