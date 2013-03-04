import sbt._
import sbt.Keys._

object SprayspaBuild extends Build {
  import BuildSettings._
  import Dependencies._
  lazy val sprayspa = Project(id = "spray-spa",base = file("."))
    .settings(spaSettings: _*)
    .settings(libraryDependencies ++= 
        compile(akkaActor,sprayIO,sprayRouting,sprayHTTPX,sprayHTTP,sprayCan,sprayJSON,bcrypt,slick,h2) ++
        provided(twirl) ++
        test(akkaTestKit,sprayTestKit,specs2,junit)
      )

}
