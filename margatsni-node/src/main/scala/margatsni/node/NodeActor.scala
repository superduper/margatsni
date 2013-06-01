package margatsni.node

import pl.edu.pjwstk.mteam.p2p.P2PNode
import pl.edu.pjwstk.mteam.jcsync.core.{JCSyncStateListener, JCSyncAbstractSharedObject, JCSyncCore}
import akka.actor.{Props, Actor}
import pl.edu.pjwstk.mteam.jcsync.core.implementation.collections.{JCSyncArrayList, SharedCollectionObject, JCSyncHashMap}
import pl.edu.pjwstk.mteam.jcsync.exception.ObjectExistsException
import pl.edu.pjwstk.mteam.jcsync.core.consistencyManager.DefaultConsistencyManager
import scala.collection.mutable.{Map => MutableMap}
import com.corundumstudio.socketio.SocketIOClient
import akka.pattern.ask
import akka.util.Timeout
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext

case class HostAndPort(host: String, port: Int)

case class NodeSettings(user: String, listen: HostAndPort, bootstrapServer: HostAndPort )

object MargatsniServiceActor {

  type NativeCollection = java.util.Collection[Any]
  type JCSyncCollection = JCSyncArrayList[Any]

  case object ConnectedToP2PNetwork
  case class Subscribe(collectionName: String, client: SocketIOClient)
  case class ClientUpdate(originator: SocketIOClient, collection: String, data: NativeCollection)
  case class P2PUpdate(collection: String, data: NativeCollection)
  case class ClientDisconnect(client: SocketIOClient)
  case class GetP2PCollection(name: String)

  def apply(nodeSettings: NodeSettings, socketIoSettings: HostAndPort): Props =
    Props(new MargatsniServiceActor(nodeSettings, socketIoSettings))

}

class MargatsniServiceActor(nodeSettings: NodeSettings,
                            socketIoSettings: HostAndPort) extends Actor with akka.actor.ActorLogging {
  import MargatsniServiceActor._
  final implicit val timeout = Timeout(1000)

  def actorRefFactory = context
  implicit def ec: ExecutionContext = context.dispatcher

  private val subscriptions = MutableMap[String, List[SocketIOClient]]()
                              .withDefaultValue(List[SocketIOClient]())
  private var socketIoStarted: Boolean = false


  def receive = {
    case ConnectedToP2PNetwork =>
      startSocketIoServer()
      
    case Subscribe(collectionName, client) =>
      subscribeCollection(collectionName, client)

    case ClientUpdate(originator, collectionName, data) =>
      notifyCollectionSubscribers(originator, collectionName, data)
      updateP2PCollection(collectionName, data)

    case P2PUpdate(collectionName, data) =>
      notifyCollectionSubscribers(collectionName, data)

    case ClientDisconnect(client) =>
      unSubscribeAllCollections(client)
  }

  override def preStart() {
    actorRefFactory.actorOf(P2PNodeActor(nodeSettings), "p2p")
  }

  private def startSocketIoServer() {
    if (!socketIoStarted) {
      actorRefFactory.actorOf(SocketIoServerActor(socketIoSettings), "socketIo")
      socketIoStarted = true
    } else
      throw new RuntimeException("SocketIO server has been already started. Awkward.")
  }

  private def updateP2PCollection(name: String, data: NativeCollection) {
    context.actorFor("p2p") ! P2PUpdate(name, data)
  }

  private def subscribeCollection0(name: String, client: SocketIOClient)  {
    val isSubscribed = (subscriptions(name) contains client)
    if (!isSubscribed) {
      subscriptions(name) = subscriptions(name) ::: List(client)
      log.debug("Updated list of subscribers :%s" format subscriptions(name))
    }
  }

  private def subscribeCollection(name: String, client: SocketIOClient) {
    subscribeCollection0(name, client)
    sendCollection(name, client)
  }

  //
  // Gets collection from p2p and forwards it to socketIoClient
  //
  private def sendCollection(name: String, client: SocketIOClient) {
    (
      (context.actorFor("p2p") ? GetP2PCollection(name)).mapTo[JCSyncCollection]
    ) onComplete {
      case Success(data) =>
        sendCollectionUpdate(client, name, data)
      case Failure(cause: Throwable) =>
        log.error(cause, "Failed to get collection")
    }
  }

  private def unSubscribeCollection(name: String, client: SocketIOClient) {
    val isSubscribed = (subscriptions(name) contains client)
    if (!isSubscribed) {
      subscriptions(name) = subscriptions(name).filterNot(_ == client)
      log.debug("Updated list of subscribers :%s" format subscriptions(name))
    }
  }
  
  private def unSubscribeAllCollections(client: SocketIOClient) {
    subscriptions.keys map {
      unSubscribeCollection(_, client)
    }
  }

  ///
  // Notify all collection subscribers except originator
  ///
  private def notifyCollectionSubscribers(originator: SocketIOClient, collectionName: String, data: NativeCollection) {
    notifyCollectionSubscribers(subscriptions(collectionName) filterNot(_ == originator), collectionName, data)
  }

  ///
  // Notify all collection subscribers
  ///

  private def notifyCollectionSubscribers(collectionName: String, data: NativeCollection) {
    notifyCollectionSubscribers(subscriptions(collectionName), collectionName, data)
  }

  ///
  // Notify subscribers
  ///

  private def notifyCollectionSubscribers(subscribers: List[SocketIOClient], collectionName: String, data: NativeCollection) {
    subscribers map (sendCollectionUpdate(_, collectionName, data))
  }

  ///
  // Notify socketIoClient with an update
  ///

  private def sendCollectionUpdate(client: SocketIOClient, collectionName: String, data: NativeCollection) {
    context.actorFor("socketIo") ! ClientUpdate(client, collectionName, data)
  }
}


class P2PNodeActor(node: P2PNode) extends Actor with akka.actor.ActorLogging {
  import MargatsniServiceActor._
  import CallbackHelpers._

  private val collections = MutableMap[String, JCSyncCollection]()

  def receive = {

    case P2PUpdate(collectionName, data) =>
      log.debug("Updating collection <%s> with <%s>".format(collectionName, data))
      updateCollection(collectionName, data)
      log.debug("Updated collection <%s> with <%s>".format(collectionName, data))

    case GetP2PCollection(collectionName) =>
      sender ! getOrCreateCollection(collectionName)
  }

  override def preStart() {
    Console.println("Started NodeActor")
    node.networkJoin()
    Console.println("Connecting to p2p network")
    node.addCallback(onJoinCallback { node =>
      Console.println("Connected to the p2p network!")
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
    val core = new JCSyncCore(node, 31337)
    var coll = new JCSyncCollection
    var coll_so: SharedCollectionObject = null

    core.init()
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
        Console.println(s"remote state updated, coll:<$collection> method:<$method> result<$result>")
        context.parent ! P2PUpdate(name, result.asInstanceOf[JCSyncCollection])
      }

      def onLocalStateUpdated(collection: JCSyncAbstractSharedObject, method: String, result: Any) {
        Console.println(s"local state updated, coll:<$collection> method:<$method> result<$result>")
        // bypass
      }
    })
  }

}


object P2PNodeActor {

  def apply(settings: NodeSettings): Props = {
    val node =  new P2PNode(null, P2PNode.RoutingAlgorithm.SUPERPEER)
    node.setBootIP(settings.bootstrapServer.host)
    node.setBootPort(settings.bootstrapServer.port)
    node.setUserName(settings.user)
    node.setTcpPort(settings.listen.port)
    apply(node)
  }

  def apply(node: P2PNode) =
    Props(new P2PNodeActor(node))
}

