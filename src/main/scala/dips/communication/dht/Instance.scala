package dips.communication.dht

import scala.actors.remote.Node
import scala.actors.remote.RemoteActor
import scala.actors.AbstractActor
import scala.annotation.serializable
import dips.communication.Addressable
import dips.communication.Uri
import dips.NotImplementedException
import dips.communication.Message
import scala.collection.mutable.Buffer
import dips.core.DistributedNetwork
import dips.simulation.DistributedSimulation
import peersim.config.Configuration
import dips.util.Logger.log


class Instance(uri:Uri) extends Addressable{
  
  val port = uri.port
  val ip = uri.ip
  val service = uri.service
  
  val actor:AbstractActor = RemoteActor.select(Node(ip, port), service)
  var last_seen = 0L
  lazy val mb:MessageBundle = {
    val BUNDLE_SIZE = Configuration getInt "distributed.bundle.size"
    log.debug("Creating bundle  with size " + BUNDLE_SIZE)
    new MessageBundle(BUNDLE_SIZE)
  }
  
  def get_messages:List[Message] = {
    throw new NotImplementedException()
  }
  
  def !(msg:Message){
    //log.debug("Saving message in Bundle of " + this)
    mb add_message msg match{
      case lm:Some[Buffer[Message]] => actor ! lm.get
      case None => Unit
    }
  }
  
  override lazy val hash = uri.hash
  
  override def toString() = uri.toString
}

object Instance{
  
  implicit def instance2actor(i:Instance) = i.actor
  implicit def instance2uri(i:Instance) = new Uri(i.ip, i.port, i.service)
  implicit def uri2instance(uri:Uri) = new Instance(uri)
}
