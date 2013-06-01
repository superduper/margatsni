package margatsni.bootstrap.server

import akka.actor.{ActorRef, ActorSystem, Props, Actor}
import pl.edu.pjwstk.p2pp.P2PPManager
import pl.edu.pjwstk.p2pp.objects.P2POptions
import pl.edu.pjwstk.p2pp.superpeer.SuperPeerBootstrapServer
import pl.edu.pjwstk.p2pp.superpeer.SuperPeerConstants
import pl.edu.pjwstk.p2pp.util.P2PPMessageFactory
import pl.edu.pjwstk.p2pp.util.P2PPUtils




class BootstrapServerActor(val manager: P2PPManager) extends Actor {

  import BootstrapServerActor._

  private val server = new SuperPeerBootstrapServer()

  def receive = {

    case Start =>
      manager.addEntity(server)
      manager.start()

    case Stop =>
      manager.stop()

  }
}

case class P2PPManagerSettings(tcpPort: Int, udpPort: Int, overlayID: String,
                               hashAlgorithm:String,
                               hashLength: Int, hashBase:Int)

object BootstrapServerActor {

  case object Start
  case object Stop

  def apply(settings: P2PPManagerSettings, name:String)
           (implicit system: ActorSystem): ActorRef = {

      val p2ppManager: P2PPManager =
        new P2PPManager(settings.tcpPort, settings.udpPort,  0, 0, 0, "", "",
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
