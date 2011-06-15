package dips

import peersim.core.Protocol

trait Linkable extends Protocol{
  def degree:Int
  
  def init_neighbors(n: Int): Unit
  
  def getNeighbor(arg0: Int): Int

  def addNeighbor(arg0: Int): Unit

  def contains(arg0: Int): Boolean
}