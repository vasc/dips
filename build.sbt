

// set the name of the project
name := "DIPS"

version := "1.0-alpha"

organization := "pt.inesc-id"

libraryDependencies += "log4j" % "log4j" % "1.2.16"

libraryDependencies += "djep" % "djep" % "2.3.0" from "http://www.singsurf.org/djep/jars/djep-full-latest.jar"

scalaVersion := "2.9.1"

mainClass in (Compile, packageBin) := Some("dips.Dips")

mainClass in (Compile, run) := Some("dips.Dips")
