import sbt._
import Keys._
import spray.revolver.RevolverPlugin.Revolver


object BuildSettings {
  import Dependencies._

  lazy val basicSettings = Seq(
    version               := "0.1",    
    organization          := "Margatsni",
    description           := "Margatsni – p2p instagram clone ",
    startYear             := Some(2013),    
    scalaVersion          := "2.10.1",
    resolvers             ++= Dependencies.resolutionRepos,
    scalacOptions         := Seq(
      "-encoding", "utf8",
      "-feature",
      "-unchecked",
      "-deprecation",
      "-target:jvm-1.6",
      "-language:postfixOps",
      "-language:implicitConversions",
      "-language:reflectiveCalls",
      "-Xlog-reflective-calls",
      "-Ywarn-adapted-args"
    ),
    libraryDependencies ++= compile(typesafeConfig)
  )

  lazy val moduleSettings = basicSettings
  lazy val g8Scaffolding = giter8.ScaffoldPlugin.scaffoldSettings
  lazy val revolverSettings = Revolver.settings

}