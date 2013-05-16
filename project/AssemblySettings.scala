import sbt.Keys._
import sbtassembly.Plugin._
import sbtassembly.Plugin.AssemblyKeys._
import spray.revolver.RevolverPlugin.Revolver

object AssemblySettings {

  lazy val margatsniBootstrapServerAssembly = assemblySettings ++ Seq(
    mainClass in assembly := Some("margatsni.bootstrap.server.main.Boot"),
    jarName in assembly := "margatsni-bootstrap-server.jar",
    test in assembly := {},
    javaOptions in Revolver.reStart += "-Dfile.encoding=UTF8"
  )

  lazy val margatsniNodeAssembly = assemblySettings ++ Seq(
    mainClass in assembly := Some("margatsni.node.main.Boot"),
    jarName in assembly := "margatsni-node.jar",
    test in assembly := {},
    javaOptions in Revolver.reStart += "-Dfile.encoding=UTF8"
  )

}