package dips.simulation
import scala.annotation.serializable
import scala.collection.mutable.SynchronizedQueue
import scala.collection.mutable.HashMap

import DistributedSimulation.simulation
import dips.communication.dht.DHT
import dips.communication.Message
import dips.communication.Uri
import peersim.core.Node

import dips.util.Logger.log

case class Migration(nodes:Option[List[Node]], msgs:Option[List[Message]], origin:Uri)


class Migrator(val dht:DHT, val msgs:SynchronizedQueue[Message]) {

  lazy val nodes:HashMap[Long, Node] = DistributedSimulation.network.getNodeMap
  
  def bootstrap(){
    dht.instances.foreach{
      instance =>
        instance ! Migration(None, None, dht.local_addr)
    }
  }
  
  def apply(m:Migration){
    
    log debug ("Received migration " + m)
    simulation.synchronized{
      nodes ++= m.nodes getOrElse List() map (n => ( n.getID, n))
      msgs ++= m.msgs getOrElse List()
    }
    apply
  }
  
  def apply(){
	  simulation.synchronized{
	    //TODO: remove msgqueue race condition and implement msg migration
	    
	    val alien_nodes = nodes.values filter { node => !(dht local node.getID) }
	    val alien_messages = msgs filter { msg => !(dht local msg.destination_node_id) }
	  
	    val node_migrations = alien_nodes.toList groupBy (dht route _.getID)
	    val msg_migrations = alien_messages.toList groupBy (dht route _.destination_node_id)
	  
	    for( i <- node_migrations.keySet/* ++ msg_migrations.keySet */){
	      log debug "Sending migration to " + i.uri
	      i ! Migration(node_migrations.get(i), None/*msg_migrations.get(i)*/, dht.local_addr)
	    }
	    
	    nodes --= alien_nodes map { _.getID }
	    
	    //race condition???
	    //msgs.dequeueAll { alien_messages contains _ }
	  }
	}
}