// set the name of the project
name := "DIPS"

version := "1.0-alpha"

organization := "pt.inesc-id"

scalaVersion := "2.9.0"

mainClass in (Compile, packageBin) := Some("dips.Dips")

mainClass in (Compile, run) := Some("dips.Dips")
