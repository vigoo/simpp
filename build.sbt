name := "simpp"
organization := "io.github.vigoo"
version := "0.1"

scalaVersion := "2.12.4"

resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.0.1",
  "org.atnos" %% "eff" % "5.0.0-20171107064756-b526890",

  "org.specs2" %% "specs2-core" % "4.0.0" % "test"
)

scalacOptions ++= Seq("-Ypartial-unification", "-deprecation")

coverageEnabled in(Test, compile) := true
coverageEnabled in(Compile, compile) := false
