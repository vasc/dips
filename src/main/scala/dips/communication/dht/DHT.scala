package dips.communication.dht

import java.net.InetAddress

import scala.actors.remote.Node
import scala.actors.remote.RemoteActor
import scala.actors.remote.TcpService
import scala.actors.OutputChannel
import scala.actors.AbstractActor
import scala.annotation.serializable
import scala.collection.immutable.TreeSet
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap

import dips.communication.Addressable
import dips.communication.Anounce
import dips.communication.Connect
import dips.communication.Envelope
import dips.communication.Message
import dips.communication.PostOffice
import dips.communication.Routable
import dips.communication.Routing
import dips.communication.Subscription
import dips.communication.Uri
import dips.surge.MessageBundle
import dips.util.Logger.log
import dips.NotImplementedException

class Instance(uri:Uri) extends Addressable{
  val port = uri.port
  val ip = uri.ip
  val service = uri.service
  
  val actor:AbstractActor = RemoteActor.select(Node(ip, port), service)
  var last_seen = 0L
  var mb:MessageBundle = _
  def get_messages:List[Envelope] = {
    throw new NotImplementedException()
  }
  /*def send(msg:Message){
    throw new NotImplementedException()
  }
  def flush_mb(){
    throw new NotImplementedException()
  }*/
  
  override lazy val hash = uri.hash
  
  override def toString() = uri.toString
}

object Instance{
  implicit def instance2actor(i:Instance) = i.actor
  implicit def instance2uri(i:Instance) = new Uri(i.ip, i.port, i.service)
  implicit def uri2instance(uri:Uri) = new Instance(uri)
}

object DHT{
  RemoteActor.classLoader = getClass().getClassLoader()
  val DEFAULT_PORT = 7653
  val DEFAULT_SERVICE = 'dht 
}

class DHT(var local_port:Int) extends PostOffice{
  //Debug.level = 9
  
  if(local_port == 0){ local_port = TcpService.generatePort }
  val local_addr:Uri = new Uri(InetAddress.getLocalHost.getHostAddress, local_port, DHT.DEFAULT_SERVICE)
  log.debug("Listenning on local_port: " + local_port)
  
  RemoteActor.alive(local_addr.port)
  RemoteActor.register(local_addr.service, this)
  
  val subscribers = new HashMap[Symbol, HashSet[AbstractActor]]
  var instances = TreeSet[Instance](local_addr) ( new Ordering[Instance] { def compare(x:Instance, y:Instance):Int = x compare y } )

  def this(){ this(DHT.DEFAULT_PORT) }
  
  /*
  def on_new_connection(new_addr:Addressable) {
    //TODO: create instance from addressable
    instances += new_addr:Instance
  }
  */
  
  /**
   * Chord like routing behavior
   * @return resulting instance 
   */
  def route(r:Routable) = {
    //val n = instances find (r.hash < _.hash )
    //val result = (n getOrElse instances.first)
    
    instances.toIndexedSeq((r.hash.abs % instances.size).toInt)
    //log.debug(r + " #" + r.hash % 1000000 + " routed to " + result + " #" + result.hash % 1000000)
    //result
  }
  
  def local(r:Routable) = {
    val instance = route(r)
    val result = instance.hash == local_addr.hash
    //log.debug(instance + " #" + instance.hash % 1000000 + " is equal to " + local_addr + " #" + local_addr.hash % 1000000 + ": " + result)
    result
  }
  
  /**
   * Syntactic sugar to send_to(route(Routable), msg)
   */
  def send(msg:Message) {
    send_to(this route msg, Envelope(msg, local_addr))
  }
  
  /**
   * Send message directly to an instance,
   * override routing
   */
  def send_to(i:Instance, msg:Any) {
    //TODO: Create envelope on send
    translate(i) ! msg
  }
  
  /**
   * Send message to all instances
   */
  def broadcast(msg:Any) {
    //TODO: Broadcast
    log.debug("Sending broadcast and waiting ack")
    instances.map(translate(_) ! msg)//.forall(_().asInstanceOf[Boolean])
  }
  
  /**
   * Take message from message queue
   */
  def dequeue():Option[Message] = {
    //TODO: self_disconnect
    throw new NotImplementedException()
  }
  
  /**
   * @deprecated
   * Inquire whether simulation has finished
   */
  def finished:Boolean = {
    //TODO: self_disconnect
    throw new NotImplementedException()
  }
  
  /**
   * Connect to a new node
   * Service is implied as DHT.DEFAULT_SERVICE
   */
  def connect(addr:Addressable) { connect(new Uri(addr.ip, addr.port, DHT.DEFAULT_SERVICE)) }
  
  /**
   * Connect to a new node
   */
  def connect(uri: Uri) { 
    log.debug("connecting to network: " + uri + "with local_addr: " + local_addr)
    val instance = new Instance(uri)
    val info = instance !? Connect(local_addr)
    info match{
      case s:HashSet[Uri] =>
        log.debug("set response: " + s)
        s map { new Instance(_) } foreach { i =>
          log.debug("Instance: " + i)
          if(!instances.contains(i)){
              log.debug("Sending Anounce")
        	  i ! Anounce(local_addr)
        	  instances += i
        	  log.debug("Instances updated: (" + instances.size + ") with " + i)
          }
        }
    }
  }

  /**
   * Remove this instance from the network
   */
  def disconnect(): Unit = {  }

  /**
   * Translate instance abstraction to concrete Actor
   */
  def translate(dest: Instance): AbstractActor = { dest.actor }

  /**
   * Internal method, handles routing events in the network
   */
  protected def routing_event(event: Routing): Unit = { 
    log.debug("routing event: " + event)
    
    event match{
      case Connect(uri:Uri) => 
        val instance = new Instance(uri)
        instances += instance
        reply(this.uriSet)
      case Anounce(uri:Uri) =>
        val i = new Instance(uri)
        instances += i
        log.debug("Instances updated: (" + instances.size + ") with " + i)
    }
  }
  
  def uriSet() = {
    val set = new HashSet[Uri]()
    
    this.instances foreach { i =>
      set += Uri(i.ip, i.port, i.service)
    }
    set
  }
  
  /**
   * Connects an @param actor with Publication messages with @param name
   */
  def subscribe(name:Symbol, actor:AbstractActor) {
    val set = {
      if(!(subscribers contains name)){
        subscribers put (name,new HashSet[AbstractActor]())
      }
      subscribers(name)
    }

    set += actor
  }
  
  /**
   * Internal method, delivers message to subscriber
   */
  protected def deliver_to_subscribers(name:Symbol, msg:Any, sender:OutputChannel[Any]){
    subscribers(name).foreach (_ ! new Subscription(name, msg, sender))
  }
}