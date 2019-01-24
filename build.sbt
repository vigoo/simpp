name := "simpp"
organization := "io.github.vigoo"
version := "0.2-SNAPSHOT"

scalaVersion := "2.12.4"

resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.5.0",
  "org.atnos" %% "eff" % "5.4.1",

  "org.specs2" %% "specs2-core" % "4.0.5" % "test",
  "org.specs2" %% "specs2-junit" % "4.0.5" % "test"
)

scalacOptions ++= Seq("-Ypartial-unification", "-deprecation")

coverageEnabled in(Test, compile) := true
coverageEnabled in(Compile, compile) := false


// Publishing

publishMavenStyle := true

licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

publishTo := sonatypePublishTo.value

import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("vigoo", "simpp", "daniel.vigovszky@gmail.com"))

developers := List(
  Developer(id="vigoo", name="Daniel Vigovszky", email="daniel.vigovszky@gmail.com", url=url("https://vigoo.github.io"))
)

credentials ++=
  (for {
    username <- Option(System.getenv().get("SONATYPE_USERNAME"))
    password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
  } yield
    Credentials(
      "Sonatype Nexus Repository Manager",
      "oss.sonatype.org",
      username,
      password)).toSeq
