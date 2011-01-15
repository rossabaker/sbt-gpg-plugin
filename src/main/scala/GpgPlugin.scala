package com.rossabaker.sbt.gpg

import _root_.sbt._
import Process._
import scala.xml._

trait GpgPlugin extends BasicManagedProject {
  def gpgCommand: String = "gpg"
  lazy val skipGpgSign = systemOptional[Boolean]("gpg.skip", false).value

  lazy val sign = signAction

  val signaturesConfig = config("signatures") 

  override def artifacts = 
    if (skipGpgSign)
      super.artifacts
    else 
      super.artifacts flatMap { artifact =>
        val ascArtifact = Artifact(artifact.name, "asc", artifact.extension+".asc", artifact.classifier, Seq(signaturesConfig), None)
        Seq(artifact, ascArtifact)
      }

  def signAction = signTask(artifacts) 
    .dependsOn(makePom)
    .describedAs("Signs each artifact with GnuPG.")

  def signTask(artifacts: Iterable[Artifact]): Task = task {
    if (skipGpgSign) {
      log.info("Skipping signing of artifacts")
      None
    }
    else {
      artifacts.toStream flatMap signArtifact firstOption
    }
  }

  def signArtifact(artifact: Artifact): Option[String] = {
    val path = artifact2Path(artifact)
    path.ext match {
      case "asc" => None
      case "md5" => None
      case "sha1" => None
      case _ =>
        List(gpgCommand, "-ab", "--yes", path).mkString(" ") ! match {
          case 0 => None
          case _ => Some("error signing artifact: '"+path+"'."+
            "Set system property gpg.skip=true to skip the signing step.")
        }    
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
