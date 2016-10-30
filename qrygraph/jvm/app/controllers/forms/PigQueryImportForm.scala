package controllers.forms

import play.api.data.Form
import play.api.data.Forms._
/**
  * Created by info on 27.10.2016.
  */
case class PigQueryImportForm(name: String, description: String, cronjob: String, importString: String)

object PigQueryImportForm {


  def form = Form(
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "cronjob" -> nonEmptyText,
      "importString" -> nonEmptyText
    )(PigQueryImportForm.apply)(PigQueryImportForm.unapply)
  )

  // access rights?
}