package dips.communication
import scala.actors.AbstractActor
import scala.collection.mutable.HashMap
import dips.util.Logger.log
import scala.actors.OutputChannel
import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer
import dips.core.ControlMessage
import scala.collection.mutable.SynchronizedQueue
import dips.simulation.DistributedSimulation
import dips.simulation.Migration

case class Publication(name:Symbol, msg:Any)
case class Subscription(name:Symbol, msg:Any, sender:OutputChannel[Any])
case class Connected(uri:Uri)

trait PostOffice extends Router{
  val local_addr:Uri
  val control_messages = new SynchronizedQueue[(ControlMessage, OutputChannel[Any])]()
  var messages = new SynchronizedQueue[Message]()
  var received_messages_count = 0
  
  def retrieve_messages = this !! Retrieve
  
  def subscribe(name:Symbol, actor:AbstractActor)
  protected def deliver_to_subscribers(name:Symbol, msg:Any, sender:OutputChannel[Any])
 
  def act() {
    while (true) {
      this.synchronized{
      
      receive {
        case msg:Message =>
          messages.synchronized{
            messages += msg
            received_messages_count += 1
            messages.notify()
          }
        case lm:Buffer[Message] =>
          
          messages.synchronized{
            messages ++= lm
            received_messages_count += lm.size
            messages.notify()
          }
        case m:Migration =>
          DistributedSimulation.migrator(m)        
        case msg:ControlMessage =>
          control_messages enqueue ((msg, sender))
          DistributedSimulation.simulation.synchronized{
            DistributedSimulation.simulation.notify()
          }
        /*case Retrieve =>
          this.reply(messages)
          messages = new AwaitableQueue[Message]()
        */
        case r:Routing =>
          this.routing_event(r)
        case Exit =>
          this.exit()
          return
        case Publication(name, msg) =>
          log.debug("Received publication: " + msg)
          //log.debug("Received publication: " + name + ", " + msg)
          this.deliver_to_subscribers(name, msg, sender)
        case a:Any =>
          log.debug("Unrecognized message: " + a)
      }
    }
    }
  }
}

/*
object PostOffice extends App{
  object po extends PostOffice{
    def translate(dest:Routable) = {this}
    def connect(uri:Uri) = None
    def disconnect() = None
    def routing_event(r:Routing) = println(r)
  }
  
  po.start()
  
  po ! Connect
  po.post_message(new Message(){ override val msg = "message1" })
  po.post_message(new Message(){ override val msg = "message2" })
  po.post_message(new Message(){ override val msg = "message3" })
  po.post_message(new Message(){ override val msg = "message4" })
  po.post_message(new Message(){ override val msg = "message5" })
  
  val msgs = po.retrieve_messages()
 
  msgs match{ case msgs:List[Message] => msgs.reverse.foreach(println)}
  po ! Exit
}
*/