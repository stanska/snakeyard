package snakeyard.actor

import akka.actor.Props
import play.api.libs.iteratee.Concurrent
import akka.actor.Actor

case class Send(data:String)

object WebSocketChannel {
	def props(channel: Concurrent.Channel[String]):Props = Props(new WebSocketChannel(channel))
}

class WebSocketChannel(channel: Concurrent.Channel[String]) extends Actor {
	def receive = {
	  case Send(data) => channel.push(data)
	}
}