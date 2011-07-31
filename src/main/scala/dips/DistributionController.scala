package dips

import dips.communication._
import scala.collection.mutable.Queue

case object Empty extends Message

trait DistributionController{
   
  def init(size:Int):Unit
  def make_connection(uri:Uri):Unit
  def send(msg:MessageWrapper)
  def dequeue:Option[MessageWrapper]
  def local_size:Int
  def size:Int
  def finished:Boolean
  def kill:Unit
}

class OneOnOne(prefix:String) extends DistributionController{
  val po = new Point2Point
  var local_size:Int = _
  var size:Int = _
  val eventQueue = new Queue[MessageWrapper]()
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
  
  def send(msg:MessageWrapper) = { 
    if(msg.destination >= local_size){
      po.post_message(None, msg) 
    }
    else{
      eventQueue enqueue msg
    }
    check_messages
  }
  
  def check_messages = {
    po.retrieve_messages().asInstanceOf[List[Message]] foreach {
      case msg:MessageWrapper =>
        eventQueue enqueue MessageWrapper(
            msg.msg,
            msg.sender + local_size,
            msg.destination - local_size,
            msg.pid)
      case Empty =>
        println("Empty :(")
        if(eventQueue.isEmpty) finished = true
        po.post_message(None, Empty)
        
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
  
  def dequeue:Option[MessageWrapper] = { 
    if(eventQueue.isEmpty) check_messages
    
    if(eventQueue.isEmpty) {
      po.post_message(None, Empty)
      None
    }
    else Some(eventQueue.dequeue())
  }
  
  def kill ={
    po ! Exit
  }
}