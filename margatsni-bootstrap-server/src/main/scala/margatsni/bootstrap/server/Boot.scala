package margatsni.bootstrap.server

import akka.actor.ActorSystem

object Boot extends App {
  val settings = BootstrapServerConfig()

  // create and start our service actor
  lazy implicit val system = ActorSystem("bootstrapServer", settings.projectConfig)
  val p2pSettings = P2PPManagerSettings(tcpPort = settings.tcpPort, udpPort = settings.udpPort,
                                     overlayID = settings.overlayId, hashAlgorithm = settings.hashAlgorithm,
                                     hashLength = 20,  hashBase = 2)
  BootstrapServerActor(p2pSettings, "bootstrapServer")
}



