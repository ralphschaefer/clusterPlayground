import akka.stream.scaladsl.Source
import org.scalatest.FunSuite
import akka.{Done, NotUsed}
import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.util.ByteString

import scala.concurrent._
import scala.concurrent.duration._

class StreamTest extends FunSuite {

  test("simple test")
  {
    assert( 1 != 2)
  }

  test("stream"){
    implicit val system = ActorSystem("QuickStart")
    implicit val materializer = ActorMaterializer()
    val source: Source[Int, NotUsed] = Source(1 to 10)
    // val source = Source.actorRef[Int](100,OverflowStrategy.dropTail)
    val a = source.runForeach(i => println(i))(materializer)
    // assert(false)
    CoordinatedShutdown.get(system).run(CoordinatedShutdown.unknownReason)
  }
}
