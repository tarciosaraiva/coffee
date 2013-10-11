name := "coffee"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
    jdbc,
    anorm,
    filters,
    "mysql" % "mysql-connector-java" % "5.1.26"
)

play.Project.playScalaSettings
