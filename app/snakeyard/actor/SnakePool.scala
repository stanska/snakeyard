package snakeyard.actor

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import scala.util.Random
import akka.actor.PoisonPill
import akka.actor.Kill

object SnakePoolConfig {
  val row = 10
  val col = 20
}

case class ChangeDirection(coordinates: Int)
case class NewSnake(snakeName: String)

object SnakePool {
  def props(webSocketChannel: ActorRef): Props = Props(new SnakePool(webSocketChannel))
}

class SnakePool(webSocketChannel: ActorRef) extends Actor {
  val apple = context.actorOf(Apple.props(new Random(), webSocketChannel))
  apple ! NewApple
  var snakes = Map.empty[String, ActorRef]
  def receive = {
    case NewSnake(snakeName) => {
      val snake = context.actorOf(Snake.props(apple, snakeName, webSocketChannel), snakeName)
      snake ! Start
      snakes += (snakeName -> snake)
      println(snakes)
    }

    case cd: ChangeDirection => snakes.map(snakesByName => snakesByName._2 ! cd)
  }

  override def postStop() {
    snakes.map(snakeByName => snakeByName._2 ! Kill)
  }
}