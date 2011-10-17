package dips.example

import dips.core.DEDProtocol
import dips.core.Linkable
import dips.simulation.DEDSimulator
import peersim.config.FastConfig
import peersim.core.CommonState
import peersim.core.Simulation
import peersim.vector.SingleValueHolder
import dips.core.DistributedNetwork
import dips.simulation.DistributedSimulation
import peersim.core.Node
import dips.util.Logger.log

trait PingPong
case class AveragePing(value:Double) extends PingPong
case class AveragePong(value:Double) extends PingPong

class SimpleAverage(prefix:String) extends SingleValueHolder(prefix) with DEDProtocol with Bootstrapable{
  def processEvent(local_node:Long, peer_node:Long, pid:Int, event:Any): Unit = { 
    
    val value = getValue
    event match{
      case event:AveragePing =>
        //log.debug("old, new: " + getValue  +", " + event.value)
      	setValue((getValue + event.value) / 2)
      	DEDSimulator.sendMessage(peer_node, local_node, pid, AveragePong(value))
        
      case event:AveragePong =>
        //log.debug("old, new: " + getValue  +", " + event.value)
        setValue((getValue + event.value) / 2)
        send_message_from(local_node, pid)
    }
  }
  
  def send_message_from(node:Long, pid:Int) = {
    val linkable = DistributedSimulation.network.get(node).getProtocol( FastConfig.getLinkable(pid) ).asInstanceOf[Linkable]
    
    val neighbor = linkable.getNeighbor(CommonState.r.nextInt(linkable.degree))
    
    DEDSimulator.sendMessage(neighbor, node, pid, AveragePing(getValue))    
  }
  
  def bootstrap(n:Node, pid:Int) = {
    send_message_from(n.getID, pid)
  }
}