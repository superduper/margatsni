package margatsni.node


object Settings {
  case class HostAndPort(host: String, port: Int)
  case class NodeSettings(user: String, listen: HostAndPort, bootstrapServer: HostAndPort, corePort: Int)

}
