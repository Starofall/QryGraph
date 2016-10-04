package util

trait RuntimeRef {
  val app: play.api.Application

  def config = app.configuration
}