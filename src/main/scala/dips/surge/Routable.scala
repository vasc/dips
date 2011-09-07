package dips.surge

import java.security.MessageDigest;


trait Routable extends Ordered[Routable]{

	val sha1 = MessageDigest getInstance "SHA1"
  
	def hash[T]:T
	
	def compare(that: Routable) = {
	  val o1_hash = BigInt(sha1.digest( this.hash[String].getBytes ))
	  val o2_hash = BigInt(sha1.digest( that.hash[String].getBytes ))
	  
	  if (o1_hash == o2_hash) 0
	  else if (o1_hash < o2_hash ) -1
	  else 1
	}
}

