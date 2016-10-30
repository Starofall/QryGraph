package util

/** interface for the runtime variables of play */
trait RuntimeRef {
  /** the play application */
  val app: play.api.Application

  /** the play config */
  def config = app.configuration
}