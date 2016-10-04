package actors

import actors.ServerConnectionActor.UserMessage
import akka.actor._
import models.Tables._
import play.api.Logger
import prickle.{Pickle, Unpickle}
import qrygraph.shared.SharedMessages._
import util.ServerKeepAlive

import scala.util.{Failure, Success}

/**
  * For each open webSocket one of this actors is created.
  * Every message that comes from the webSocket passes this actor.
  * Messages to the actor are either send to the webSocket or to the editingActor.
  */
class ServerConnectionActor(out: ActorRef, user: User, editingActor: ActorRef) extends Actor with ActorLogging with ServerKeepAlive {

  /** forward each message to the serverActor */
  def receive = {

    case msg: String => Unpickle[NetworkMessage].fromString(msg) match {
      case Failure(exception) => Logger.error("UNKNOWN PACKET FORMAT")
      case Success(value)     => self ! value
    }

    //Messages from the client go the the mainServerActor
    case m: ClientToServer =>
      Logger.debug("ServerConnectionActor-fromClient - " + m)
      m match {
        case CKeepAlive() => receivedKeepAlive()
        case _            => editingActor ! UserMessage(user, m)
      }

    //Messages to the client go out through the backChannel
    case n: ServerToClient =>
      Logger.debug("ServerConnectionActor-toClient   - " + n)
      out ! Pickle.intoString(n: NetworkMessage)

    case x => Logger.error("Unknown packet in ConnectionHandlerActor - " + x)
  }

  def connectionTimedOut() = editingActor ! UserMessage(user, CUserDisconnected())
}

object ServerConnectionActor {
  def props(backChannel: ActorRef, user: User, editingActor: ActorRef): Props = Props(new ServerConnectionActor(backChannel, user, editingActor))

  /** a wrapper for a user send message to enable user linking */
  case class UserMessage(user: User, packet: NetworkMessage)
}

