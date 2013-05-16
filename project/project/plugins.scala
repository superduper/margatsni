import sbt._
import Keys._

object PluginDef extends Build {

  lazy val root = Project("plugins", file(".")).dependsOn(
    ProjectRef(uri("git://github.com/superduper/giter8"), "giter8-scaffold"))
    .settings(Classpaths.baseSettings: _*)
    .settings(
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    scalaVersion := "2.9.2",
    scalaBinaryVersion := "2.9.2",

    resolvers ++= Seq(
      "Maven" at "repo.maven.org",
      "Sonatype Releases"  at "https://oss.sonatype.org/content/repositories/releases/",
      "Sonatype Backup" at "https://oss.sonatype.org/service/local/repositories/releases/content/",
      "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
      "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
      "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
      "Scalasbt" at "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases",
      "Typesafe Backup Releases " at "http://repo.typesafe.com/typesafe/repo/"),

    addSbtPlugin("io.spray" % "sbt-revolver" % "0.6.2"),    
    addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.3.0-SNAPSHOT"),
    addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.8.8")
  )

}