package dips.example



import peersim.config.Configuration
import peersim.core.Control
import peersim.core.Network
import peersim.core.Simulation
import peersim.core.CommonState
import dips.simulation.DistributedSimulation

trait Bootstrapable{
  def bootstrap(pid:Int)
}


class SimpleBootstrap(prefix:String) extends Control{
  val mc = Configuration getInt prefix+".message_count"
  val protocol_pid = Configuration getPid prefix+".protocol"
  
  
  def execute():Boolean = {
      Range(0, mc) foreach { i =>
        val bs = Simulation.network.get(CommonState.r.nextInt(Simulation.network.size)).getProtocol(protocol_pid).asInstanceOf[Bootstrapable]
        bs.bootstrap(protocol_pid)
      }
    false
  }

}