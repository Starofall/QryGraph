package qrygraph.client.wrapper

import scala.scalajs.js
import scala.scalajs.js.Any

/** Console used by JS - shows a different view on the data than println() */
object JSConsole {
  def log(any: scala.AnyRef) = js.Dynamic.global.console.log(any.asInstanceOf[Any])
}
