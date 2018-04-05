package my.playground.controllers

import akka.NotUsed
import akka.actor.PoisonPill
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Sink}
import my.playground.actors.webSocket.IncomingMessageActor
import my.playground.{AkkaArtifacts, actors, controllers}
import org.json4s._

class WebServer(port:Int, akkaArtifacts: AkkaArtifacts) {

  private implicit val system = akkaArtifacts.system
  private implicit val materializer = akkaArtifacts.materializer
  private implicit val executionContext = system.dispatcher

  private val incomingMessageActor = system.actorOf(IncomingMessageActor.props(akkaArtifacts.outgoingMessagesQueue),"incomingactor")

  private val incomingMessages: Sink[Message, NotUsed] = Flow[Message].map {
      case msg:TextMessage =>
        akkaArtifacts.senderActor ! actors.Echo.EchoMessage(msg.asTextMessage.getStrictText)
        IncomingMessageActor.IgnoreMessage
      case other =>
        println("ignore other : " + other.toString)
        IncomingMessageActor.IgnoreMessage
    }.to(Sink.actorRef[IncomingMessageActor.Messages](incomingMessageActor, PoisonPill))

  private val chat = Flow.fromSinkAndSource(incomingMessages, akkaArtifacts.outgoingMessageSource)

  val route:Route =
    pathEndOrSingleSlash {
      complete(controllers.RenderMainHtmlWithContent(akkaArtifacts).render("index.html", JNull, "INDEX"))
    } ~
    path("testws") {
      complete(controllers.RenderMainHtmlWithContent(akkaArtifacts).render("testWebsocket.html", JNull, "Test Websockets"))
    } ~
    pathPrefix("assets") {
      getFromResourceDirectory("assets")
    } ~
    path("listen") {
      withoutRequestTimeout {
        handleWebSocketMessages(chat)
      }
    } ~
    path("trigger") {
      akkaArtifacts.echoActor ! actors.Echo.EchoMessage("hello WebServer Socket")
      complete("OK")
    }

  private val bindingFuture = Http().bindAndHandle(route, "localhost", port)

  println(s"*** Start webserver on port $port")

  def unbind() = bindingFuture.flatMap(_.unbind())
}
