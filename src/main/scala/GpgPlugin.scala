package com.rossabaker.sbt.gpg

import _root_.sbt._
import Process._

trait GpgPlugin extends BasicManagedProject {
  def gpgCommand: String = "gpg"

  lazy val sign = signAction

  def signAction = signTask(artifacts) 
    .dependsOn(super.publishAction.dependencies : _*)
    .describedAs("Signs each artifact with GnuPG.")

  def signTask(artifacts: Iterable[Artifact]): Task = task {
    artifacts.toStream flatMap signArtifact firstOption
  }

  def signArtifact(artifact: Artifact): Option[String] = {
    val path = artifact2Path(artifact)
    List(gpgCommand, "-ab", path).mkString(" ") ! match {
      case 0 => None
      case _ => Some("error signing artifact: "+path)
    }    
  }

  protected def artifact2Path(artifact: Artifact): Path = {
    val filename = 
      artifactBaseName + 
      (artifact.classifier map { "-"+_ } getOrElse "") +
      "."+artifact.extension
    outputPath / filename
  }

  override def publishAction = super.publishAction dependsOn(sign)
}
