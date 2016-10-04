package qrygraph.shared

import scala.concurrent.duration._

/**
  * Created by info on 11.04.2016.
  */
object Util {

  /** the interval for which we send the keepAlive */
  val TIMEOUT_SEND_INTERVAL = 80.seconds

  /** the maximum number of seconds the client waits for a keepAlive from the client */
  final val TIMEOUT_SECONDS = 90

  /** true if we want to debug webSocket IO */
  final val DEBUG_CLIENT_WEBSOCKET = true
}
