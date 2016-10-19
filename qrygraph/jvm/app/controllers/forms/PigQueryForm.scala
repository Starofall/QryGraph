package controllers.forms

import models.Tables.{DataSource, User}
import play.api.data.Form
import play.api.data.Forms._

/**
  * Created by info on 11.07.2016.
  */
object PigQueryForm {


  def form = Form(
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "cronjob" -> nonEmptyText
    )(PigQueryForm.apply)(PigQueryForm.unapply)
  )

  // access rights?
}
case class PigQueryForm(name: String, description: String, cronjob: String)


object PigComponentForm {


  def form = Form(
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText
    )(PigComponentForm.apply)(PigComponentForm.unapply)
  )

  // access rights?
}
case class PigComponentForm(name: String, description: String)


object DataSourceForm {

  def from(q: DataSource): DataSourceForm = {
    DataSourceForm(q.name, q.description, q.loadcommand)
  }

  def fillFrom(q: DataSource): Form[DataSourceForm] = {
    form.fill(from(q))
  }

  def form = Form(
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "loadCommand" -> nonEmptyText
    )(DataSourceForm.apply)(DataSourceForm.unapply)
  )

  // access rights?
}
case class DataSourceForm(name: String, description: String, loadCommand: String)


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
case class DBUserForm(firstName: String, lastName: String, email: String, userRole: String, password: String)
