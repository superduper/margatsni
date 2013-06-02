package margatsni.node


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
