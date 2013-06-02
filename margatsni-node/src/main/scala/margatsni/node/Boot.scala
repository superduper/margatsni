package margatsni.node

import akka.actor.ActorSystem
import java.util.UUID

object Boot extends App {
  import Settings._
  implicit val system = ActorSystem("nodeSystem")

  val port = args(0).toInt
  val user = args(1)
  val nodeSettings = NodeSettings(
    user = user,
    listen = HostAndPort("0.0.0.0", port+1),
    bootstrapServer = HostAndPort("0.0.0.0", 6666),
    corePort = port+2
  )
  val socketIoSettings = HostAndPort("0.0.0.0", port)

  system.actorOf(MargatsniServiceActor(nodeSettings, socketIoSettings), "margatsniService")
}



