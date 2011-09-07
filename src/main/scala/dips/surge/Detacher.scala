package dips.surge
import scala.actors.Future
import dips.communication.Message
import dips.surge.BoundedDivergence.RoutableMessage

case class Disconnect extends Message

object Detacher {
  def detach(dht:Dht){
    val confirmations:Future[Boolean] = dht broadcast Disconnect()
    dht.self_disconnect()
    confirmations()
  }
  
  def on_new_message(dht:Dht, msg:RoutableMessage){
    val destination = dht route msg
    destination send msg
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