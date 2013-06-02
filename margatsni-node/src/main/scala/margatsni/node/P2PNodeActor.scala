package margatsni.node

import pl.edu.pjwstk.mteam.p2p.P2PNode
import pl.edu.pjwstk.mteam.jcsync.core.{JCSyncStateListener, JCSyncAbstractSharedObject, JCSyncCore}
import akka.actor.{Props, Actor}
import pl.edu.pjwstk.mteam.jcsync.core.implementation.collections.SharedCollectionObject
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectExistsException
import pl.edu.pjwstk.mteam.jcsync.core.consistencyManager.DefaultConsistencyManager
import scala.collection.mutable.{Map => MutableMap}


class P2PNodeActor(node: P2PNode, coreFactory: => JCSyncCore) extends Actor with akka.actor.ActorLogging {
  import MargatsniServiceActor._
  import CallbackHelpers._
  import P2PNodeActor._

  private val collections = MutableMap[String, JCSyncCollection]()
  def receive = {

    case P2PUpdate(collectionName, data) =>
      log.debug("Updating collection <%s> with <%s>".format(collectionName, data))
      updateCollection(collectionName, data)
      log.debug("Updated collection <%s> with <%s>".format(collectionName, data))

    case GetP2PCollection(collectionName) =>
      sender ! getOrCreateCollection(collectionName)
  }

  lazy val core = coreFactory

  override def preStart() {
    log.debug("Started NodeActor")
    node.networkJoin()
    log.debug("Connecting to p2p network")
    node.addCallback(onJoinCallback { node =>
      log.debug("Connected to the p2p network!")
      log.debug("Initializing jcsync core")
      core.init()
      log.debug("Initializing jcsync core: done")
      context.parent ! MargatsniServiceActor.ConnectedToP2PNetwork

    })
  }

  private def updateCollection(name: String, data: java.util.Collection[Any]) = {
    val coll = getOrCreateCollection(name)
    coll.clear()
    coll.addAll(data)
  }

  private def getOrCreateCollection(name: String): JCSyncCollection  = {
    if (collections contains name) {
      collections(name)
    } else {
      val (coll_so, coll) = getOrCreateCollection0(name)
      subscribeCollectionUpdates(name, coll_so)
      collections.put(name, coll)
      coll
    }
  }

  private def getOrCreateCollection0(name: String): (SharedCollectionObject, JCSyncCollection) = {

    var coll = new JCSyncCollection
    var coll_so: SharedCollectionObject = null
    try {
      log.debug(s"Collection <$name> is created")
      coll_so = new SharedCollectionObject(name, coll, core, classOf[DefaultConsistencyManager])
    } catch {
      case e: ObjectExistsException =>
        log.debug(s"Got existing collection  <$name> ")
        coll_so = JCSyncAbstractSharedObject.getFromOverlay(name, core).asInstanceOf[SharedCollectionObject]
        coll = coll_so.getNucleusObject.asInstanceOf[JCSyncCollection]
      case e: Exception =>
        log.error(e, "Error on getOrCreateCollection0")
    }
    (coll_so, coll)
  }

  private def subscribeCollectionUpdates(name: String, coll_so: SharedCollectionObject) {
    coll_so.addStateListener(new JCSyncStateListener {
      def onRemoteStateUpdated(collection: JCSyncAbstractSharedObject, method: String, result: Any) {
        log.info(s"remote state updated, coll:<$collection> method:<$method> result<$result>")
        method match {
          case "addAll" =>
            context.parent ! P2PUpdate(name, collections(name))
          case _ =>
        }

      }
      def onLocalStateUpdated(collection: JCSyncAbstractSharedObject, method: String, result: Any) {
        log.info(s"local state updated, coll:<$collection> method:<$method> result<$result>")
        // bypass
      }
    })
  }

}

object P2PNodeActor {
  import Settings._

  case class P2PUpdate(collection: String, data: NativeCollection)
  case class GetP2PCollection(name: String)

  def apply(settings: NodeSettings): Props = {
    val node = new P2PNode(null, P2PNode.RoutingAlgorithm.SUPERPEER)
    def core = new JCSyncCore(node, settings.corePort)
    node.setBootIP(settings.bootstrapServer.host)
    node.setBootPort(settings.bootstrapServer.port)
    node.setUserName(settings.user)
    node.setTcpPort(settings.listen.port)
    apply(node, core)
  }

  def apply(node: P2PNode, core: => JCSyncCore) =
    Props(new P2PNodeActor(node, core))
}

