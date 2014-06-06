package snakeyard.actor

import play.api.libs.iteratee.Concurrent
import scala.util.Random
import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef

object NewApple

case class Eat(coordinates: Int)

object Apple {
  def props(randomGenerator: Random, webSocketChannel: ActorRef): Props = Props(new Apple(randomGenerator, webSocketChannel))
}

class Apple(randomGenerator: Random, webSocketChannel: ActorRef) extends Actor {
  var apple: Int = randomCell
  var channels: List[Concurrent.Channel[String]] = List()
  def receive = {
    case NewApple => {
      apple = randomCell
      webSocketChannel ! Send("APPLE:" + apple.toString)
    }
    case Eat(coordinates) => {
      if (apple != coordinates) {
        sender ! StayHungry
      } else {
        self ! NewApple
        sender ! Grow
      }
    }
  }
  def randomCell(): Int = {
    randomGenerator.nextInt(SnakePoolConfig.row * SnakePoolConfig.col - 1) + 1
  }
}