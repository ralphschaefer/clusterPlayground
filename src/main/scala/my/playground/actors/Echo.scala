package my.playground.actors

import akka.actor.{Actor, Props}
import akka.cluster.pubsub.DistributedPubSubMediator.{Count, CountSubscribers}
import my.playground.AkkaArtifacts
import akka.cluster.pubsub._
import akka.http.scaladsl.model.ws.TextMessage
import org.json4s._
import org.json4s.jackson.JsonMethods._

class Echo(akkaArtifacts: AkkaArtifacts) extends Actor {

  import Echo._
  import DistributedPubSubMediator.{ Subscribe, SubscribeAck }
  import DistributedPubSubMediator.Publish

  val mediator = DistributedPubSub(context.system).mediator

  mediator ! Subscribe("conversation",self)

  def receive = {
    case m@EchoMessage(text) =>
      println(s"(${sender().toString}) -> $text")
      akkaArtifacts.outgoingMessagesQueue offer TextMessage(
        compact(render(m.withSender(sender().toString).toJson))
      )
    case SubscribeAck(item) =>
      println("subsribe akn " + item)
      mediator ! Publish("conversation", EchoMessage("Akn subscription from: "+self))
      Thread.sleep(3000)
      mediator ! Count

      /*
      Thread.sleep(10000)

      println("GO!!")
      akkaArtifacts.senderActor ! EchoMessage("-----------")
      */

    case message => println("(ECHO) Undefined Message: "+message)
  }

}

object Echo {

  case class EchoMessage(text:String) {
    def withSender(sender :String):EchoMessageWithSender = EchoMessageWithSender(sender,text)
  }

  case class EchoMessageWithSender(sender:String, message:String) {
    implicit val formats = org.json4s.DefaultFormats
    def toJson:JValue = Extraction.decompose(this)
  }


  def props(akkaArtifacts: AkkaArtifacts) = Props(new Echo(akkaArtifacts))

}
