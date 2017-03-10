name := """PlayAssignment"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

//libraryDependencies += "org.webjars" % "bootstrap" % "3.3.4"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.mindrot" % "jbcrypt" % "0.3m",
  specs2 % Test,
  "org.mockito" % "mockito-core" % "1.9.5"
)


