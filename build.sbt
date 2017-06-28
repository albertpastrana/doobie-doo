import sbt.Keys._

lazy val commonSettings = Seq(
  scalaVersion := "2.12.2",
  organization := "com.intenthq",
  scalacOptions in Test ++= Seq("-Yrangepos"),
  scalacOptions ++= Seq(
    "-Xlint",
    "-Xcheckinit",  // Remove for performance improvement
    "-Xfatal-warnings",
    "-unchecked",
    "-deprecation",
    "-feature")
)

lazy val root = project.in(file("."))
    .settings(commonSettings: _*)
    .settings(testFrameworks in Test := Seq(sbt.TestFrameworks.Specs2))
    .settings(
      testOptions in Test := Seq(Tests.Argument(TestFrameworks.Specs2, "showtimes", "true", "timeFactor", "5")))

lazy val versions = new {
  val doobie = "0.4.1"
}

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.jcenterRepo,
  "Twitter Maven" at "https://maven.twttr.com",
  "Keyczar Maven" at "https://raw.githubusercontent.com/google/keyczar/master/java/maven/"
)

libraryDependencies ++= Seq(
  "org.flywaydb" % "flyway-core" % "4.2.0",
  "org.tpolecat" %% "doobie-core-cats" % versions.doobie,
  "org.tpolecat" %% "doobie-hikari-cats" % versions.doobie,
  "org.tpolecat" %% "doobie-postgres-cats" % versions.doobie,
  "org.tpolecat" %% "doobie-specs2-cats" % versions.doobie % "test",
  "org.specs2" %% "specs2-core" % "3.9.1" % "test"
)
