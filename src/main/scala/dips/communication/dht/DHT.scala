package dips.communication.dht

import java.net.InetAddress

import scala.actors.remote.RemoteActor
import scala.actors.remote.TcpService
import scala.actors.OutputChannel
import scala.actors.AbstractActor
import scala.collection.immutable.TreeSet
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap

import dips.communication.Addressable
import dips.communication.Anounce
import dips.communication.Connect
import dips.communication.Connected
import dips.communication.Message
import dips.communication.PostOffice
import dips.communication.Publication
import dips.communication.RequestMessages
import dips.communication.Routable
import dips.communication.Routing
import dips.communication.Subscription
import dips.communication.Uri
import dips.simulation.{DistributedSimulation => ds}
import dips.util.Logger.log
import dips.NotImplementedException


object DHT{
  RemoteActor.classLoader = classOf[java.io.ObjectInputStream].getClassLoader() //getClass().getClassLoader()
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
  
  var indexed_instances = instances.toIndexedSeq

  
  def this(){ this(DHT.DEFAULT_PORT) }
  

  
  /**
   * Chord like routing behavior
   * @return resulting instance 
   */
  def route(r:Routable) = {
    //val n = instances find (r.hash < _.hash )
    //val result = (n getOrElse instances.first)
    //log debug instances.size
    //instances.toIndexedSeq((r.hash.abs % instances.size).toInt)
    indexed_instances( (r.hash.abs % instances.size).toInt )
  }
  
  def local(r:Routable) = {
    val instance = route(r)
    instance.hash == local_addr.hash
  }
  
  def send(r:Routable){
    //log.debug("Sending value " + msg + " to " + (this route msg))
    (this route r) ! r
  }
  
  def send(msg:Message) {
    //log.debug("Sending message " + msg + " to " + (this route msg.destination_node_id))
    val destination = this route msg.destination_node_id
    
    if(destination.hash == local_addr.hash){
      messages enqueue msg
    }
    else{
      msg.local = false
      destination ! msg
    }
  }
  
  /**
   * Send message directly to an instance,
   * override routing
   */
  def send_to(i:Instance, msg:Any) {
    //log.debug("Sending message " + msg)
    i ! msg
  }
  
  /**
   * Send message to all instances
   */
  def broadcast(msg:Any) {
    //log.debug("Sending broadcast and waiting ack")
    instances.map(translate(_) ! msg)//.forall(_().asInstanceOf[Boolean])
  }
  
  def request_messages(){
    instances.withFilter(_.hash != local_addr.hash).foreach{
      _.actor ! RequestMessages(local_addr)
    }
  }
  
  /**
   * Take message from message queue
   */
  /*def get_messages() = {
    /*while(this.messages.size == 0){
      log.debug("Waiting new messages")
      Thread.sleep(10000)
    }*/
    
    log.debug("Updated message queue with " + this.messages.size + " remote messages")
    (this !? Retrieve).asInstanceOf[ListBuffer[Message]]
  }*/
  
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
    val initial_time = ds.networkTimeMilis
    val info = instance !? Connect(local_addr, initial_time)
    info match{
      case Tuple2(s:HashSet[Uri], delay:Long) =>
        val clockDelay = delay + (ds.networkTimeMilis-initial_time)/2
        ds setClockDelay clockDelay
        log.debug("Clock delay set to " + clockDelay + " ms")
        log.debug("Set response: " + s)
        s map { new Instance(_) } foreach { i =>
          log.debug("Instance: " + i)
          if(!instances.contains(i)){
              log.debug("Sending Anounce")
        	  i ! Anounce(local_addr)
        	  instances += i
        	  indexed_instances = instances.toIndexedSeq
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
    //log.debug("routing event: " + event)
    
    event match{
      case Connect(uri:Uri, instance_time) => 
        val instance = new Instance(uri)
        
        if(!(instances contains instance)){
          instances += instance
          indexed_instances = instances.toIndexedSeq
          this ! Publication('coordinate, Connected(uri))
        }
        reply(this.uriSet, ds.networkTimeMilis - instance_time)
      case Anounce(uri:Uri) =>
        val instance = new Instance(uri)
        if(!(instances contains instance)){
          instances += instance
          indexed_instances = instances.toIndexedSeq
          this ! Publication('coordinate, Connected(uri))
          log.debug("Instances updated: (" + instances.size + ") with " + instance)
        }
      case RequestMessages(uri:Uri) =>
        val i = instances.find(_.hash == uri.hash).get
        i.flush()
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