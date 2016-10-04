package util

import akka.actor.{Actor, Cancellable}
import play.api.Logger
import qrygraph.shared.SharedMessages.SKeepAlive
import qrygraph.shared.{SharedMessages, Util}
import scala.concurrent.duration._
/**
  * Created by info on 12.07.2016.
  */
trait ServerKeepAlive {
  this: Actor =>

  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  def receivedKeepAlive(): Unit = {
    lastResponseTime = System.currentTimeMillis() / 1000
  }

  def connectionTimedOut(): Unit

  /** the last time we got a keepAlive from the client */
  var lastResponseTime = System.currentTimeMillis() / 1000
  /** this explains the pickler how to pickle and unpickle objects */
  implicit val messagePickler = SharedMessages.messagePickler

  /**
    * Pings the ClientConnectionActor every 'sendTime' seconds
    * If he doesn't ping back in 'maxTime' seconds, we lost the connection
    * Lets all the other rooms know and kills himself
    **/
  val keepAlive: Cancellable = context.system.scheduler.schedule(0.seconds, Util.TIMEOUT_SEND_INTERVAL) {
    self ! SKeepAlive()
    if (System.currentTimeMillis() / 1000 - lastResponseTime >= Util.TIMEOUT_SECONDS) {
      keepAlive.cancel()
      connectionTimedOut()
      //this prevents that in the last run there could be an nullPointer
      if (context != null && self != null) {
        Logger.error(s"My Client ${context.self.toString()} didn't send anything for ${System.currentTimeMillis() / 1000 - lastResponseTime}s...")
        context.stop(self)
      }
    }
  }

}
