package margatsni.node

import akka.actor.ActorSystem

object Boot extends App {
  implicit val system = ActorSystem("nodeSystem")
  val settings = NodeConfig()
  system.actorOf(MargatsniServiceActor(settings.node, settings.socketIo), "margatsniService")
}



