package dips.test

import scala.collection.mutable.LinkedHashSet
import scala.Seq

import dips.Linkable
import peersim.core.Node
import peersim.config.Configuration

class SimpleLink(prefix:String) extends Linkable {
  var degree:Int = 0
  var neighbors:Array[Int] = _
  
  def init_neighbors(n:Int) = {
    neighbors = new Array(n)
  }
  
  def getNeighbor(i: Int): Int = { neighbors(i) }

  def addNeighbor(n: Int) = {
    neighbors(degree) = n
    degree += 1
  }

  def contains(arg0: Int): Boolean = { false }
  
  override def clone() : Object ={
    val obj = new SimpleLink(prefix)
    obj.degree = degree
    if(neighbors != null){
    	obj.neighbors = neighbors.clone()
    }
    obj
  }
  
}