package com.example.actors



import akka.actor.{Terminated, Actor, ActorRef}
import spray.json._
import collection.mutable.ArrayBuffer
import com.example.actors.ChatActor.{ChatMessage, AddListener}
import com.example.domain.NameModule.Name
import spray.util.SprayActorLogging

object ChatActor {
  case class ChatMessage(message:String)
  case class AddListener(listener: ActorRef)

  object ChatMessage extends ChatMessageJsonProtocol
  trait ChatMessageJsonProtocol extends DefaultJsonProtocol {
    implicit val messageFormat = jsonFormat1(ChatMessage.apply)
  }

}

class ChatActor extends Actor with SprayActorLogging {
  val watched = ArrayBuffer.empty[ActorRef]


  def receive : Receive = {
    case AddListener(listener) =>
      log.warning(s"Adding SSE listener.")
      context.watch(listener)
      watched += listener

    case msg @ ChatMessage(_) =>
      log.warning(s"Received chat message.")
      watched.foreach(_ ! SSEActor.SSEEvent(event=Some("message"),data=List(msg.toJson.compactPrint)))

    case Terminated(listener) =>
      watched -= listener

  }


}
