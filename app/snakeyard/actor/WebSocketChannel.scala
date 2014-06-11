package snakeyard.actor

import akka.actor.Props
import play.api.libs.iteratee.Concurrent
import akka.actor.Actor

case class Send(data:String)

/**
 * Factory for [[snakeyard.actor.WebSocketChannel]] instances. 
 * 
 * 
 * WebSocketChannel actor is created with a string output channel as a parameter, where
 * all send messages are pushed. 
 */
object WebSocketChannel {
	def props(channel: Concurrent.Channel[String]):Props = Props(new WebSocketChannel(channel))
}

class WebSocketChannel(channel: Concurrent.Channel[String]) extends Actor {
	def receive = {
	  case Send(data) => channel.push(data)
	}
}