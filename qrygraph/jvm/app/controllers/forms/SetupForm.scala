package controllers.forms

import play.api.data.Form
import play.api.data.Forms._

/**
  * Created by info on 11.07.2016.
  */
case class SetupForm(email: String, password: String, firstName: String, lastName: String, hadoopUser: String, qrygraphFolder: String, fsDefaultName: String, mapredJobTracker: String)

object SetupForm {
  def form = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText,
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "hadoopUser" -> nonEmptyText,
      "qrygraphFolder" -> nonEmptyText,
      "fsDefaultName" -> nonEmptyText,
      "mapredJobTracker" -> nonEmptyText
    )(SetupForm.apply)(SetupForm.unapply)
  )
}