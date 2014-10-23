package net.imagini.sbt

import sbt._
import sbt.Keys._
import sbtrelease.ReleasePlugin.ReleaseKeys._
import sbtrelease._
import sbtrelease.Utilities._
import sbtrelease.ReleaseStateTransformations._

object ExtraSettings {
  var isRC: Boolean = false

  def cmd(args: Any*): ProcessBuilder = Process("git" +: args.map(_.toString))


  def checkoutBranch(branch : String) = ReleaseStep(
    action = {
      st: State =>
        val vc = st.extract.get(versionControlSystem).getOrElse(sys.error("Aborting release. Working directory is not a repository of a recognized VCS."))
        vc.checkRemote("origin " + branch)
        if ((cmd("checkout", branch) !) != 0) {
          sys.error("Aborting the release! Couldn't check out " + branch)
        }
        st
    }
  )

  // Stuff above I think is pointless - I don't like sbt doing this weird git stuff, it just breaks all the time

  def snapshotCommandCurrentProject = Command.command("snapshot") {
    state =>
      val extracted = Project.extract(state)
      val eVersion = extracted.getOpt(version).get // getting current version

      extracted.append(List(version := eVersion + "-SNAPSHOT"), state)
  }

  def releaseCandidateCommandCurrentProject = Command.command("release-candidate") {
    state =>
      val extracted = Project.extract(state)
      val eVersion = extracted.getOpt(version).get // getting current version

//      isRC = true

      extracted.append(List(version := eVersion + "-RC"), state)
  }
}
