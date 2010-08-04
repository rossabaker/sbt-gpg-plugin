import sbt._

class GpgPluginProject(info: ProjectInfo) extends PluginProject(info) {
  override def managedStyle = ManagedStyle.Maven
  val publishTo = "Scala-tools Nexus Snapshots" at "http://nexus.scala-tools.org/content/repositories/snapshots"
  Credentials(Path.userHome / ".ivy2" / "credentials" / "nexus.scala-tools.org", log)
}
