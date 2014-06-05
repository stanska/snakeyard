package controllers

import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Iteratee
import play.api.libs.iteratee.Concurrent
import play.api.mvc.Controller
import play.api.mvc.WebSocket

object Application extends Controller {
  def test = WebSocket.using[String] { request =>

    val (out, channel) = Concurrent.broadcast[String]
    val in = Iteratee.foreach[String] { msg =>
      //Actor messages must be pushed here, how?
      channel push ("RESPONSE: " + msg)
    }

    (in, out)

  }
}