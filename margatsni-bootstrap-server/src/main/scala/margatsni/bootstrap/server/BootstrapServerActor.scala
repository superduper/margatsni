package margatsni.bootstrap.server

import akka.actor.{ActorRef, ActorSystem, Props, Actor}
import pl.edu.pjwstk.p2pp.P2PPManager
import pl.edu.pjwstk.p2pp.objects.P2POptions
import pl.edu.pjwstk.p2pp.superpeer.SuperPeerBootstrapServer
import pl.edu.pjwstk.p2pp.superpeer.SuperPeerConstants
import pl.edu.pjwstk.p2pp.util.P2PPMessageFactory
import pl.edu.pjwstk.p2pp.util.P2PPUtils


class BootstrapServerActor(val manager: P2PPManager) extends Actor with akka.actor.ActorLogging {

  private val server = new SuperPeerBootstrapServer()

  def receive = {
    case _ =>   // nothing to handle
  }

  override def preStart() {
    manager.addEntity(server)
    manager.start()
    log.info(
      "Started bootstrap server at tcpPort: %d, udpPort: %d" format (manager.getTcpPort, manager.getUdpPort)
    )
  }

  override def postStop() {
    manager.stop()
    log.info(
      "Stopped bootstrap server"
    )
  }
}

case class P2PPManagerSettings(tcpPort: Int, udpPort: Int, overlayID: String,
                               hashAlgorithm:String,
                               hashLength: Int, hashBase:Int)

object BootstrapServerActor {


  def apply(settings: P2PPManagerSettings, name:String)
           (implicit system: ActorSystem): ActorRef = {

      val p2ppManager: P2PPManager =
        new P2PPManager(settings.tcpPort, settings.udpPort, 0, 0, 0, "", "",
          new P2PPMessageFactory, settings.overlayID.getBytes("UTF-8")
        )

      p2ppManager.setOptions(
        new P2POptions(
          P2PPUtils.convertHashAlgorithmName(settings.hashAlgorithm),
          settings.hashLength.toByte,
          P2PPUtils.convertP2PAlgorithmName(
            SuperPeerConstants.SUPERPEER_PROTOCOL_NAME
          ),
          settings.hashBase.toByte,
          settings.overlayID.getBytes("UTF-8")
        )
      )
      apply(p2ppManager, name)
  }

  def apply(p2ppManager: P2PPManager, name:String)(implicit system: ActorSystem): ActorRef = {
    system.actorOf(Props(new BootstrapServerActor(p2ppManager)), name)
  }

 }
