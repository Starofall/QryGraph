package controllers.forms

import models.Tables.DataSource
import play.api.data.Form
import play.api.data.Forms._
/**
  * Created by info on 27.10.2016.
  */
case class DataSourceForm(name: String, description: String, loadCommand: String)

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