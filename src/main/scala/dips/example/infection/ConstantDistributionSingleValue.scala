package dips.example.infection

import peersim.vector.VectControl
import peersim.config.Configuration
import dips.simulation.DistributedSimulation
import peersim.core.Control
import peersim.vector.SingleValue

class ConstantDistributionSingleValue(prefix:String) extends Control {
  val constant = Configuration.getDouble(prefix+".value")
  val pid = Configuration.getPid(prefix+".protocol")
  
  def execute() = {
    
    for(node <- DistributedSimulation.network.nodes){
    	node.getProtocol(pid).asInstanceOf[SingleValue] setValue constant
    }
    false
  }

}