package dips.core

import peersim.core._
import java.util.Comparator
import dips.communication.dht.DHT
import scala.collection.mutable.HashMap
import dips.NotImplementedException
import peersim.config.Configuration
import dips.util.Logger.log
import dips.simulation.DistributedSimulation


class DistributedNetwork(val dht:DHT) extends Network {
  //TODO: network becomes fragile after a remove
  
  protected var node_map:HashMap[Long, Node] = _
  protected var local_node_map:HashMap[Int, Node] = _
  protected var network_size = 0L
  
  def getNodeMap = node_map
  
  override def add(node:Node) = {
    if(dht local node.getID){
    	node_map(node.getID) = node
    	local_node_map(node.getIndex) = node
    	network_size += 1
    }
    else{
      throw new NotImplementedException()
    }
  }
  
  override def reset() = {
    log.debug("--- Starting network reset")
    node_map = new HashMap[Long, Node]()
    local_node_map = new HashMap[Int, Node]()
    
    network_size = Configuration.getLong(Network.PAR_SIZE)
    log.debug("--- Network size: " + network_size)
    
    log.debug("--- Creating prototype")
    prototype = createPrototypeNode()
    
    if(DistributedSimulation.simulation.status == 'init){
      log.debug("--- Generating nodes")
      for(val i:Long <- 0L until network_size if dht local i){
        //log.debug("---- node: " + i)
        val node = prototype.duplicate(i)
        //log.debug(i)
        node.setIndex(node_map.size)
        local_node_map(node.getIndex) = node
        node_map(i) = node
      }
      log.debug("--- Number of nodes created in this instance: " + node_map.size)
    }
    /*for(val i  <- 0 until list_pair.size){
      list_pair(i)._2.setIndex(i)
    }

    node_map = HashMap(list_pair:_*)*/
  }
  
  override def get(index:Int):Node = { 
    //node_map(index)
    //throw new NotImplementedException()
    //log.debug("getint " + index)
    node_map.find{ _._2.getIndex == index }.get._2
 }
  
  def get(id:Long):Node = {
    node_map(id)
  }
  
  def nodes() = {
    node_map.values
  }
  
  //override def getCapacity():Int
  
  override def remove():Node = {
    val node = node_map.first
    remove(node._1)
  }
  
  override def remove(index:Int):Node = { 
    throw new NotImplementedException()
  }
  
  def remove(id:Long):Node = {
    val node = node_map(id)
    node_map -= id
    //network_size -= 1
    node.setFailState(Fallible.DEAD);
    node
  }
  
  //override def setCapacity(capacity:Int)
  
  //override def shuffle()
  
  override def size():Int = { node_map.size } 
  
  def full_size() = { network_size }
  //def sort(o:Ordering[Node])
  
  //override def swap(left:Int, right:Int)
  
  //override def test()
}