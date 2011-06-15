package dips.test

import dips.{DEDSimulator, Linkable}
import peersim.core.{Control, Network, CommonState}
import peersim.config.Configuration

class SimpleWire(prefix:String) extends Control {
  val linkable_pid = Configuration getPid prefix+".protocol"
  val degree = Configuration getInt prefix+".k"
  
  def execute(): Boolean = {
    val network_size = DEDSimulator.full_network_size
    for(i <- 0 until Network.size){  
      val lnk = Network.get(i).getProtocol(linkable_pid).asInstanceOf[Linkable]
      lnk.init_neighbors(degree)
      for(j <- 0 until degree){
        val n = CommonState.r.nextInt(network_size)
        lnk.addNeighbor(n)
      }
    }
    false
  }
}