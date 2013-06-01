package margatsni.bootstrap.server

import akka.actor.ActorSystem
import BootstrapServerActor.Start

object Boot extends App {

  val system = ActorSystem("bootstrapServer")
  val settings = P2PPManagerSettings(tcpPort = 6666, udpPort = 6666,
                                     overlayID = "fooOverlay", hashAlgorithm = "SHA-1",
                                     hashLength = 20,  hashBase = 2)
  val bootstrapServer = BootstrapServerActor(settings, "bootstrapServer")(system)
  bootstrapServer ! Start

}



