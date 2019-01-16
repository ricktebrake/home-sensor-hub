name := "home-sensor-hub"

version := "0.1"

scalaVersion := "2.12.8"

lazy val akkaVersion = "2.5.16"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.lightbend.akka" %% "akka-stream-alpakka-mqtt" % "1.0-M1"
)