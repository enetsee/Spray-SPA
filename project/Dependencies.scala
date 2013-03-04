import sbt._

object Dependencies {

  val resolutionRepos = Seq(
    "spray repo" at "http://repo.spray.io/",
    "typesafe repo" at "http://repo.typesafe.com/typesafe/releases/",
    "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"
  )

  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def runtime   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")
  def container (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")


  val slick         = "com.typesafe"        %   "slick_2.10.0-RC5"  % "0.11.2"
  val bcrypt        = "org.mindrot"         %   "jbcrypt"           % "0.3m"    
  val scalaz        = "org.scalaz"          %   "scalaz-core"       % "7.0.0-M6"    cross CrossVersion.full
  val twirl         = "io.spray"            %%  "twirl-api"         % "0.6.1"
  val sprayHTTP     = "io.spray"            %   "spray-http"        % "1.1-M7"
  val sprayHTTPX    = "io.spray"            %   "spray-httpx"       % "1.1-M7"
  val sprayIO       = "io.spray"            %   "spray-io"       % "1.1-M7"
  val sprayCan      = "io.spray"            %   "spray-can"         % "1.1-M7"
  val sprayRouting  = "io.spray"            %   "spray-routing"     % "1.1-M7"
  val sprayClient   = "io.spray"            %   "spray-client"      % "1.1-M7"
  val sprayTestKit  = "io.spray"            %   "spray-testkit"     % "1.1-M7"
  val sprayJSON     = "io.spray"            %%  "spray-json"        % "1.2.3"       cross CrossVersion.full
  val akkaActor     = "com.typesafe.akka"   %%  "akka-actor"        % "2.1.0-RC6"   cross CrossVersion.full
  val akkaTestKit   = "com.typesafe.akka"   %%  "akka-testkit"      % "2.1.0-RC6"   cross CrossVersion.full
  val specs2        = "org.specs2"          %%  "specs2"            % "1.12.3"      cross CrossVersion.full
  val junit         = "junit"               %   "junit"             % "4.7"           
  val shapeless     = "com.chuusai"         %%  "shapeless"         % "1.2.3"
  val h2            = "com.h2database"      %   "h2"                % "1.3.166"
  val postgresql    = "postgresql"          %   "postgresql"        % "9.1-901.jdbc4"  
  val scalatest     = "org.scalatest"       %%  "scalatest"         % "1.9.1"

}
