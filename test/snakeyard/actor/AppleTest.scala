package snakeyard.actor
 
import scala.util.Random

import org.scalatest.FunSpec
import org.specs2.mock.Mockito

import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.testkit.TestProbe

class AppleTest extends TestKit(ActorSystem("AppleTest"))
  with FunSpec
  with Mockito {

  describe("Apple Test") {
    it("New apple push to channel") {
      val channel = TestProbe()
      val appleName = "testApple1"
      val random = mock[Random]
      val apple = system.actorOf(Apple.props(random, channel.ref), appleName)

      //when
      random.nextInt(SnakePoolConfig.row * SnakePoolConfig.col - 1) returns 134
      apple ! NewApple
      //then
      channel.expectMsg(Send("APPLE:135"))
    }
    it("When Eat coordinate same as apple, then Grow") {
      val channel = TestProbe()
      val client = TestProbe()		  
      val appleName = "testApple2"
      val random = mock[Random]
      val apple = system.actorOf(Apple.props(random, channel.ref), appleName)

      //when
      random.nextInt(SnakePoolConfig.row * SnakePoolConfig.col - 1) returns 134
      apple ! NewApple
      client.send(apple, Eat(135)) 
      //then
      client.expectMsg(Grow)
    }
    it("When Eat coordinate != apple, then StayHungry") {
      val client = TestProbe()
      val channel = TestProbe()
      val appleName = "testApple3"
      val random = mock[Random]
      random.nextInt(SnakePoolConfig.row * SnakePoolConfig.col - 1) returns 123
      val apple = system.actorOf(Apple.props(random, channel.ref), appleName)
      
      //when
      apple ! NewApple
      client.send(apple, Eat(135)) 
      //then
      client.expectMsg(StayHungry)
    }
  }
}