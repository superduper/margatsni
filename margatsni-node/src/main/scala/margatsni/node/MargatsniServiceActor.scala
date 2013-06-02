package margatsni.node

import akka.actor.{Props, Actor}
import scala.collection.mutable.{Map => MutableMap}
import com.corundumstudio.socketio.SocketIOClient
import akka.pattern.ask
import akka.util.Timeout
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext

import Settings._

object MargatsniServiceActor {
  case object ConnectedToP2PNetwork
  def apply(nodeSettings: NodeSettings, socketIoSettings: HostAndPort): Props =
    Props(new MargatsniServiceActor(nodeSettings, socketIoSettings))

}


class MargatsniServiceActor(nodeSettings: NodeSettings,
                            socketIoSettings: HostAndPort) extends Actor with akka.actor.ActorLogging {
  import MargatsniServiceActor._
  import P2PNodeActor._
  import SocketIoServerActor._

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