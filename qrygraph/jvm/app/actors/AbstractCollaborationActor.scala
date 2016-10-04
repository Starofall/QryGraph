package actors

import actors.ServerConnectionActor.UserMessage
import akka.actor.{Actor, ActorRef}
import models.Tables.User
import play.api.Logger
import qrygraph.shared.SharedMessages.{CUserDisconnected, NetworkMessage}

/**
  * An abstract actor used for a collaboration actor
  * allows to track connected users and to send broadcast messages to them
  */
abstract class AbstractCollaborationActor extends Actor {

  /** a list of users that are connected to this editing session */
  var connectedUsers = Set[(User, ActorRef)]()

  /** sends a message to all connected clients with optional excluding one user */
  def broadCast(message: NetworkMessage, excluding: Option[ActorRef] = None): Unit = connectedUsers
    .map(_._2)
    .filterNot(excluding.contains(_)) // remove user - if option is valid and contains the user
    .foreach(_ ! message)

  /** abstract message handler */
  def handleMessage: PartialFunction[NetworkMessage, Unit]

  /** central recieve */
  def receive: Receive = {

    case UserMessage(user, message) =>
      // add actor if not done
      if (!connectedUsers.exists(_._2 == sender())) {
        val newEntry = (user, sender())
        connectedUsers += newEntry
      }
      // process his message
      message match {
        case CUserDisconnected() =>
          connectedUsers = connectedUsers.filterNot(_._1 == user)
          if (connectedUsers.isEmpty) {
            context.stop(self)
          }
        // normal message -> forward it
        case x                   => handleMessage(x)
      }

    // here only use messages are allowed
    case x => Logger.warn("Unknown packet in AbstractCollaborationActor - content:" + x.toString)
  }

}
