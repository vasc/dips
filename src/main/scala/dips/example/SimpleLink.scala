package dips.example

import dips.core.Linkable
import dips.simulation.DistributedSimulation
import dips.util.Logger.log

class SimpleLink(prefix:String) extends Linkable {
  var degree:Int = 0
  var neighbors:Array[Long] = _
  
  def init_neighbors(n:Int) = {
    neighbors = new Array(n)
  }
  
  def getNeighbor(i: Int): Long = { neighbors(i) }

  def addNeighbor(n: Long) = {
    //val is_local = if(DistributedSimulation.dht local n) "local" else "remote"
    //log.debug("Adding neighbor " + n + ", " + is_local)
    neighbors(degree) = n
    degree += 1
  }

  def contains(arg0: Long): Boolean = { false }
  
  override def clone() : Object ={
    val obj = new SimpleLink(prefix)
    obj.degree = degree
    if(neighbors != null){
    	obj.neighbors = neighbors.clone()
    }
    obj
  }
  
}