import sbt._

class Project(info: ProjectInfo) extends ParentProject(info) {
  lazy val core = project("core", "salvero-core", new SalveroCoreProject(_))
  lazy val akka = project("akka", "salvero-akka", new SalveroAkkaProject(_), core)
  lazy val lift = project("lift", "salvero-lift", new SalveroLiftProject(_), core)
  
  val novusRels = "repo.novus rels" at "http://repo.novus.com/releases/"
  val novusSnaps = "repo.novus snaps" at "http://repo.novus.com/snapshots/"
  val scalaToolsRels = "scala-tools rels" at "http://scala-tools.org/repo-releases/"
  
  class SalveroCoreProject(info: ProjectInfo) extends DefaultProject(info) {
    //need to get zmq.jar into a repo! for now it's in lib
    //for runtime: need 0MQ C lib and 0MQ JNI lib installed, and use -Djava.library.path=/path/to/their/install
    //val jzmq = "org.zeromq" % "jzmq" % "1.0"
    
    //use this when salat-avro is in a repo:
    //val salatAvro = "com.banno.salat.avro" %% "salat-avro" % "0.0.7-SNAPSHOT"
    
    //until salat-avro is in a repo, we'll just declare these and put salat-avro.jar in lib
    val salat = "com.novus" %% "salat-core" % "0.0.7-SNAPSHOT"
    val avro = "org.apache.avro" % "avro" % "1.5.0"
    
    val slf4j = "org.clapper" %% "grizzled-slf4j" % "0.4"
    val logback = "ch.qos.logback" % "logback-classic" % "0.9.28" % "test"
    
    //testing
    val snapshots = "snapshots" at "http://scala-tools.org/repo-snapshots"
    val releases  = "releases" at "http://scala-tools.org/repo-releases"
    val specs2 = "org.specs2" %% "specs2" % "1.2" % "test"
    def specs2Framework = new TestFramework("org.specs2.runner.SpecsFramework")
    override def testFrameworks = super.testFrameworks ++ Seq(specs2Framework)
  }
  
  class SalveroAkkaProject(info: ProjectInfo) extends DefaultProject(info) with AkkaProject {
    //TODO logback is getting included in compile & test configs, and causes errors during unit test runs...
  
    //testing
    def specs2Framework = new TestFramework("org.specs2.runner.SpecsFramework")
    override def testFrameworks = super.testFrameworks ++ Seq(specs2Framework)
  }
  
  class SalveroLiftProject(info: ProjectInfo) extends DefaultProject(info) {
    val liftActor = "net.liftweb" %% "lift-actor" % "2.3"
    
    //testing
    def specs2Framework = new TestFramework("org.specs2.runner.SpecsFramework")
    override def testFrameworks = super.testFrameworks ++ Seq(specs2Framework)
  }
}
