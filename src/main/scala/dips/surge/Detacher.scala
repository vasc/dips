package dips.surge

import scala.actors.Future
import dips.communication.Message
import dips.communication.dht.DHT
import dips.communication.Routing
import dips.communication.Disconnect

object Detacher {
  def detach(dht:DHT){
    
    //while(! (dht broadcast Disconnect)){}
    dht.disconnect()
  }
  
  def on_new_message(dht:DHT, msg:Message){
    val destination = dht route msg.destination_node_id
    destination ! msg
  }
}

/**

	detach: (dht) ->
		confirmations = dht.broadcast DISCONNECT
		dht.self_disconnect()
		#migrator.on_network_updates is called
		#wait for broadcast confirmations before exiting
		confirmations.wait()

	on_new_message: (msg) ->
		destination = this.dht.route msg
		this.send msg, destination
*/