package dips.example.average.peersim

import peersim.core.Linkable
import dips.simulation.DistributedSimulation
import dips.util.Logger.log
import peersim.core.Node
import scala.collection.mutable.ListBuffer
import peersim.core.Protocol

class SimplePeersimLink(prefix:String) extends Linkable with Protocol {
  var degree:Int = 0
  var neighbors:ListBuffer[Node] = new ListBuffer[Node]
  
  
  def getNeighbor(i: Int): Node = { neighbors(i) }

  def pack(){}
  def onKill(){}
  
  override def addNeighbor(n: Node) = {
    //val is_local = if(DistributedSimulation.dht local n) "local" else "remote"
    //log.debug("Adding neighbor " + n + ", " + is_local)
    neighbors += n
    degree += 1
    true
  }

  def contains(arg0: Node): Boolean = { false }
  
  override def clone() : Object ={
    val obj = new SimplePeersimLink(prefix)
    obj.degree = degree
    if(neighbors != null){
    	obj.neighbors = neighbors.clone()
    }
    obj
  }
  
}