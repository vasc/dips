package dips.example.average.peersim

import dips.core.DEDProtocol
import peersim.core.Linkable
import dips.simulation.DEDSimulator
import peersim.config.FastConfig
import peersim.core.CommonState
import peersim.core.Simulation
import peersim.vector.SingleValueHolder
import dips.core.DistributedNetwork
import dips.simulation.DistributedSimulation
import peersim.core.Node
import dips.util.Logger.log
import dips.example.AveragePong
import dips.example.AveragePing
import dips.example.Bootstrapable
import peersim.core.Protocol
import peersim.edsim.EDProtocol
import dips.example.PingPong
import peersim.edsim.EDSimulator

object DelayCounter{
  private var delay = 0
  private lazy val initial_time = CommonState.getTime
  
  def next = {
    delay += 1
    delay - CommonState.getTime + initial_time
  }
}

case class Event(val sender:Node, val msg:PingPong)

class SimplePeersimAverage(prefix:String) extends SingleValueHolder(prefix) with EDProtocol with Bootstrapable{
  def processEvent(node:Node, pid:Int, event:Any): Unit = { 
    
    val value = getValue
    event match{
      case Event(sender, msg:AveragePing) =>
        //log.debug("old, new: " + getValue  +", " + event.value)
      	setValue((getValue + msg.value) / 2)
      	EDSimulator.add(DelayCounter.next, Event(node, AveragePong(value)), sender, pid)
      	
      	//DEDSimulator.sendMessage(node, local_node, pid, AveragePong(value))
        
      case Event(sender, event:AveragePong) =>
        //log.debug("old, new: " + getValue  +", " + event.value)
        setValue((getValue + event.value) / 2)
        send_message_from(node, pid)
    }
  }
  
  def send_message_from(node:Node, pid:Int) = {
    val linkable = node.getProtocol( FastConfig.getLinkable(pid) ).asInstanceOf[Linkable]
    
    val neighbor = linkable.getNeighbor(CommonState.r.nextInt(linkable.degree))
    
    EDSimulator.add(DelayCounter.next, Event(node, AveragePing(getValue)), neighbor, pid)    
  }
  
  def bootstrap(n:Node, pid:Int) = {
    send_message_from(n, pid)
  }
}