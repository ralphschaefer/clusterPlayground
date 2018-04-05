package my.playground

import my.playground.actors.Echo
import my.playground.controllers.WebServer
object Main extends App {

  println("Startup")

  private def testIfIsSeed(argList:Seq[String]) = argList.foldLeft(false)((a,b) => a || b == "seed" )

  val akkaArtifacts:AkkaArtifacts = try AkkaArtifacts(testIfIsSeed(args))
  catch {
    case e:Exception =>
      println(s"exception ${e.getMessage}")
      System.exit(-1)
      null
  }

  val webServer = akkaArtifacts.httpServerPort.map(p =>new WebServer(p,akkaArtifacts))

  if (akkaArtifacts.isSeed) {
    println("ANY Key?")
    scala.io.StdIn.readLine()
  }
  else
  {
    println("Enter any text or 'exit'")
    var line = "N/A"
    while (line != "exit") {
      line = scala.io.StdIn.readLine()
      println(line)
      akkaArtifacts.senderActor ! Echo.EchoMessage(line)
    }
  }

  webServer.foreach(_.unbind())
  akkaArtifacts.shutdown()

}