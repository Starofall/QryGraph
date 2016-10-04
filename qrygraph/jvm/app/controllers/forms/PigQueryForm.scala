package controllers.forms

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
