package dips.stats

import peersim.config.Configuration
import peersim.core.Control

trait Sub{
  def receive(key:Symbol, value:Any)
}

class Stats(val prefix:String) extends Control {

  lazy val testname =  {
    Configuration.getString(prefix + ".name", prefix)
  }
  
  def clear(){
    clear(testname)
  }
  def clear(key:Namespace){
    Registry.redis.del(Registry.id+key)
  }
  
  def add(value:Any) = {
    addMap(testname, value, Map.empty)
  }
  
  def add(key:Any, value:Any) = {
    addMap(key, value, Map.empty)
  }
  
  def addMap(key:Namespace, value:Any, m:Map[Any, Any]) = {
    val index = Registry.redis.lpush(Registry.id + key, value)
    index match{
      case Some(i:Int) => m.foreach{ t =>
          val namespace = Registry.id+key+i+t._1
          Registry.redis.set(namespace, t._2)
        }
        i
      case None => -1
    }
  }
  
  def save(key:Namespace, value:Any){
    Registry.redis.set(Registry.id+testname+key, value)
  }
  
  def execute():Boolean = false
}