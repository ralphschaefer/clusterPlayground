package my.playground.actors.webSocket

import akka.actor.{Actor, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.SourceQueueWithComplete
import org.json4s._
import org.json4s.jackson.JsonMethods._


class IncomingMessageActor(outgoingQueue: SourceQueueWithComplete[Message]) extends Actor {
  def receive: Receive = {
    case m@IncomingMessageActor.IncomingMessage(text) =>
      outgoingQueue.offer(TextMessage(m.toString))
    case someThing =>
  }
}

object IncomingMessageActor {
  trait Messages
  case object IgnoreMessage extends Messages
  case class MessageWithSource(message:String, source:String)
  case class IncomingMessage(item:JValue) extends Messages {
    override def toString: String = pretty(render(item))
  }
  def props(outgoingActor: SourceQueueWithComplete[Message]) = Props(new IncomingMessageActor(outgoingActor))
}
