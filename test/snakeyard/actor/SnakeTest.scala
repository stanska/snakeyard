package snakeyard.actor

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration

import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSpec
import org.scalatest.GivenWhenThen
import org.specs2.matcher.ShouldMatchers
import org.specs2.mock.Mockito

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.actorRef2Scala
import akka.testkit.TestKit
import akka.testkit.TestProbe
import play.api.libs.iteratee.Concurrent

class SnakeTest extends TestKit(ActorSystem("SnakeTest"))
  with FunSpec
  with ShouldMatchers
  with GivenWhenThen
  with BeforeAndAfterAll
  with Mockito {

  describe("Snake Actor Test") {
    it("given new snake when started starts to the right, from thee first cell on the first line") {
      val apple = TestProbe()
      val channel = TestProbe()
      val snakeName = "testSnake1"
      val snake = system.actorOf(Snake.props(apple.ref, snakeName, channel.ref), snakeName)
      //when
      snake ! Start
      //then
      apple.expectMsg(Eat(1))
      apple.expectMsg(Eat(2))
    }
    it("given snake on row 2 and column 4 , when user clicks the next cell of snake head on the left (row 2, column 3), snake starts to move on the left") {
      val apple = TestProbe()
      val channel = TestProbe()
      val snakeName = "testSnake2"
      val snake = system.actorOf(Snake.props(apple.ref, snakeName, channel.ref), snakeName)
      //given
      snake ! Start
      positionTo2_4(snakeName, snake, apple)
      //when
      snake ! ChangeDirection(SnakePoolConfig.col * 2 + 3)
      //then
      apple.expectMsg(Eat(SnakePoolConfig.col * 2 + 3))
    }

    def positionTo2_4(snakeName:String, snake: ActorRef, apple: TestProbe) = {
      apple.expectMsg(Eat(1))
      snake ! ChangeDirection(SnakePoolConfig.col + 1) //move down
      apple.expectMsg(Eat(SnakePoolConfig.col + 1))
      snake ! ChangeDirection(SnakePoolConfig.col + 2) //right
      apple.expectMsg(Eat(SnakePoolConfig.col + 2))
      snake ! ChangeDirection(SnakePoolConfig.col + 3) //right
      apple.expectMsg(Eat(SnakePoolConfig.col + 3))
      snake ! ChangeDirection(SnakePoolConfig.col + 4) //right
      apple.expectMsg(Eat(SnakePoolConfig.col + 4))
      snake ! ChangeDirection(SnakePoolConfig.col * 2 + 4)
      apple.expectMsg(Eat(SnakePoolConfig.col * 2 + 4))
    }
//    it("given snake head on row 2 and column 4 , when user clicks the next cell of snake head on the right (row 2, column 5), snake starts to move on the right") {
//      val apple = TestProbe()
//      val channel = TestProbe()
//      val snakeName = "testSnake3"
//      val snake = system.actorOf(Snake.props(apple.ref, snakeName, channel.ref), snakeName)
//      //given
//      snake ! Start
//      positionTo2_4(snakeName, snake, apple)
//      //when
//      snake ! ChangeDirection(SnakePoolConfig.col * 2 + 5)
//      //then
//      apple.expectMsg(Eat(SnakePoolConfig.col * 2 + 5))
//    }
//    ignore("given snake head on row 2 and column 4 , when user clicks the next cell of snake head up (row 1, column 4), snake starts to move up") {
//      val apple = TestProbe()
//      val channel = TestProbe() 
//      val snakeName = "testSnake4"
//      val snake = system.actorOf(Snake.props(apple.ref, snakeName, channel.ref), snakeName)
//      //given
//      snake ! Start
//      positionTo2_4(snakeName, snake, apple)
//      //when
//      snake ! ChangeDirection(SnakePoolConfig.col + 4)
//      //then
//      apple.expectMsg(Eat(SnakePoolConfig.col + 4))
//    }
    it("given snake head on row 2 and column 4 , when user clicks the next cell of snake head down (row 3, column 4), snake starts to move down") {
      val apple = TestProbe()
      val channel = TestProbe()
      val snakeName = "testSnake5"
      val snake = system.actorOf(Snake.props(apple.ref, snakeName, channel.ref), snakeName)
      //given
      snake ! Start
      positionTo2_4(snakeName, snake, apple)
      //when
      snake ! ChangeDirection(SnakePoolConfig.col * 3 + 4)
      //then
      apple.expectMsg(Eat(SnakePoolConfig.col * 3 + 4))
      apple.expectMsg(Eat(SnakePoolConfig.col * 4 + 4))
    }
    it("given snake head on row 2 and column 4 heading to the right, when user clicks not next cell of snake head down (row 4, column 4), snake continue to the right") {
      val apple = TestProbe()
      val channel = TestProbe()
      val snakeName = "testSnake6"
      val snake = system.actorOf(Snake.props(apple.ref, snakeName, channel.ref), snakeName)
      //given
      snake ! Start
      positionTo2_4(snakeName, snake, apple)
      //when
      snake ! ChangeDirection(SnakePoolConfig.col * 2 + 5)
      apple.expectMsg(Eat(SnakePoolConfig.col * 2 + 5))
      snake ! ChangeDirection(SnakePoolConfig.col * 4 + 4)
      //then
      apple.expectMsg(Eat(SnakePoolConfig.col * 2 + 6))
    }
    
    it("given snake head on row 1;1 and apple on 1;1, snake grows on the next background move") {
      val apple = TestProbe()
      val channel = TestProbe()
      val snakeName = "testSnake7"
      val snake = system.actorOf(Snake.props(apple.ref, snakeName, channel.ref), snakeName)
      //given
      snake ! Start
      apple.expectMsg(Eat(1))
      //when
      apple.send(snake, Grow)
      //then
      channel.expectMsg(Send("1"))
    }
   
    it("given snake 1 head on row 2 and column 4 heading down and snake 2 head on row 1;1 heading right, when user clicks 4;3 snake2 then snake 1 countinues down, snake 2 goes up and snake2 continues rights(does not change direction)") {
      val apple = TestProbe()
      val apple2 = TestProbe()
      val channel = TestProbe()
      val snakeName = "testSnake9"
      val snakeName2 = "testSnake92"
      val snake = system.actorOf(Snake.props(apple.ref, snakeName, channel.ref), snakeName)
      val snake2 = system.actorOf(Snake.props(apple2.ref, snakeName2, channel.ref), snakeName2)
      snake ! Start
      //given
      snake2 ! Start
      positionTo2_4(snakeName, snake, apple)
       snake ! ChangeDirection(SnakePoolConfig.col * 3 + 4)
      //then
      apple2.expectMsg(Eat(1))
      apple2.expectMsg(Eat(2))
      apple2.expectMsg(Eat(3))
      apple.expectMsg(Eat(SnakePoolConfig.col * 3 + 4))
      apple.expectMsg(Eat(SnakePoolConfig.col * 4 + 4))
      apple.expectMsg(Eat(SnakePoolConfig.col * 5 + 4))
    }
//    ignore("given snake head on row SnakeCongfig.row and column 1 heading down When background move Then game over") {
//      val apple = TestProbe()
//      val channel = TestProbe()
//      val snakeName = "testSnake10"
//      val snake = system.actorOf(Snake.props(apple.ref, snakeName, channel.ref), snakeName)
//      //given
//      snake ! Start
//      apple.expectMsg(Eat(1))
//      //when
//      snake ! ChangeDirection(SnakePoolConfig.col + 1)
//      for (i <- 1 to SnakePoolConfig.row-2) {
//    	  apple.expectMsg(Duration.create(35, TimeUnit.SECONDS), Eat(i*SnakePoolConfig.col+1))
//      }
//      //then
//      channel.expectMsg(Send("Game Over"))
//    }
//
//    ignore("given snake head on row 1:1heading right When background move Then game over") {
//      val apple = TestProbe()
//      val channel = TestProbe()
//      val snakeName = "testSnake11"
//      val snake = system.actorOf(Snake.props(apple.ref, snakeName, channel.ref), snakeName)
//      //given
//      snake ! Start
//      //when
//      for (i <- 1 to SnakePoolConfig.col) {
//    	  apple.expectMsg(Eat(i))
//      }
////      snake ! ChangeDirection(SnakePoolConfig.col + 1)
//      //then
//      channel.expectMsg(Send("Game Over"))
//      
//    }
//    it("given snake head on row 1 and column 6 heading up When background move Then game over") {
//      pending
//    }
//    it("given snake head on row 7 and column 1 heading left When background move Then game over") {
//      pending
//    }
  }
}