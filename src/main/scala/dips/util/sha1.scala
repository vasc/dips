package dips.util
import java.security.MessageDigest
import dips.communication.Routable

object sha1{
  val sha1 = MessageDigest getInstance "SHA1"
  
  def apply(n:Any):BigInt = {
    n match {
    	//case n:Routable => BigInt(sha1 digest n.hash)
        case n:Array[Byte] => BigInt(sha1 digest n)
    	case n:BigInt => BigInt(sha1 digest n.toByteArray)
    	//case _ => BigInt(sha1 digest n.toString().getBytes())
    }
  }
}