package dips.example



import peersim.config.Configuration
import peersim.core.CommonState
import peersim.core.Control
import peersim.core.Simulation
import peersim.core.Node

trait Bootstrapable{
  def bootstrap(n:Node, pid:Int)
}


class SimpleBootstrap(prefix:String) extends Control{
  val mc = Configuration getInt prefix+".message_count"
  val protocol_pid = Configuration getPid prefix+".protocol"
  
  
  def execute():Boolean = {
      Range(0, mc) foreach { i =>
        val n = Simulation.network.get(CommonState.r.nextInt(Simulation.network.size))
        val bs = n.getProtocol(protocol_pid).asInstanceOf[Bootstrapable]
        bs.bootstrap(n, protocol_pid)
      }
    false
  }

}