package margatsni.node

import com.typesafe.config.{Config, ConfigFactory, ConfigException}

object Settings {
  case class HostAndPort(host: String, port: Int)
  case class NodeSettings(user: String, listen: HostAndPort, bootstrapServer: HostAndPort, corePort: Int)
}

trait TypesafeConfig {
  protected val c: Config

  protected def asOpt[T](v: => T): Option[T] = {
    try {
      Option(v)
    } catch {
      case e: ConfigException.Missing => None
      case e: Throwable => throw e
    }
  }

}

class NodeConfig(config: Config = ConfigFactory.load){
  import Settings._
  // required boilerplate for picking up config

  val configName = "margatsni.node"

  val projectConfig = config.getConfig(configName)

  protected val c: Config = prepareSubConfig(config, configName)

  def prepareSubConfig(config: Config, path: String): Config = {
    val c = config.withFallback(referenceConfig)
    c.checkValid(referenceConfig, path)
    c.getConfig(path)
  }

  lazy val referenceConfig = ConfigFactory.defaultReference
  ///
  /// config
  ///
  val node = NodeSettings(
    user = (c getString "p2p-node.user"),
    listen = HostAndPort(
      host = (c getString "p2p-node.listen.host"),
      port = (c getInt "p2p-node.listen.port")
    ),
    corePort = (c getInt "p2p-node.jcsync-core.listen.port"),
    bootstrapServer = HostAndPort(
      host = (c getString "p2p-node.bootstrap-server.host"),
      port = (c getInt "p2p-node.bootstrap-server.port")
    )
  )
  val socketIo = HostAndPort(
    host = (c getString "socket-io.listen.host"),
    port = (c getInt "socket-io.listen.port")
  )

}

object NodeConfig {
  implicit def apply(config: Config = ConfigFactory.load()) = new NodeConfig(config)
}