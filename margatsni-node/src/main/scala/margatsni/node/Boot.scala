package margatsni.node

import akka.actor.ActorSystem
import java.util.UUID

object Boot extends App {

  implicit val system = ActorSystem("nodeSystem")

  val port = args(0).toInt

  val nodeSettings = NodeSettings(
    user = UUID.randomUUID().toString,
    listen = HostAndPort("0.0.0.0", port+1),
    bootstrapServer = HostAndPort("0.0.0.0", 6666)
  )
  val socketIoSettings = HostAndPort("0.0.0.0", port)

  system.actorOf(MargatsniServiceActor(nodeSettings, socketIoSettings), "margatsniService")
}



