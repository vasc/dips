package dips.example

import peersim.config.Configuration
import dips.core.Linkable
import peersim.core.Network
import peersim.core.Control
import peersim.core.CommonState
import peersim.core.Simulation
import dips.core.DistributedNetwork

class SimpleWire(prefix:String) extends Control {
  val linkable_pid = Configuration getPid prefix+".protocol"
  val degree = Configuration getInt prefix+".k"
  
  def execute(): Boolean = {
    //val network_size = DEDSimulator.full_network_size
    val network_size = DistributedNetwork.network.full_size
    for(node <- DistributedNetwork.network.nodes){  
      val lnk = node.getProtocol(linkable_pid).asInstanceOf[Linkable]
      lnk.init_neighbors(degree)
      for(j <- 0 until degree){
        val n = CommonState.r.nextLong(network_size)
        lnk.addNeighbor(n)
      }
    }
    false
  }
}