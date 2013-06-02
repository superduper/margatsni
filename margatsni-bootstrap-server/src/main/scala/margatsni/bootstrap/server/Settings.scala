package margatsni.bootstrap.server

import com.typesafe.config.{Config, ConfigFactory, ConfigException}

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

class BootstrapServerConfig(config: Config = ConfigFactory.load) {
  // required boilerplate for picking up config

  val configName = "margatsni.bootstrap-server"

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
  val tcpPort = (c getInt "listen.tcp-port")
  val udpPort = (c getInt "listen.udp-port")
  val overlayId = (c getString "overlay-id")
  val hashAlgorithm = (c getString "hash-algorithm")

}

object BootstrapServerConfig {
  implicit def apply(config: Config = ConfigFactory.load()) = new BootstrapServerConfig(config)
}