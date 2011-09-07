package dips.surge
import dips.communication.Message
import scala.collection.mutable.Buffer
import dips.surge.BoundedDivergence.RoutableMessage

class MessageBundle(val size:Int) {
	var messages:Buffer[Message] = _
	
	def add_message(msg:Message) = {
	  messages += msg
	  messages.size >= size
	}
	
	def clear() = messages.clear()
}

object MessageBundler{
  var dht:Dht = _
  def initiate(dht:Dht, size:Int){
    this.dht = dht
    for(instance <- dht.network){
      instance.mb = new MessageBundle(size)
    }
  }
  
  def on_new_message(msg:RoutableMessage){
    val i = dht route msg
    if( i.mb add_message msg ){
      i.flush_mb()
    }
  }
}