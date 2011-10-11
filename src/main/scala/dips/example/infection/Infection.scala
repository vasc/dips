package dips.example.infection

import dips.core.DEDProtocol
import dips.core.Linkable
import dips.example.Bootstrapable
import dips.simulation.DEDSimulator
import dips.simulation.DistributedSimulation
import peersim.config.Configuration
import peersim.config.FastConfig
import peersim.core.CommonState
import peersim.core.Simulation
import peersim.vector.SingleValueHolder
import peersim.core.Node

case object Increase

class Infection(prefix:String) extends SingleValueHolder(prefix) with DEDProtocol with Bootstrapable{
  val degree = Configuration getInt prefix + ".degree"
  val limit = Configuration getInt prefix + ".limit"
  
  
  
  def processEvent(node:Long, from:Long, pid:Int, event:Any): Unit = { 
    if(getValue < limit){
    	event match{
    	  case Increase => setValue(getValue + 1)
    	}
    
    	send_message(node, pid) 
    }
  }
  
  def send_message(node:Long, pid:Int) = {
    val linkable = DistributedSimulation.network.get(node).getProtocol( FastConfig.getLinkable(pid) ).asInstanceOf[Linkable]
    
    for(i <- 0 until degree){
        val neighbor = linkable.getNeighbor(CommonState.r.nextInt(linkable.degree))
    	DEDSimulator.sendMessage(neighbor, node, pid, Increase)
    }
  }
  
  def bootstrap(n:Node, pid:Int) = {
    val node = Simulation.network.get(CommonState.r.nextInt(Simulation.network.size)).getID
    send_message(node, pid)
  }
}