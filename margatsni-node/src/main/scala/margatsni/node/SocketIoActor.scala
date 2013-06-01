package margatsni.node

import akka.actor.{Props, Actor}
import com.corundumstudio.socketio.{SocketIOClient, SocketIOServer, Configuration}
import scala.concurrent.ExecutionContext
import akka.util.Timeout
import margatsni.node.MargatsniServiceActor.{ClientUpdate, Subscribe, NativeCollection}


object SocketIoServerHelpers {
  import SocketIoServerActor._

  def marshalCollection(name: String, data: NativeCollection): UpdatePayload = {
    val wrapped = new java.util.LinkedHashMap[String, Object]()
    wrapped.put("collection", name)
    wrapped.put("data", data)
    wrapped
  }

  def unmarshalCollection(payload: UpdatePayload): (String, java.util.ArrayList[Any]) = {
    val name = payload.get("collection").asInstanceOf[String]
    val data = payload.get("data").asInstanceOf[java.util.ArrayList[Any]]
    (name, data)
  }

}

class SocketIoServerActor(config: Configuration) extends Actor with akka.actor.ActorLogging {
  import SocketIoDSL._
  import SocketIoServerHelpers._
  import SocketIoServerActor._

  final implicit val timeout = Timeout(1000)
  implicit val server: SocketIOServer = new SocketIOServer(config)
  implicit def ec: ExecutionContext = context.dispatcher

  override def preStart() {
    server.start()
  }

  override def postStop() {
    server.stop()
    //TODO: forward to message to Node, in order to remove all subscribers
  }

  def receive = {
    case ClientUpdate(client, collection, data) =>
      updateClient(client, collection, data)
  }

  private def updateClient(client: SocketIOClient, collection: String, data: NativeCollection) {
    if (client.isChannelOpen)
      client.sendEvent("update", marshalCollection(collection, data))
     else
      log.error(s"Cant send <$collection> collection update to client <$client> when channel is closed")
  }

  ///
  //  Event handlers
  ///

  onConnect { client =>
    Console.println("Client %s connected".format(client.getSessionId.toString))
  }

  onDisconnect { client =>
    Console.println("Client %s disconnected".format(client.getSessionId.toString))
  }

  onEvent[SubscribePayload]("subscribe") { (client, payload, request) =>
    val collectionName =  payload.get("collection")
    log.debug(s"Client <$client> subscribes to <$collectionName> collection updates")
    context.parent ! Subscribe(collectionName, client)
  }

  onEvent[UpdatePayload]("update") { (client, payload, request) =>
    val (name, data) = unmarshalCollection(payload)
    log.debug(s"Got update for <$name> collection, from: <$client> with payload: <$payload>")
    context.parent ! ClientUpdate(client, name, data)
  }

}


object SocketIoServerActor {

  type SubscribePayload = java.util.LinkedHashMap[String, String]
  type UpdatePayload = java.util.LinkedHashMap[String, Object]

  def apply(settings: HostAndPort): Props =
    apply(settings.host, settings.port)

  def apply(host: String, port: Int): Props = {
    val config = new Configuration()
    config.setHostname(host)
    config.setPort(port)
    apply(config)
  }

  def apply(config: Configuration) =
    Props(new SocketIoServerActor(config))

}