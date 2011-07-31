package dips.communication

import scala.actors.{Actor, AbstractActor}
import scala.actors.Actor.actor

//import org.scalatest.FunSuite


sealed trait Communication

case object Exit extends Communication

trait Message extends Communication
case class MessageHolder[T](msg:T) extends Message
case class Post(dest:Any, msg:Message) extends Communication
case object Retrieve extends Communication

case class Uri(host: String, port: Int, service: Symbol)

sealed trait Routing

@serializable
case class Connect(uri:Uri) extends Routing
case object Disconnect extends Routing

trait Router extends Actor{
  def connect(uri:Any)
  def disconnect()
  def translate(dest:Any):AbstractActor
  def routing_event(event:Routing)
}

trait PostOffice extends Router{
  var messages = List[Message]()
  
  def post_message[T](dest:Any, msg:Message) = this ! Post(dest, msg)
  def retrieve_messages = this !! Retrieve
  
  def act() {
    while (true) {
      receive {
        case Post(dest, msg) =>
          translate(dest) ! msg
        case Retrieve =>
          this.reply(messages)
          messages = List[Message]()
        case r:Routing =>
          this.routing_event(r)
        case msg:Message =>
          messages = msg::messages
        case Exit =>
          this.exit()
          return
      }
    }
  }
}


object Run extends App{
  object po extends PostOffice{
    def translate(dest:Any) = {this}
    def connect(uri:Any) = None
    def disconnect() = None
    def routing_event(r:Routing) = println(r)
  }
  
  po.start()
  
  po ! Connect
  po.post_message(None, MessageHolder("message1"))
  po.post_message(None, MessageHolder("message2"))
  po.post_message(None, MessageHolder("message3"))
  po.post_message(None, MessageHolder("message4"))
  po.post_message(None, MessageHolder("message5"))
  
  val msgs = po.retrieve_messages()
 
  msgs match{ case msgs:List[MessageHolder[String]] => msgs.reverse.foreach(println)}
  po ! Exit
}




