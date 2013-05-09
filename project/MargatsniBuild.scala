import sbt._
import Keys._

object MargatsniBuild extends Build {
  import BuildSettings._
  import Dependencies._ 
  import AssemblySettings._

  // configure prompt to show current project
  override lazy val settings = super.settings :+ {
    shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
  }

  // -------------------------------------------------------------------------------------------------------------------
  // 3rd party unmanaged jars
  // -------------------------------------------------------------------------------------------------------------------

  var unmanagedListing = unmanagedJars in Compile :=  {
    listUnmanaged( file(".").getAbsoluteFile )
  }

  // -------------------------------------------------------------------------------------------------------------------
  // Root Project
  // -------------------------------------------------------------------------------------------------------------------

  lazy val root = Project( "root", file("."))
    .aggregate(margatsniBootstrapServer, margatsniNode)
    .settings(basicSettings: _*)
    .settings(g8Scaffolding: _*)

  // -------------------------------------------------------------------------------------------------------------------
  // Bootstrap Server
  // -------------------------------------------------------------------------------------------------------------------

  lazy val margatsniBootstrapServer = Project("margatsni-bootstrap-server", file("margatsni-bootstrap-server"))
    .dependsOn(zappPlatformApi, zappSharedApi, zappAdminCore)
    .settings(moduleSettings: _*)
    .settings(revolverSettings: _*)
    .settings(margatsniBootstrapServerAssembly: _*)


  // -------------------------------------------------------------------------------------------------------------------
  // Admin Cli
  // -------------------------------------------------------------------------------------------------------------------

  lazy val margatsniNode = Project("margatsni-node", file("margatsni-node"))
    .dependsOn(zappAdminCore)
    .settings(moduleSettings: _*)
    .settings(revolverSettings: _*)   
    
}
