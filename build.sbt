name := "simpp"
organization := "io.github.vigoo"
version := "0.1"

scalaVersion := "2.12.4"

resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.1.0",
  "org.atnos" %% "eff" % "5.3.0",

  "org.specs2" %% "specs2-core" % "4.0.0" % "test",
  "org.specs2" %% "specs2-junit" % "4.0.0" % "test"
)

scalacOptions ++= Seq("-Ypartial-unification", "-deprecation")

coverageEnabled in(Test, compile) := true
coverageEnabled in(Compile, compile) := false


// Publishing

publishMavenStyle := true

pomIncludeRepository := { _ => false }

isSnapshot := version.value endsWith "SNAPSHOT"

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <url>https://github.com/vigoo/simpp</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:vigoo/simpp.git</url>
      <connection>scm:git:git@github.com:vigoo/simpp.git</connection>
    </scm>
    <developers>
      <developer>
        <id>vigoo</id>
        <name>Daniel Vigovszky</name>
        <url>https://github.com/vigoo</url>
      </developer>
    </developers>)

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

