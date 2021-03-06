# sbt-gpg-plugin

A [simple build tool](http://code.google.com/p/simple-build-tool) plugin for signing the project's artifacts with [GnuPG](http://www.gnupg.org/).  It is inspired by [maven-gpg-plugin](http://maven.apache.org/plugins/maven-gpg-plugin/).

## Usage

1. Create a project/plugins/Plugins.scala:

        import sbt._

        class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
          val gpgPlugin = "com.rossabaker" % "sbt-gpg-plugin" % "0.1.0-SNAPSHOT"
        }

2. Extend your project with GpgPlugin:

        import sbt._
        import com.rossabaker.sbt.gpg._

        class MyProject(info: ProjectInfo) extends DefaultProject(info) with GpgPlugin

3. run `sbt update`
4. run `sbt publish`

## Actions

### sign

The `sign` action runs just before `publish`.  It signs each artifact with GnuPG.

## gnupg-agent

No attempt is made by this plugin to store your passphrase, which you may be prompted for several times.  I use [gnupg-agent](http://www.debian-administration.org/article/Using_gnupg-agent_to_securely_retain_keys) to reduce these prompts.

## Changes

### v0.2.0

- Runs `sign` before `publish` instead of `deliver` and `deliverLocal`.  This makes it less obtrusive and plays nicer with [Maven-SBT](https://github.com/codahale/maven-sbt).
