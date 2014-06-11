package snakeyard.actor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props

import scala.language.postfixOps

object SnakeConfig {
  val marchTimeSeconds = 1
}

object Start

object StayHungry
object Grow
trait Direction
object Up extends Direction
object Down extends Direction
object Left extends Direction
object Right extends Direction

/**
 * Factory for [[snakeyard.actor.Snake]] instances.
 * 
 * Snake actor can be created for a given apple - the one the snake will strive for, and the channel
 * where coordinates will be sent to simulate snake movement. When coordinate(row*column) is sent, 
 * to a web socket, the cell will be a part of the snake, when a coordinate is sent with minus in front (-row*column),
 * the cell will not be a part of the snake any more.
 */
object Snake {
  def props(apple: ActorRef, uuid: String, webSocketChannel: ActorRef): Props = Props(new Snake(apple, uuid, webSocketChannel))
}

/**
 * Snake actor corresponds to a single snake in the Snake Pool. 
 * Snake can:
 *   change direction 
 *   move in a headed direction at a certain period of time(SnakeConfig)
 *   eat apple.
 * 
 * @param apple - the one apple the snake strives for
 * @param webSocketChannel - actor sends data to an already open socket when there is a change in the snake body.
 * @param name - unique identifier within the pool
 * 
 * Snake has a snake body as an internal state - a list of all cells that are part of the snake. 
 * Snake head will be the last element in the list.
 * Snake starts to march on the right by default and continues till change direction
 * message is received.
 * Snake eats the apple if the snake head reaches the cell with the apple on it.
 */
class Snake(apple: ActorRef, name: String, webSocketChannel: ActorRef) extends Actor {
  val snakeBody = new scala.collection.mutable.Queue[Int]
  var march = context.system.scheduler.schedule(0 seconds, SnakeConfig.marchTimeSeconds.seconds,
    self, Right)

  def receive = {
    case Start => {
      snakeBody.enqueue(0)
      self ! ChangeDirection(1)
    }
    case ChangeDirection(coordinates) => {
      val direction =
        if (snakeBody.last > coordinates && (snakeBody.last - coordinates) % SnakePoolConfig.col == 0) {
          Up
        } else if (snakeBody.last - 1 == coordinates) {
          Left
        } else if ((coordinates - snakeBody.last) % SnakePoolConfig.col == 0) {
          Down
        } else if (snakeBody.last + 1 == coordinates) {
          Right
        } else {
          None
        }
      if (direction != None) {
        march.cancel
        march = context.system.scheduler.schedule(0 seconds, SnakeConfig.marchTimeSeconds seconds,
          self, direction)
        self ! direction
      }
    }
    case Right => {
    	val newElement = snakeBody.last + 1
    			val reachEndCondition = newElement > 1 && newElement % SnakePoolConfig.col == 1
    			eat(newElement, reachEndCondition)
    } 
    case Left => {
      val newElement = snakeBody.last - 1
      val reachEndCondition = snakeBody.last % SnakePoolConfig.col == 0
      eat(newElement, reachEndCondition)
    }
    case Up => {
    	val newElement = snakeBody.last - SnakePoolConfig.col
    			val reachEndCondition = snakeBody.last < 0
    			eat(newElement, reachEndCondition)
    }
    case Down => {
      val newElement = snakeBody.last + SnakePoolConfig.col
      val reachEndCondition = snakeBody.last > SnakePoolConfig.col * SnakePoolConfig.row
      eat(newElement, reachEndCondition)
    }
    case StayHungry => {
      if (snakeBody.tail.size > 0) {
        val last = snakeBody.dequeue
        if (last > 0) {
          webSocketChannel ! Send("-" + last)
        }
      }
      webSocketChannel ! Send(snakeBody.last.toString)
    }
    case Grow => {
      webSocketChannel ! Send(snakeBody.last.toString)
    }

  }
  def eat(newElement: Int, reachEndCondition: Boolean) = {
    if (reachEndCondition || eatItsTale) {
      gameOver
    } else {
      snakeBody.enqueue(newElement)
      apple ! Eat(newElement)
    }
    def gameOver = {
      self ! PoisonPill
    }
    def eatItsTale() = {
      snakeBody.contains(newElement)
    }
  }
}
