package dips.surge
import scala.collection.mutable.Queue
import dips.communication.dht.DHT
import dips.communication.dht.Instance
import dips.communication.Message
import dips.communication.Routable

case class Local

case class Migration(val nodes:List[Routable])

class Migrator(dht:DHT, local_nodes:List[Routable], msgs:Queue[Message]) {
	def on_network_update(){
	  val node_migrations = local_nodes.filter{(node) =>  
	    !(dht route node).isInstanceOf[Local]
	  }.groupBy (dht route _)
	  
	  node_migrations.foreach{(msgs_tupple) =>
	    dht.send_to(msgs_tupple._1, Migration(msgs_tupple._2))
	  }
	  
	  msgs.filter(!dht.route(_).isInstanceOf[Local]) foreach ( dht send _ )
	}
}