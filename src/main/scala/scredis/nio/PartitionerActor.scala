package scredis.nio

import com.codahale.metrics.MetricRegistry

import akka.actor.{ Actor, ActorRef, Props }
import akka.routing._
import akka.util.ByteString

import scredis.util.Logger
import scredis.protocol.{ NioProtocol, Request }

import scala.util.Success
import scala.collection.mutable.{ Queue => MQueue, ListBuffer }

import java.nio.ByteBuffer

case class Partition(data: ByteString, requests: Iterator[Request[_]])

class PartitionerActor(ioActor: ActorRef) extends Actor {
  
  private val logger = Logger(getClass)
  private val requests = MQueue[Request[_]]()
  
  private val router = context.actorOf(
    Props[DecoderActor]
      .withDispatcher("scredis.decoder-dispatcher")
      .withRouter(RoundRobinRouter(nrOfInstances = 10))
  )
  
  private val tellTimer = scredis.protocol.NioProtocol.metrics.timer(
    MetricRegistry.name(getClass, "tellTimer")
  )
  
  private var remainingByteStringOpt: Option[ByteString] = None
  
  def receive: Receive = {
    case request: Request[_] => {
      requests.enqueue(request)
      ioActor ! request
    }
    case data: ByteString => {
      val buffer = data.asByteBuffer
      var count = NioProtocol.count(buffer)
      val position = buffer.position
      
      val adaptedData = if (buffer.remaining == 0) {
        remainingByteStringOpt match {
          case Some(remains) => {
            remainingByteStringOpt = None
            count += 1
            remains ++ data
          }
          case None => data
        }
      } else {
        remainingByteStringOpt = Some(ByteString(buffer))
        data.take(position)
      }
      
      val requests = ListBuffer[Request[_]]()
      for (i <- 1 to count) {
        requests += this.requests.dequeue()
      }
      
      router ! Partition(adaptedData, requests.toList.iterator)
    }
  }
  
}