package dips.surge
import scala.collection.mutable.Queue

import dips.surge.BoundedDivergence.RoutableMessage

case class Local

case class Migration(val instance:Instance, val nodes:List[Routable]) extends RoutableMessage

class Migrator(dht:Dht, local_nodes:List[Routable], msgs:Queue[RoutableMessage]) {
	def on_network_update(){
	  val node_migrations = local_nodes.filter{(node) =>  
	    !(dht route node).isInstanceOf[Local]
	  }.groupBy (dht route _)
	  
	  node_migrations.foreach{(msgs_tupple) =>
	    dht send Migration(msgs_tupple._1, msgs_tupple._2)
	  }
	  
	  msgs.filter(!dht.route(_).isInstanceOf[Local]) foreach ( dht send _ )
	}
}