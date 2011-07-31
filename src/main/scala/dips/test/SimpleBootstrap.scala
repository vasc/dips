package dips.test

import dips.{Linkable, DEDSimulator}

import peersim.core.{Control, Network, CommonState}
import peersim.config.Configuration

trait Bootstrapable{
  def bootstrap(pid:Int)
}

class SimpleBootstrap(prefix:String) extends Control {
  val mc = Configuration getInt prefix+".message_count"
  val protocol_pid = Configuration getPid prefix+".protocol"
  
  
  def execute():Boolean = {
    Range(0, mc) foreach { i =>
      val bs = Network.get(CommonState.r.nextInt(Network.size)).getProtocol(protocol_pid).asInstanceOf[Bootstrapable]
      bs.bootstrap(protocol_pid)
    }
    false
  }

}