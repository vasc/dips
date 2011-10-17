package dips.example.average.peersim

import peersim.config.Configuration
import peersim.core.Linkable
import peersim.core.Network
import peersim.core.Control
import peersim.core.CommonState
import peersim.core.Simulation
import dips.core.DistributedNetwork
import dips.simulation.DistributedSimulation
import dips.util.Logger.log

class SimplePeersimWire (prefix:String) extends Control {
  val linkable_pid = Configuration getPid prefix+".protocol"
  val degree = Configuration getInt prefix+".k"
  
  def execute(): Boolean = {
    //val network_size = DEDSimulator.full_network_size
    val size = Simulation.network.size
    
    for(i <- Range(0, size)){  
      val node = Simulation.network.get(i)
      val lnk = node.getProtocol(linkable_pid).asInstanceOf[Linkable]
      //lnk.    .init_neighbors(degree)
      for(j <- 0 until degree){
        val n = CommonState.r.nextInt(size)
        lnk.addNeighbor(Simulation.network.get(n))
      }
    }
    false
  }
}