name := "coffee"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
    jdbc,
    anorm,
    filters,
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4"
)

play.Project.playScalaSettings
