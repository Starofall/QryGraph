package qrygraph.client.actors

import akka.actor._
import org.scalajs.dom._
import org.scalajs.dom.raw.{Event, WebSocket}
import prickle.{Pickle, Unpickle}

import scala.util.{Failure, Success}
import akka.actor.Cancellable
import qrygraph.shared.SharedMessages.{CKeepAlive, ClientToServer, NetworkMessage, SKeepAlive, ServerToClient}
import qrygraph.shared.{SharedMessages, Util}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

/**
  * This Actor hides the implementation details of webSocket
  * Messages send to this actor are forwarded to the server
  */
class ClientConnectionActor(clientActor: ActorRef) extends Actor {

  /** this explains the pickler how to pickle and unpickle objects */
  implicit val messagePickler = SharedMessages.messagePickler

  /** the last time we got a keepAlive from the server */
  var lastResponseTime = System.currentTimeMillis() / 1000

  /* webSocket used to Server-Client communication */
  var webSocket: Option[WebSocket] = None

  var initConfiguration: Option[InitConfiguration] = None

  /**
    * responsible for keeping clients "logged in"
    * Sends every SEND_INTERVAL keepAlive Messages
    * If the server doesn't respond in CONNECTION_TIMEOUT_SECONDS the clients tries to reconnect */
  val keepAlive: Cancellable = context.system.scheduler.schedule(0.seconds, Util.TIMEOUT_SEND_INTERVAL) {
    webSocket.foreach { m =>
      if (m.readyState == 1) {
        //we are connected
        self ! CKeepAlive()
        if (System.currentTimeMillis() / 1000 - lastResponseTime >= Util.TIMEOUT_SECONDS) {
          println(s"My Server didn't send anything for ${System.currentTimeMillis() / 1000 - lastResponseTime}s...")
          initConfiguration.foreach(ic => webSocket = Some(createWebSocket(ic)))
        }
      }
    }
  }

  /** creating a new webSocket instance is used to connect to Server */
  def createWebSocket(ic: InitConfiguration): WebSocket = {
    // to simplify component vs query editing we check if a queryId is given, else we assume it is a component
    val url = if(ic.queryId != ""){
      "ws://" + window.location.hostname + ":" + window.location.port + "/webSocket?qgtoken=" + ic.authToken + "&queryId=" + ic.queryId
    }else{
      "ws://" + window.location.hostname + ":" + window.location.port + "/webSocket?qgtoken=" + ic.authToken + "&componentId=" + ic.componentId
    }
    val webSocket = new WebSocket(url)
    webSocket.onopen = onConnect _
    webSocket.onmessage = onMessage _
    webSocket.onerror = onError _
    webSocket.onclose = onClose _
    webSocket
  }

  /** webSocket is trying to connect */
  def onConnect(e: Event) {
    clientActor ! IConnectionEstablished()
  }

  /** webSocket error in connection */
  def onError(e: Event): Unit = {
    println("Connection Error... trying to reconnect")
  }

  /** webSocket connection closed */
  def onClose(e: Event) {
    setTimeout(() => {
      initConfiguration.foreach(ic => webSocket = Some(createWebSocket(ic)))
    }, 100)
  }

  /**
    * This handles the messages that are send through the WebSocket instance.
    * The message gets unpickled into objects and send to the akka instance
    */
  def onMessage(msgEvent: MessageEvent) = {
    Unpickle[NetworkMessage].fromString(msgEvent.data.toString) match {
      case Failure(exception) => println("Was not able to unpickle server message: " + exception)
      case Success(value)     => self ! value
    }
  }

  /** the behaviour as an akka instance */
  def receive: Receive = {

    case e: InitConfiguration =>
      initConfiguration = Some(e)
      webSocket = Some(createWebSocket(e))

    case n: ClientToServer =>
      webSocket.map { ws =>
        if (ws.readyState != 1 /* connected */ ) {
          setTimeout(() => {
            initConfiguration.foreach(ic => webSocket = Some(createWebSocket(ic)))
          }, 100)
        }
      }
      if (Util.DEBUG_CLIENT_WEBSOCKET) {
        println("Outgoing: " + n.toString)
      }
      webSocket.foreach(_.send(Pickle.intoString(n: NetworkMessage)))

    case m: ServerToClient =>
      if (Util.DEBUG_CLIENT_WEBSOCKET) {
        println("Incoming: " + m.toString)
      }
      //filter out SKeepAlive, as they are only relevant for timeout checking
      m match {
        case SKeepAlive() => lastResponseTime = System.currentTimeMillis() / 1000
        case _            => clientActor ! m
      }

    case x => println(s"WARNING UNKNOWN MESSAGE: $x")
  }
}
