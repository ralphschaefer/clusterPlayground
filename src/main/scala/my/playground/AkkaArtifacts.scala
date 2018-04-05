package my.playground

import akka.actor.{ActorRef, ActorSystem, CoordinatedShutdown}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{BroadcastHub, Keep, Source, SourceQueueWithComplete}
import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.model.ws.Message

import collection.JavaConverters._

trait AkkaArtifacts {

  val system        : ActorSystem
  val echoActor     : ActorRef
  val senderActor   : ActorRef
  val materializer  : ActorMaterializer
  val httpServerPort: Option[Int]

  val outgoingMessagesQueue:SourceQueueWithComplete[Message]
  val outgoingMessageSource:Source[Message,Any]

  val isSeed:Boolean

  def shutdown():Unit = {
    CoordinatedShutdown.get(system).run(CoordinatedShutdown.unknownReason)
    println("Shutdown actor system")
  }

}


object AkkaArtifacts {

  import my.playground.actors.{Echo, Sender}

  def apply(seedFlag:Boolean = false):AkkaArtifacts = new AkkaArtifacts {

    val httpServerPort: Option[Int] = utils.Port.findFree()
    val isSeed: Boolean = seedFlag
    val actorSystemName = "testSystem"
    val system: ActorSystem = if (!seedFlag)
      ActorSystem(actorSystemName,
        ConfigFactory.parseMap(mapAsJavaMap(Map(
          "akka.remote.netty.tcp.port" -> 0
        ))).withFallback(ConfigFactory.load()).resolve()
      )
    else
      ActorSystem(actorSystemName)
    val materializer: ActorMaterializer = ActorMaterializer()(system)

    val (outgoingMessagesQueue,outgoingMessageSource):(SourceQueueWithComplete[Message],Source[Message,Any]) =
      Source.queue[Message](1000,OverflowStrategy.dropHead).toMat(BroadcastHub.sink[Message])(Keep.both).run()(materializer)
    val senderActor : ActorRef = system.actorOf(Sender.props(this),"sender")
    val echoActor   : ActorRef = system.actorOf(Echo.props(this),"echo")

    println("SeedNode: " + system.settings.config.getString("settings.seednode"))
    println("isSeed  : " + this.isSeed)
  }

}
