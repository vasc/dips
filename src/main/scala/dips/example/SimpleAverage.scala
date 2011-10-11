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


case class AveragePing(value:Double)
case class AveragePong(avg:Double)

class SimpleAverage(prefix:String) extends SingleValueHolder(prefix) with DEDProtocol with Bootstrapable{
  def processEvent(node:Long, from:Long, pid:Int, event:Any): Unit = { 
    event match{
      case event:AveragePing =>
      	setValue((getValue + event.value) / 2)
      	DEDSimulator.sendMessage(node, from, pid, AveragePong(getValue))
        
      case event:AveragePong =>
        setValue(event.avg)
    }
    
    send_message(node, pid)
  }
  
  def send_message(node:Long, pid:Int) = {
    val linkable = DistributedSimulation.network.get(node).getProtocol( FastConfig.getLinkable(pid) ).asInstanceOf[Linkable]
    
    val neighbor = linkable.getNeighbor(CommonState.r.nextInt(linkable.degree))
    
    DEDSimulator.sendMessage(neighbor, node, pid, AveragePing(getValue))    
  }
  
  def bootstrap(n:Node, pid:Int) = {
    send_message(n.getID, pid)
  }
}