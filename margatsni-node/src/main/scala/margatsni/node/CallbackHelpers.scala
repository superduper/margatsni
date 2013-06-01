package margatsni.node

import pl.edu.pjwstk.mteam.core.{NetworkObject, Node, NodeCallback}

object CallbackHelpers {
  def onJoinCallback(f: Node => Unit) = {
    new NodeCallback {

      def onDisconnect(p1: Node) {}

      def onUserLookup(p1: Node, p2: Any) {}

      def onObjectLookup(p1: Node, p2: Any) {}

      def onTopicNotify(p1: Node, p2: Any, p3: Array[Byte], p4: Boolean, p5: Short) {}

      def onTopicCreate(p1: Node, p2: Any) {}

      def onTopicCreate(p1: Node, p2: Any, p3: Int) {}

      def onTopicRemove(p1: Node, p2: Any) {}

      def onTopicSubscribe(p1: Node, p2: Any) {}

      def onTopicSubscribe(p1: Node, p2: Any, p3: Int) {}

      def onTopicUnsubscribe(p1: Node, p2: Any, p3: Int) {}

      def onInsertObject(p1: Node, p2: NetworkObject) {}

      def onOverlayError(p1: Node, p2: Any, p3: Int) {}

      def onOverlayError(p1: Node, p2: Any, p3: Int, p4: Int) {}

      def onPubSubError(p1: Node, p2: Any, p3: Short, p4: Int) {}

      def onPubSubError(p1: Node, p2: Any, p3: Short, p4: Int, p5: Int) {}

      def onDeliverRequest(p1: java.util.List[NetworkObject]): Boolean = true

      def onForwardingRequest(p1: java.util.List[NetworkObject]): Boolean = true

      def onBootstrapError(p1: Node, p2: Int) {}

      def onMessageDelivery(p1: java.util.List[NetworkObject]) {}

      def onJoin(p1: Node) {
        f(p1)
      }
    }
  }
}
