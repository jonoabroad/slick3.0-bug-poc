name := "slick3-bug"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick"       % "3.0.0",
  "org.postgresql"     %  "postgresql"  % "9.3-1100-jdbc41",
  "ch.qos.logback"      % "logback-classic" % "1.1.2"  % Runtime
)
