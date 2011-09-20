package dips.core
import peersim.core.Protocol

trait DEDProtocol extends Protocol{
  def processEvent(dest:Long, src:Long, pid:Int, event:Any)
}