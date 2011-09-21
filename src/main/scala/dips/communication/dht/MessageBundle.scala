package dips.communication.dht

import dips.communication.Message
import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer

class MessageBundle(val size:Int) {
	var messages:Buffer[Message] = new ListBuffer()
	
	def add_message(msg:Message) = {
	  messages += msg
	  if(messages.size >= size){
	    Some(flush())
	  }
	  else{
	    None
	  }
	}
	
	def flush() = {
	  val result = messages
	  messages = new ListBuffer()
	  result
	}
}

/*object MessageBundler{
  var dht:DHT = _
  def initiate(dht:DHT, size:Int){
    this.dht = dht
    for(instance <- dht.instances){
      instance.mb = new MessageBundle(size)
    }
  }
  
  def on_new_message(msg:Message) = {
    val i = dht route msg.destination_node_id
    i.mb add_message msg 
  }
}*/