package controllers.forms

import models.Tables._
import play.api.data.Form
import play.api.data.Forms._
/**
  * Created by info on 27.10.2016.
  */
case class DBUserForm(firstName: String, lastName: String, email: String, userRole: String, password: String)

object DBUserForm {

  def from(q: User): DBUserForm = {
    DBUserForm(q.firstName, q.lastName, q.email, q.userRole, q.password)
  }

  def fillFrom(q: User): Form[DBUserForm] = {
    form.fill(from(q))
  }

  def form = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "email" -> email,
      "userRole" -> nonEmptyText,
      "password" -> nonEmptyText
    )(DBUserForm.apply)(DBUserForm.unapply)
  )

  // access rights?
}