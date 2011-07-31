package dips.test

import dips.{DEDProtocol, DEDSimulator, Linkable}

import peersim.core.{Node, Network, CommonState}
import peersim.config.FastConfig
import peersim.vector.SingleValueHolder

case class AveragePing(value:Double)
case class AveragePong(avg:Double)

class SimpleAverage(prefix:String) extends SingleValueHolder(prefix) with DEDProtocol with Bootstrapable{
  def processEvent(node:Int, from:Int, pid:Int, event:AnyRef): Unit = { 
    event match{
      case event:AveragePing =>
      	setValue((getValue + event.value) / 2)
      	DEDSimulator.sendMessage(node, from, pid, AveragePong(getValue))
        
      case event:AveragePong =>
        setValue(event.avg)
    }
    
    send_message(node, pid)
  }
  
  def send_message(node:Int, pid:Int) = {
    val linkable = Network.get(node).getProtocol( FastConfig.getLinkable(pid) ).asInstanceOf[Linkable]
    
    val neighbor = linkable.getNeighbor(CommonState.r.nextInt(linkable.degree))
    
    DEDSimulator.sendMessage(node, neighbor, pid, AveragePing(getValue))    
  }
  
  def bootstrap(pid:Int) = {
    send_message(CommonState.r.nextInt(Network.size), pid)
  }
}