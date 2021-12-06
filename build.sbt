enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

name := "dataops-movie-search"

version := "0.1"

scalaVersion := "2.13.6"

val AkkaVersion = "2.5.32"
val akkaHttpVersion = "10.2.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % "7.6.1",
  "com.github.pureconfig" %% "pureconfig" % "0.17.0"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.10" % Test,
  "org.testcontainers" % "testcontainers" % "1.16.2" % Test,
  "org.testcontainers" % "elasticsearch" % "1.16.2" % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
)

packageName in Docker := "dataops/movie-search"
dockerExposedPorts ++= Seq(9000)

dockerBaseImage := "openjdk:8-jre"

dockerEntrypoint := Seq("/opt/docker/bin/dataops-movie-search")
daemonUserUid in Docker := Some("1000")
daemonUser in Docker    := "appuser"
mappings in Universal += file("src/main/resources/static/index.html") -> "resources/index.html"
mappings in Universal += file("src/main/resources/static/css/main.css") -> "resources/css/main.css"
mappings in Universal += file("src/main/resources/static/js") -> "resources/js"


scapegoatVersion in ThisBuild := "1.4.10"
scapegoatReports := Seq("xml")
scalacOptions in Scapegoat += "-P:scapegoat:overrideLevels:all=Warning"

import sbtsonar.SonarPlugin.autoImport.sonarProperties
sonarProperties := Map(
  "sonar.projectName" -> name.value.replace("-", " "),
  "sonar.projectKey" -> name.value,
  "sonar.sources" -> "src/main/scala",
  "sonar.sourceEncoding" -> "UTF-8",
  "sonar.scoverage.reportPath" -> "target/scala-2.13/scoverage-report/scoverage.xml",
  "sonar.scala.scapegoat.reportPath" -> "target/scala-2.13/scapegoat-report/scapegoat.xml"
)
