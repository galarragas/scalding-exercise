import _root_.sbtassembly.Plugin.MergeStrategy
import _root_.sbtassembly.Plugin.PathList
import net.imagini.sbt.ExtraSettings._
import sbt._
import sbt.Keys._
import sbtassembly.Plugin.AssemblyKeys._
import sbtassembly.Plugin._
import sbtrelease._
import ReleaseStateTransformations._
import sbtrelease.ReleasePlugin.ReleaseKeys._
import sbtrelease.ReleasePlugin._
import scala.Some

// TODO Should rename to MyAppBuild
object ScaldingExerciseBuild extends Build {
  val appName = "log-analysis"
  val companyName = "barclays"
  val domain = "com"

  val assemblyStrategy : Seq[sbt.Project.Setting[_]] = Seq(
    excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
      val excludes = Set("jsp-api-2.1-6.1.14.jar", "jsp-2.1-6.1.14.jar",
        "jasper-compiler-5.5.12.jar", "janino-2.5.16.jar")
      cp filter { jar => excludes(jar.data.getName)}
    },
    // Some of these files have duplicates, let's ignore:
    mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
    {
      case s if s.endsWith(".class") => MergeStrategy.last
      case s if s.endsWith("project.clj") => MergeStrategy.concat
      case s if s.endsWith("xsd") => MergeStrategy.last
      case s if s.endsWith("dtd") => MergeStrategy.last
      case s if s.endsWith("xml") => MergeStrategy.last
      case s if s.endsWith("html") => MergeStrategy.last
      case s if s.endsWith("txt") => MergeStrategy.last
      case s if s.endsWith("properties") => MergeStrategy.last
      case s if s.endsWith("jersey-module-version") => MergeStrategy.last
      case PathList("org", "joda", "time",xs @ _*) => MergeStrategy.last
      case x => old(x)
    }
    })

  val mavenRepo: String = "maven." + companyName + "." + domain

  val mavenPublishSettings = Seq(

    publishMavenStyle := true,

    publishTo <<= isSnapshot {
      (ss: Boolean) =>
        val nexus = "https://" + mavenRepo + "/nexus/"
        if (ss || isRC)
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "content/repositories/releases")
    },

    credentials ++= {
      val sonatype = ("Sonatype Nexus Repository Manager", mavenRepo)
      def loadMavenCredentials(file: java.io.File): Seq[Credentials] = {
        xml.XML.loadFile(file) \ "servers" \ "server" map (s => {
          val host = (s \ "id").text
          val realm = sonatype._1
          val hostToUse = mavenRepo
          Credentials(realm, hostToUse, (s \ "username").text, (s \ "password").text)
        })
      }
      val ivyCredentials = Path.userHome / ".ivy2" / ".credentials"
      val mavenCredentials = Path.userHome / ".m2" / "settings.xml"
      (ivyCredentials.asFile, mavenCredentials.asFile) match {
        case (ivy, _) if ivy.canRead => Credentials(ivy) :: Nil
        case (_, mvn) if mvn.canRead => loadMavenCredentials(mvn)
        case _ => Nil
      }
    }

  )

  val dependencySettings : Seq[sbt.Project.Setting[_]] = Seq(
    resolvers ++= Seq(
      "Concurrent Maven Repo" at "http://conjars.org/repo",
      "mvnrepository" at "https://repository.cloudera.com/artifactory/cloudera-repos/",
    "repo.codahale.com" at "http://repo.codahale.com",
      "maven" at "http://mvnrepository.com/artifact"

      ),

    resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo),

    libraryDependencies ++= Seq(
      "com.github.seratch" %% "awscala" % "[0.2,)",
      "org.scalacheck" %% "scalacheck" % "1.10.1" % "test",
      "org.specs2" %% "specs2" % "1.14" % "test",
      "org.apache.hadoop" % "hadoop-common" % "2.3.0-cdh5.0.0",
      "org.apache.hadoop" % "hadoop-hdfs" % "2.3.0-cdh5.0.0",
      "org.apache.hadoop" % "hadoop-client" % "2.3.0-cdh5.0.0",
      "com.twitter" % "scalding-core_2.10" % "0.11.1",
      "org.apache.spark" % "spark-core_2.10" % "0.9.0-cdh5.0.0",
      "com.cloudphysics" %% "jerkson" % "0.6.3",//jerkson
      "com.amazonaws" % "aws-java-sdk" % "1.8.2",
      "net.java.dev.jets3t" % "jets3t" % "0.9.0",
      "org.scalaz" %% "scalaz-core" % "7.0.5",
      "net.minidev" % "json-smart" % "1.2",
      "org.apache.commons" % "commons-compress" % "1.5"

    )
  )

  val generalSettings : Seq[sbt.Project.Setting[_]]  = Seq(
    scalaVersion := "2.10.3",
    javaOptions ++= Seq("-target", "1.7", "-source", "1.7"),
    organization := domain + "." + companyName,
    mainClass in assembly := None,
    mainClass in Compile := None,
    name := appName,
    parallelExecution in Test := false,
    commands ++= Seq(snapshotCommandCurrentProject, releaseCandidateCommandCurrentProject),
    releaseProcess := Seq[ReleaseStep](
      checkoutBranch("master"),
      checkSnapshotDependencies,
      inquireVersions,
      runTest,
      publishArtifacts,
      tagRelease,
      checkoutBranch("develop")
    )
  )
  jarName in assembly := name + "-" + version + "-assembly.jar"

  lazy val rdCore: Project = Project(appName, base = file(".")).
    settings(assemblySettings ++ releaseSettings ++ generalSettings ++ dependencySettings ++ mavenPublishSettings ++
    net.virtualvoid.sbt.graph.Plugin.graphSettings  ++ assemblyStrategy : _*).
    settings(addCommandAlias("releaseWithDefaults", "release with-defaults"): _*)

}
