name := "JPaxos"

javaSource in Compile := baseDirectory.value / "src"
javaSource in Test := baseDirectory.value / "test"

crossPaths := false
autoScalaLibrary := false


val logbackVer = "1.2.3"
val slf4jVer = "1.7.25"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-core" % logbackVer,
  "ch.qos.logback" % "logback-classic" % logbackVer,

  "org.slf4j" % "slf4j-api" % slf4jVer,
  "org.slf4j" % "slf4j-simple" % slf4jVer,
  "org.slf4j" % "slf4j-log4j12" % slf4jVer,
  "org.slf4j" % "slf4j-jdk14" % slf4jVer,

  "org.mockito" % "mockito-all" % "1.10.19",
  "junit" % "junit" % "4.12"
)

