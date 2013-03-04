import sbt._
import Keys._
import ls.Plugin._

object BuildSettings {
	val VERSION = "0.1-SNAPSHOT"

	lazy val baseSettings = seq (
		version 				:= VERSION,
		description 			:= "Spray singl-page app example",
		startYear 				:= Some(2012),
		scalaVersion 			:= "2.10.0-RC5",
		resolvers 				++= Dependencies.resolutionRepos,
		scalacOptions 			:= Seq(
			"-encoding", "utf8",
      		"-feature",
      		"-unchecked",
      		"-deprecation",
      		"-target:jvm-1.6",
      		"-language:postfixOps",
      		"-language:implicitConversions",
      		"-Xlog-reflective-calls",
      		"-Ywarn-adapted-args"
		)
	)

	lazy val spaSettings =
    	baseSettings ++ twirl.sbt.TwirlPlugin.Twirl.settings ++ 
    	spray.revolver.RevolverPlugin.Revolver.settings


}