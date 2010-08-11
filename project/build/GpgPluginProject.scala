import sbt._

class GpgPluginProject(info: ProjectInfo) extends PluginProject(info) {
  override def managedStyle = ManagedStyle.Maven
  val publishTo = 
    if (version.toString.endsWith("-SNAPSHOT"))
      "Scala-tools Snapshots" at "http://nexus.scala-tools.org/content/repositories/snapshots" 
    else 
      "Scala-tools Releases" at "http://nexus.scala-tools.org/content/repositories/releases"
  Credentials(Path.userHome / ".ivy2" / "credentials" / "nexus.scala-tools.org", log)
}
