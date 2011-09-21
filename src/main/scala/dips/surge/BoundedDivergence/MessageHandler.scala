package dips.surge.BoundedDivergence

import scala.collection.mutable.Queue

import dips.communication.dht.DHT
import dips.communication.dht.Instance
import dips.communication.Message
import peersim.core.CommonState

/*
class MessageHandler(bounded_limit:Long, dht:DHT) {
	val message_queue = new Queue[Message]
	
	val time = CommonState.getTime
	
	//TODO: Actual instances in DHT
	dht.instances.foreach { _.asInstanceOf[Instance].last_seen = time }
	
	def add_message( env:Envelope[Message] ) = {
	  message_queue.enqueue(env.msg)
	  
	  //Where the message came from
	  val origin = dht route env.origin
	  origin.last_seen = CommonState.getTime
	}
	
	def retrieve_message() = {
	  val current_time = CommonState.getTime
	  
	  val over_limit = dht.instances.filter { (i) =>
	    val diff = current_time - i.asInstanceOf[Instance].last_seen
	    diff > bounded_limit
	  }
	  
	  for(i <- over_limit){
	    i.asInstanceOf[Instance].get_messages.foreach { add_message(_) }
	  }
	  
	  message_queue.dequeue()
	}
}
*/
