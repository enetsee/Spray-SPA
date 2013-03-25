package com.example.actors

import concurrent.duration._
import akka.actor.{ReceiveTimeout, Actor}

import spray.routing.RequestContext
import spray.http._
import com.example.actors.SSEActor.{SSEClose, SSEEnd, SSEEvent}
import spray.can.server.HttpServer
import spray.util.SprayActorLogging
import spray.http.ChunkedMessageEnd
import spray.http.HttpResponse
import com.example.actors.SSEActor.SSEEvent
import spray.routing.RequestContext
import spray.http.ChunkedResponseStart

object SSEActor {
  sealed trait SSEMessage
  case object SSEEnd extends SSEMessage
  case object SSEClose extends SSEMessage
  case class SSEEvent(data:List[String], id:Option[String] = None,event:Option[String] = None,retry: Option[Long] = None) extends SSEMessage {
    override def toString() = {
      val idStr  = id.map(id => s"id:$id\n").getOrElse("")
      val evtStr = event.map(evt => s"event:$evt\n").getOrElse("")
      val retryStr = retry.map(t => s"retry:$t\n").getOrElse("")
      val dataStr = data.map({x => s"data:$x"}).reduce(_ + "\n" + _)
      s"${idStr}${evtStr}${retryStr}${dataStr}\n\n"
    }
  }
}


class SSEActor(ctx:RequestContext) extends Actor with SprayActorLogging {
  val comment = ":\n\n"
  ctx.responder ! ChunkedResponseStart(HttpResponse(entity = comment))
  context.setReceiveTimeout(20.seconds)

  def receive: Receive = {
    case evt @ SSEEvent(_,_,_,_) =>
      log.debug(s"Sending SSE event: ${evt.toString}")
      ctx.responder ! MessageChunk(evt.toString)

    case ReceiveTimeout =>
      ctx.responder ! MessageChunk(comment)

    case SSEEnd =>
      ctx.responder ! ChunkedMessageEnd
      context.stop(self)

    case SSEClose =>
        // notify client to stop retrying
       ctx.responder ! StatusCodes.NotFound
       context.stop(self)

    case HttpServer.Closed(_,reason) =>
      log.warning(s"Stopping SSE stream, reason: $reason")
      context.stop(self)
  }
}
