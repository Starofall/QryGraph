package controllers.forms

import play.api.data.Form
import play.api.data.Forms._
/**
  * Created by info on 27.10.2016.
  */
case class PigComponentForm(name: String, description: String)

object PigComponentForm {


  def form = Form(
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText
    )(PigComponentForm.apply)(PigComponentForm.unapply)
  )

  // access rights?
}