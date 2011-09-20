package dips.core

import peersim.core.Protocol

trait Linkable extends Protocol{
  def degree:Int
  
  def init_neighbors(n: Int): Unit
  
  def getNeighbor(arg0: Int): Long

  def addNeighbor(arg0: Long): Unit

  def contains(arg0: Long): Boolean
}