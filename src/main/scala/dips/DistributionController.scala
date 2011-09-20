package dips
/*
import dips.communication._
import scala.collection.mutable.Queue
import dips.communication.Message

case object Empty

trait DistributionController{
   
  def init(size:Int):Unit
  def make_connection(uri:Uri):Unit
  def send(msg:Message)
  def dequeue:Option[Message]
  def local_size:Int
  def size:Int
  def finished:Boolean
  def kill:Unit
}

class OneOnOne(prefix:String) extends DistributionController{
  val po = new Point2Point
  var local_size:Int = _
  var size:Int = _
  val eventQueue = new Queue[Message]()
  var finished = false
  
  po.start
  

  def init(size:Int) = {
    po.synchronized{
	    if(!po.connected){
	      println("Init called on unconnected slave, awaiting connection.")
	      po.wait() 
	    }
    }
    
    this.size = size
    local_size = size / 2
  }
  
  def make_connection(uri:Uri):Unit = { 
    po.connect(uri)
  }
  
  def send(msg:Message) = { 
    if(msg.destination_node_id >= local_size){
      po.post_message(msg) 
    }
    else{
      eventQueue enqueue msg
    }
    check_messages
  }
  
  def check_messages = {
    po.retrieve_messages().asInstanceOf[List[Message]] foreach {
      case m:Message =>
        eventQueue enqueue new Message(){
            val msg = m.msg
            val origin_node_id = m.origin_node_id + local_size
            val destination_node_id = m.destination_node_id - local_size
            val pid = m.pid}
      /*case Empty =>
        println("Empty :(")
        if(eventQueue.isEmpty) finished = true
        po.post_message(Empty)
      */  
    }
    
    
    /*=> l foreach { msg =>
      case msg:MessageWrapper =>
        eventQueue enqueue MessageWrapper(
            msg.msg,
            msg.sender + local_size,
            msg.destination - local_size,
            msg.pid)
      case Empty =>
        if(eventQueue.isEmpty) finished = true
    }*/
  }
  
  def dequeue:Option[Message] = { 
    if(eventQueue.isEmpty) check_messages
    
    if(eventQueue.isEmpty) {
      po.post_message(Empty)
      None
    }
    else Some(eventQueue.dequeue())
  }
  
  def kill ={
    po ! Exit
  }
}

*/