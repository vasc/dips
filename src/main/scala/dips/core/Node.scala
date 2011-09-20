package dips.core

trait DistributedNode extends peersim.core.Node {}

object DistributedNode{
  implicit def node2node(n:peersim.core.Node):DistributedNode = { n }
  implicit def node2long(n:peersim.core.Node):Long = { n.getID }
}