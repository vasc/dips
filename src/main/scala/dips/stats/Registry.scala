package dips.stats

import scala.collection.mutable.HashMap
import peersim.core.Control
import com.redis._
import peersim.config.Configuration

object Namespace{
  implicit def any2namespace(a:Any) = { new Namespace(a.toString) }
}

class Namespace(val str:String){
  def +(next:Namespace) = { new Namespace(this.str+":"+next.str) }
  override def toString = this.str
}

object Registry {
  lazy val simname = Configuration.getString("simulation.name")
  lazy val simid = Configuration.getString("simulation.id", "")
  lazy val simversion = Configuration.getString("simulation.version", "")
  lazy val id:Namespace = {
    var r = simname
    if(simversion != "") r += simversion
    if(simid != "") r += simid
    r
  }
  
  lazy val redis = {
    val r = new RedisClient("localhost", 6379)
    r.sadd("simulations", id)
    r
  }
  
  def controls = new HashMap[String, Control]()
  
  def register(name:String, writter:Control){
    controls(name) = writter
  }
  
  def get[T](name:String):T = {
    controls.get(name).asInstanceOf[T]
  }

}