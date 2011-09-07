package dips.surge

import scala.actors.Future
import scala.collection.immutable.TreeSet
import dips.communication.Message
import dips.surge.BoundedDivergence.RoutableMessage
import dips.NotImplementedException
import java.security.MessageDigest


object sha1{
  val sha1 = MessageDigest getInstance "SHA1"
  
  def apply(n:Any):BigInt = {
    n match {
    	case n:Routable => BigInt(sha1 digest n.hash[String].getBytes)
    	case _ => BigInt(sha1 digest n.toString().getBytes())
    }
  }
}

trait Addressable extends Routable{
  def ip:String
  def port:Int
  def hash[String] = (ip + ":" + port).asInstanceOf[String]
}

trait Instance extends Addressable{
  var last_seen = 0L
  var mb:MessageBundle = _
  def get_messages:List[RoutableMessage]
  def send(msg:Message)
  def flush_mb()
}

class Dht(val local_addr:Instance){
  var network = TreeSet[Instance] ( local_addr ) ( new Ordering[Instance] { def compare(x:Instance, y:Instance):Int = x compare y } )
  network += local_addr
  
  def on_new_connection(new_addr:Addressable) {
    //TODO: create instance from addressable
    network += new_addr .asInstanceOf[Instance]
  }
  
  def route(r:Routable) = {
    val n = network find (r.compare(_) < 0 )
    
    n getOrElse network.first
  }
  
  def send(msg:RoutableMessage) {
    //TODO: Create envelope
    val i = this route msg
    i send msg
  }
  
  def broadcast(msg:Message):Future[Boolean] = {
    //TODO: Broadcast
    throw new NotImplementedException()
  }
  
  def self_disconnect() {
    //TODO: self_disconnect
    throw new NotImplementedException()
  }
}