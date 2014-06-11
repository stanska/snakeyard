package snakeyard

import akka.actor.ActorSelection.toScala
import akka.actor.PoisonPill
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Iteratee
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.WebSocket
import play.libs.Akka
import snakeyard.actor.ChangeDirection
import snakeyard.actor.NewSnake
import snakeyard.actor.SnakePool
import snakeyard.actor.WebSocketChannel

/**
 * SnakeController handles all incoming requests from snake's user interface:
 * GET   /                  snakeyard.SnakeController.index
 * GET   /startgame         snakeyard.SnakeController.startGame
 * GET   /addsnake/:pool    snakeyard.SnakeController.addSnake(pool)
 * GET   /movesnake/:pool   snakeyard.SnakeController.move(pool)
 * GET   /stopgame/:pool    snakeyard.SnakeController.stop(pool)
 *
 */
object SnakeController extends Controller {

  /**
   * Show snake's starting page
   */
  def index() = Action {
    Ok(views.html.snake.render)
  }

  /**
   * opens web socket where actors will push data, and creates snake pool
   */
  def startGame = WebSocket.using[String] { request =>
    val (out, channel) = Concurrent.broadcast[String]
    val webSocketChannel = Akka.system.actorOf(WebSocketChannel.props(channel))
    val snakePool = Akka.system.actorOf(SnakePool.props(webSocketChannel))
    val in = Iteratee.foreach[String] { msg => if (msg == "GET POOL") channel.push("POOL:" + snakePool.path.name); }
    (in, out)
  }

  /**
   * Adds snake in the pool
   */
  def addSnake(snakePoolName: String) = Action { implicit request =>
    {
      val snakeName = request.getQueryString("snake_name").get
      val snakePool = Akka.system.actorSelection("/user/" + snakePoolName)
      snakePool ! NewSnake(snakeName)
      Ok("Done")
    }
  }

  /**
   * Receives move snake request
   */
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

  /**
   * Stop game - stop pool and snakes.
   */
  def stop(snakePool: String) = Action {
    val snake = Akka.system.actorSelection("/user/" + snakePool)
    snake.tell(PoisonPill)
    Ok("Accepted")
  }
}
