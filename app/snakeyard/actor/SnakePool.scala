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

/**
 * Factory for [[snakeyard.actor.SnakePool]] instances. 
 */
object SnakePool {
  def props(webSocketChannel: ActorRef): Props = Props(new SnakePool(webSocketChannel))
}

/**
 * Snake pool contains a bundle of snakes striving for the one apple. 
 * SnakePool creates one apple upon creation and a new snake upon a new snake message. 
 * When a change direction message is received, it is resent to all snakes in the pool,
 * the snakes with appropriate coordinates will consume the message.
 * 
 * @param webSocketChannel - web socket is passed to the snake pool actor upon creation. It is used by snake
 * and apple to send changes of apple and snake's body.
 *
 * In case there is some exception with at least one snake, or one snake dies, 
 * all snakes and the pool die.
 * 
 * Game over is done by sending a separate string to the web socket - "Game Over"
 */
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