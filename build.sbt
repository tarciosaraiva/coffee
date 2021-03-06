name := "coffee"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
    jdbc,
    anorm,
    filters,
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
    "org.apache.poi" % "poi" % "3.9",
    "org.apache.poi" % "poi-ooxml" % "3.9",
    "org.apache.poi" % "poi-ooxml-schemas" % "3.9",
    "org.apache.poi" % "poi-scratchpad" % "3.9",
    "com.typesafe" %% "play-plugins-mailer" % "2.2.0"
)

play.Project.playScalaSettings

scalacOptions ++= Seq(
    "-feature",
    "-language:postfixOps"
)
