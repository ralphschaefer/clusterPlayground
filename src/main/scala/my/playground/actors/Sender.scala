package my.playground.actors

import akka.actor.{Actor, Props}
import my.playground.AkkaArtifacts
import akka.cluster.pubsub._

class Sender(akkaArtifacts: AkkaArtifacts) extends Actor {

  import DistributedPubSubMediator.Publish

  val mediator = DistributedPubSub(context.system).mediator

  def receive = {
    case m@Echo.EchoMessage(_) =>
      mediator ! Publish("conversation",m)
    case message => println("(SENDER) Undefined Message: "+message)
  }

}

object Sender {

  def props(akkaArtifacts: AkkaArtifacts) = Props(new Sender(akkaArtifacts))

}