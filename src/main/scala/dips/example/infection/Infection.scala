package dips.example.infection

import dips.core.DEDProtocol
import dips.core.Linkable
import dips.example.Bootstrapable
import dips.simulation.DEDSimulator
import peersim.config.Configuration
import peersim.config.FastConfig
import peersim.core.CommonState
import peersim.core.Simulation
import peersim.vector.SingleValueHolder
import dips.core.DistributedNetwork
import dips.util.Logger.log
import dips.simulation.DistributedSimulation
import dips.stats.Registry
import dips.stats.Stats

case object Increase

class Infection(prefix:String) extends SingleValueHolder(prefix) with DEDProtocol with Bootstrapable{
  val degree = Configuration getInt prefix + ".degree"
  val limit = Configuration getInt prefix + ".limit"
  
  Registry.get[Stats]("performance").save("infection.degree", degree)
  
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
  
  def bootstrap(pid:Int) = {
    val node = Simulation.network.get(CommonState.r.nextInt(Simulation.network.size)).getID
    send_message(node, pid)
  }
}