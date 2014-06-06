package snakeyard.actor

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import scala.util.Random
import akka.actor.PoisonPill
import akka.actor.Kill
import akka.actor.AllForOneStrategy
import akka.actor.SupervisorStrategy._
import akka.actor.Terminated

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
  
  override val supervisorStrategy = AllForOneStrategy() {
    case anyException => Stop //if something happens to one snake, kill them all
  }
  
  val apple = context.actorOf(Apple.props(new Random(), webSocketChannel))
  apple ! NewApple
  var snakes = Map.empty[String, ActorRef]
  def receive = {
    case NewSnake(snakeName) => {
      val snake = context.actorOf(Snake.props(apple, snakeName, webSocketChannel), snakeName)
      snake ! Start
      snakes += (snakeName -> snake)
      context.watch(snake)
    }
    case t: Terminated => self ! PoisonPill
    case cd: ChangeDirection => snakes.map(snakesByName => snakesByName._2 ! cd)
  }
  
  override def postStop() {
    webSocketChannel ! Send("Game Over")
    snakes.map(snakeByName => snakeByName._2 ! Kill)
  }
}