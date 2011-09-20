package dips.communication

import java.net.InetAddress
import dips.util.sha1
import dips.util.Logger.log

object Routable{
  /*implicit def bigInt2routable(i:BigInt) = {
    new Routable(){
      val hash = sha1(i.toByteArray)
    }
  }*/
  
  implicit def long2bigint(l:Long) = {
    new Routable(){
      def hash = sha1(BigInt(l).toByteArray)
    }
  }
}

trait Routable extends Ordered[Routable]{

	//val sha1 = MessageDigest getInstance "SHA1"
  
	def hash:BigInt
	
	def compare(that: Routable) = {
	  this.hash compare that.hash
	}
	
	override def equals(other:Any) = other match{
	  case r:Routable => 
	    //log.debug(this.hash + " equals " + r.hash)
	    this.hash == r.hash
	  case _ => false
	}
}

trait Addressable extends Routable{
  def ip:String
  def port:Int
  lazy val hash = {
    val hash = sha1((ip + ":" + port).getBytes)
    log.debug( "hash: '" + ip + ":" + port +"' => " +  hash % 1000000)
    hash
  }
}

object Addressable{
  implicit def tuple2addressable(t:Tuple2[String, Int]):Addressable = {
    new Addressable(){
      def ip = t._1
      def port = t._2
     }
  }
  
  implicit def inetaddr2addressable(ia:InetAddress):Addressable = {
    new Addressable(){
      def ip = ia.getHostAddress
      def port = 0
    }
  }
}

@serializable
case class Uri(val ip:String, val port:Int, val service:Symbol) extends Addressable{
  override def toString = {
    ip + ":" + port + ":" + service
  }
  
  override lazy val hash = sha1((ip + ":" + port + ":" + service).getBytes)
}

trait Message extends Routable {
  val destination_node_id:Long
  val origin_node_id:Long
  val pid:Int
  val msg:Any
}


