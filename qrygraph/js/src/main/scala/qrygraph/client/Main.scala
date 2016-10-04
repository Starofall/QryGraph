package qrygraph.client


import akka.actor.{ActorRef, ActorSystem, Props}
import org.scalajs.jquery.jQuery
import qrygraph.client.actors.{ClientActor, InitConfiguration}

import scala.scalajs._

/** the main method is started when the js launcher is loaded */
object Main extends js.JSApp {

  /** the main actor on the client handling the graphEditor */
  lazy val clientActor = ActorSystem("ActorSystem").actorOf(Props(new ClientActor), name = "ClientActor")

  /** this method gets called from the launcher */
  def main(): Unit = {
    // Start the app when jQuery is ready
    jQuery(() => {
      clientActor // access the lazy val creates the instance
      loadConfig(clientActor)
    })
  }

  /**
    * the tool is started in the editor.scala.html file
    * here the server also defines the start configuration of the tool
    * this function is parsing this configuration and applies it into the runtime
    */
  def loadConfig(actorRef: ActorRef): Unit = {
    // parse config
    val config = js.Dynamic.global.window.runConfiguration
    // the auth token used to access the secure webSocket connection
    val authToken = config.selectDynamic("authToken").asInstanceOf[String]
    // the id of the query we want to edit in this session
    val queryId = config.selectDynamic("queryId").asInstanceOf[String]
    val componentId = config.selectDynamic("componentId").asInstanceOf[String]
    // send init config to actor
    actorRef ! InitConfiguration(authToken, queryId,componentId)
  }

}

