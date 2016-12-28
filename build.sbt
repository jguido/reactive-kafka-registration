
name := "reactive-kafka-scala"

version := "3.0"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.12"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.13",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.12",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion
    exclude("log4j", "log4j")
)

cancelable in Global := true

assemblyJarName in assembly := "kafka-broker.jar"

credentials += Credentials("Sonatype Nexus Repository Manager", "0.0.0.0:8001", "admin", "admin123")

publishTo := {
  val nexus = "http://0.0.0.0:8001"
  Some("releases" at nexus + "/nexus/content/repositories/snapshots")
}