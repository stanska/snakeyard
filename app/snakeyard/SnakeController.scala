package snakeyard

import play.libs.Akka
import play.api.mvc.Action
import play.api.mvc.WebSocket
import play.api.mvc.Controller
import scala.util.Random
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Iteratee
import akka.actor.PoisonPill
import akka.actor.actorRef2Scala
import snakeyard.actor.Apple
import snakeyard.actor.Snake
import snakeyard.actor.Start
import snakeyard.actor.ChangeDirection
import snakeyard.actor.WebSocketChannel
import snakeyard.actor.SnakePool
import snakeyard.actor.NewSnake
import play.api.libs.iteratee.Enumerator

object SnakeController extends Controller {

  def index() = Action {
    Ok(views.html.snake.render)
  }

  def startGame = WebSocket.using[String] { request =>
    val (out, channel) = Concurrent.broadcast[String]
    val webSocketChannel = Akka.system.actorOf(WebSocketChannel.props(channel))
    val snakePool = Akka.system.actorOf(SnakePool.props(webSocketChannel))
    val in = Iteratee.foreach[String] { msg => if (msg == "GET POOL") channel.push("POOL:" + snakePool.path.name); }
    (in, out)
  }

  def addSnake(snakePoolName: String) = Action { implicit request =>
    {
      val snakeName = request.getQueryString("snake_name").get
      val snakePool = Akka.system.actorSelection("/user/" + snakePoolName)
      snakePool ! NewSnake(snakeName)
      Ok("Done")
    }
  }

  def move(snakePoolName: String) = Action {
    implicit request =>
      {
        val coordinates = request.getQueryString("coordinates")
        		
        val snakePoolActor = Akka.system.actorSelection("/user/" + snakePoolName)
        snakePoolActor.tell(ChangeDirection(toInt(coordinates)))
        Ok("accepted")
      }
  }

  def toInt(s: Option[String]): Int = {
    val intValue = try {
      Some(s.get.toInt)
    } catch {
      case e: Exception => None
    }
    intValue.getOrElse(-1)
  }

  def stop(snakePool: String) = Action {
    val snake = Akka.system.actorSelection("/user/" + snakePool)
    snake.tell(PoisonPill)
    Ok("Accepted")
  }
}
