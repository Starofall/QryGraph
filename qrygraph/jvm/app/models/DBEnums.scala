package models

import scala.language.implicitConversions

/**
  * As slick is not able to auto-enum our databases, we have to use string enums
  */
object DBEnums {

  val RoleAdmin = "admin"
  val RoleUser = "user"

  val AuthApproved = "approved"
  val AuthPending = "pending"
  val AuthRejected = "rejected"

  val ExecSuccess = "success"
  val ExecFailed = "failed"


  val RunRunning = "running"
  val RunScheduled = "scheduled"
  val RunPaused = "paused"
}
