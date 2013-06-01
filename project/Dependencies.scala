import sbt._

object Dependencies {

  val resolutionRepos = Seq(    
    "Sonatype Releases"  at "https://oss.sonatype.org/content/repositories/releases/",
    "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    "Sonatype Backup" at "https://oss.sonatype.org/service/local/repositories/releases/content/",
    "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
    "Typesafe Releases Backup" at "http://repo.typesafe.com/typesafe/repo/",
    "jboss repo" at "http://repository.jboss.org/nexus/content/groups/public-jboss/"
  )

  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def runtime   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")
  def container (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")

  def akkaModule  (id: String) = "com.typesafe.akka" %% id % akkaVersion

  val akkaVersion  = "2.1.2"

  val akkaActor     = akkaModule("akka-actor")
  val akkaTestKit   = akkaModule("akka-testkit")
  val akkaRemote    = akkaModule("akka-remote")
  val akkaSlf4j     = akkaModule("akka-slf4j")
  val akkaUtil      = akkaModule("akka-util")
  
  val specs2          = "org.specs2"                  %% "specs2"           % "1.13"
  val typesafeConfig  = "com.typesafe"                %  "config"           % "1.0.0"
  val log4j           = "log4j"                       %  "log4j"            % "1.2.17" exclude("javax.jms", "jms")
  val bouncyCastle    = "org.bouncycastle"            %  "bcprov-jdk16"     % "1.45"
  val jacksonDatabind = "com.fasterxml.jackson.core"  %  "jackson-databind" % "2.2.1"
  val sfl4j           = "org.slf4j"                   %  "slf4j-simple"     % "1.6.4"
  val ritsCloning     = "uk.com.robust-it"            % "cloning"           % "1.9.0"

  //  val netty           = "org.jboss.netty"             %  "netty"            % "3.2.9.Final"

  def listUnmanaged( base : RichFile ) : Keys.Classpath = {
    val baseDirectories = (base / "lib")
    (baseDirectories ** "*.jar").classpath
  }

}
