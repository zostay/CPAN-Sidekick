name := "CPAN-Sidekick"

version := "0.9.0"

scalaVersion := "2.10.0"

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.10.0" withSources()
