package margatsni.node

import com.corundumstudio.socketio.{AckRequest, SocketIOServer, SocketIOClient}
import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener, ConnectListener}

object SocketIoDSL {


  def onConnect(f: SocketIOClient => Unit)(implicit server: SocketIOServer) {
    server.addConnectListener(new ConnectListener {
      def onConnect(client: SocketIOClient) { f(client) }
    })
  }

  def onDisconnect(f: SocketIOClient => Unit)(implicit server: SocketIOServer) {
    server.addDisconnectListener(new DisconnectListener {
      def onDisconnect(client: SocketIOClient) { f(client) }
    })
  }

  def onEvent[DataType](name: String)(f: (SocketIOClient, DataType, AckRequest) => Unit)
                       (implicit server: SocketIOServer, m: Manifest[DataType]) {
    server.addEventListener(name, m.runtimeClass.asInstanceOf[Class[DataType]] , new DataListener[DataType] {
      def onData(client: SocketIOClient, data: DataType, request: AckRequest) {
        f(client, data, request)
      }
    })
  }

  def onMessage(f: (SocketIOClient, String, AckRequest) => Unit)
                       (implicit server: SocketIOServer) {
    server.addMessageListener(new DataListener[String]{
      def onData(client: SocketIOClient, data: String, request: AckRequest) {
        f(client, data, request)
      }
    })
  }

}
