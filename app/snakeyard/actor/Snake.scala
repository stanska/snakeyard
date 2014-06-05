package snakeyard.actor

import play.api.libs.iteratee.Concurrent
import akka.actor.ActorRef
import akka.actor.Props
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.Props
import akka.actor.Actor
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import akka.actor.PoisonPill

object SnakeConfig {
  val marchTimeSeconds = 1
}

object Start
object Stop

trait Direction
object Up extends Direction
object Down extends Direction
object Left extends Direction
object Right extends Direction

object StayHungry
object Grow

object Snake {
  def props(apple: ActorRef, uuid: String, webSocketChannel: ActorRef): Props = Props(new Snake(apple, uuid, webSocketChannel))
}

class Snake(apple: ActorRef, name: String, webSocketChannel: ActorRef) extends Actor {
  val snakeBody = new scala.collection.mutable.Queue[Int]
  var march = context.system.scheduler.schedule(Duration.create(0, TimeUnit.SECONDS), Duration.create(SnakeConfig.marchTimeSeconds, TimeUnit.SECONDS),
    self, Right)

  def receive = {
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
    case Down => {
      val newElement = snakeBody.last + SnakePoolConfig.col
      val edgeCondition = snakeBody.last > SnakePoolConfig.col * SnakePoolConfig.row
      eat(newElement, edgeCondition)
    }
    case Up => {
      val newElement = snakeBody.last - SnakePoolConfig.col
      val edgeCondition = snakeBody.last < 0
      eat(newElement, edgeCondition)
    }
    case Left => {
      val newElement = snakeBody.last - 1
      val edgeCondition = snakeBody.last % SnakePoolConfig.col == 0
      eat(newElement, edgeCondition)
    }
    case Right => {
      val newElement = snakeBody.last + 1
      val edgeCondition = newElement > 1 && newElement % SnakePoolConfig.col == 1
      eat(newElement, edgeCondition)
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
        march = context.system.scheduler.schedule(Duration.create(0, TimeUnit.SECONDS), Duration.create(SnakeConfig.marchTimeSeconds, TimeUnit.SECONDS),
          self, direction)
        self ! direction
      }
    }
    case Start =>
      {
        snakeBody.enqueue(0)
        self ! ChangeDirection(1)

      }
    case Stop =>
      {
        march.cancel
        self ! PoisonPill
      }
  }
  def gameOver = {
    webSocketChannel ! Send("Game Over")
    context.parent ! PoisonPill
  }
  def eat(newElement: Int, edgeCondition: Boolean) = {
    if (edgeCondition || snakeBody.contains(newElement)) {
      gameOver
    } else {
      snakeBody.enqueue(newElement)
      apple ! Eat(newElement)
    }
  }
}
