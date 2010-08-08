package com.rossabaker.sbt.gpg

import _root_.sbt._
import Process._
import scala.xml._

trait GpgPlugin extends BasicManagedProject {
  def gpgCommand: String = "gpg"

  lazy val sign = signAction

  val signaturesConfig = config("signatures") 

  // Crude hack.  Issue #2: we don't want signatures to be artifacts if 
  // we're just running 'publish-local', or else the build fails until 
  // you've published.
  // 
  // A var in your build might void the SBT warranty.  Would love a
  // cleaner solution.
  var signaturesAreArtifacts = false 

  override def artifacts = 
    if (signaturesAreArtifacts) { 
      super.artifacts flatMap { artifact =>      
        val ascArtifact = Artifact(artifact.name, "asc", artifact.extension+".asc", artifact.classifier, Seq(signaturesConfig), None)
        Seq(artifact, ascArtifact)
      }
    } else {
      super.artifacts
    }

  def signAction = signTask(artifacts) 
    .dependsOn(super.publishAction.dependencies : _*)
    .describedAs("Signs each artifact with GnuPG.")

  def signTask(artifacts: Iterable[Artifact]): Task = task {
    val result = artifacts.toStream flatMap signArtifact firstOption;
    signaturesAreArtifacts = true
    result
  }

  def signArtifact(artifact: Artifact): Option[String] = {
    val path = artifact2Path(artifact)
    List(gpgCommand, "-ab", "--yes", path).mkString(" ") ! match {
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

  /*
   * http://github.com/rossabaker/sbt-gpg-plugin/issues#issue/1
   *
   * Set packaging to be main artifact's type.
   */
  override def pomPostProcess(pom: Node): Node =
    pom match {
      case Elem(prefix, label, attr, scope, c @ _*) =>
        val children = c flatMap {
          case Elem(_, "packaging", _, _, _ @ _*) =>
            <packaging>{mainArtifactType}</packaging>
          case x => x
        }
        Elem(prefix, label, attr, scope, children : _*)
    }

  def mainArtifactType: String =
    this match {
      case proj: BasicScalaProject => proj.mainArtifact.`type`
      case _: ParentProject => "pom"
    }
}
