package margatsni

import pl.edu.pjwstk.mteam.jcsync.core.implementation.collections.JCSyncArrayList


package object node {

  /// P2P type aliases

  type NativeCollection = java.util.Collection[Any]
  type JCSyncCollection = JCSyncArrayList[Any]

  /// SocketIO type aliases

  type SubscribePayload = java.util.LinkedHashMap[String, String]
  type UpdatePayload = java.util.LinkedHashMap[String, Object]

}
