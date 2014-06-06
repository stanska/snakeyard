import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "snakeyard"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Select Play modules
    javaCore, // The core Java API
    "org.webjars" % "webjars-play" % "2.1.0",
    "org.webjars" % "bootstrap" % "2.3.1",
    "org.mockito" % "mockito-all" % "1.9.0" % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test",
    "com.typesafe.akka" %% "akka-actor" % "2.1.0" % "test",
    "com.typesafe.akka" %% "akka-testkit" % "2.1.0" % "test",
    "org.scalacheck" %% "scalacheck" % "1.10.1" % "test")

  val main = play.Project(appName, appVersion, appDependencies).settings(
    scalaVersion := "2.10.1" // Add your own project settings here      
    )

}
