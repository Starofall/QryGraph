package controllers.forms

import play.api.data.Form
import play.api.data.Forms._
/**
  * Created by info on 11.07.2016.
  */
case class SettingsForm( hadoopUser: String, qrygraphFolder: String, fsDefaultName: String, mapredJobTracker: String)

object SettingsForm {
  def form = Form(
    mapping(
      "hadoopUser" -> nonEmptyText,
      "qrygraphFolder" -> nonEmptyText,
      "fsDefaultName" -> nonEmptyText,
      "mapredJobTracker" -> nonEmptyText
    )(SettingsForm.apply)(SettingsForm.unapply)
  )
}