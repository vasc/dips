package dips
import scala.actors.Actor.exit
import scala.actors.remote.Node
import scala.actors.remote.RemoteActor
import scala.actors.Actor
import scala.actors.Exit
import scala.actors.UncaughtException
import scala.annotation.serializable
import scala.collection.mutable.HashMap
import dips.communication.dht.DHT
import dips.communication.Publication
import dips.communication.Subscription
import dips.communication.Uri
import dips.simulation.DistributedSimulation
import dips.util.Logger.log
import dips.util.File
import scopt.OptionParser
import dips.communication.Connected

sealed trait Coordination

case class GetToken extends Coordination 
case object DeliverToken extends Coordination
case class StartSimulation(uri:Uri, config:String) extends Coordination
case class SimulationDefinition(config:String) extends Coordination
case class EnterSimulation(uri:Uri, config:String) extends Coordination

case object StartSync extends Coordination
case class TotalMessages(count: Int) extends Coordination
case object SyncReady extends Coordination
case object Resume extends Coordination

class Coordinator(val dht:DHT) extends Actor{
  var has_token = false
  log.debug("running Coordinator")
  dht.subscribe('coordinate, this)
  this.start()
  dht.start()
    
  def start_simulation(coordinator:Uri, config:String) {
    log.debug("Starting Simulation")
    DistributedSimulation.new_simulation(coordinator, config)      
    
	link(body = {
	  Dips parse_configuration Array[String](config)
	  Dips load_simulation 
	})
  }
  
  def enter_simulation(coordinator:Uri, config:String){
    log debug "Entering Simulation"
    DistributedSimulation.new_simulation(coordinator, config)
    DistributedSimulation.simulation.status = 'running
    
    link(body = {
      Dips parse_configuration Array[String](config)
      Dips load_simulation
    })
    
  }

  def coordinator_start_sync() {
    val count_per_instance = dht.instances.map { i =>
      i !! Publication('coordinate, StartSync)
    }
    
    log debug "StartSync: Getting counts"
    
    val counts = new HashMap[Uri, Int]()
    
    for(instance_counts <- count_per_instance.map { _().asInstanceOf[Map[Uri, Int]] }){
      for((uri, count) <- instance_counts){
        if(!(counts contains uri)) counts(uri) = 0
        counts(uri) += count
      }
    }
    
    log debug "StartSync: Counts " + counts  
    
    val acks = dht.instances.map { i =>
      i !! Publication('coordinate, TotalMessages(counts(i)))
    }
    
    acks.map { _() }
    
    log debug "StartSync: Sync is ready"
  }
  
  def coordinator_stop_sync() {
    dht.instances.foreach {
      i => i ! Publication('coordinate, Resume)
    }
    log debug "Sync released"
  }

  def coordinator_handle_new_connection(new_connection:Uri) {
    if(DistributedSimulation.simulation != null
       && DistributedSimulation.isCoordinator){
      
      dht send_to (new_connection, Publication('coordinate, EnterSimulation(dht.local_addr, DistributedSimulation.config)))
    }
  }
  
  private def start_sync(): Map[Uri,Int] = {
    log debug "SyncState: Blocking main loop execution" 
    DistributedSimulation.simulation.synchronized{
      DistributedSimulation.simulation.paused = true
    }
    
    val counts = new HashMap[Uri, Int]
    
    for(i <- dht.instances){
      counts(i.uri) = i.sent_messages_count
    }
    log debug "StartSync: Local counts " + counts
    counts.toMap
  }
  
  def act{
    trapExit = true
    log.debug("running Coordinator act method")
    while(true){
      receive {
        case Subscription('coordinate, msg, publisher) =>
          msg match{
            
            case GetToken =>
              publisher ! DeliverToken
              log.debug("replied: DeliverToken to " + publisher)
            case DeliverToken => 
              has_token = true
            
            case StartSimulation(coordinator, config) =>
              //publisher ! true
              this start_simulation (coordinator, config)
            case EnterSimulation(coordinator, config) =>
              //publisher ! true
              this enter_simulation (coordinator, config)
            case SimulationDefinition(config) =>
              has_token = true
              dht.broadcast(Publication('coordinate, StartSimulation(dht.local_addr, config)))

            case Connected(uri) =>
              log debug "Coordinator: " + uri + " Connected"
              coordinator_handle_new_connection(uri)
              
            case StartSync =>
              val counts = start_sync()
              publisher ! counts
            case TotalMessages(count) =>
              while(count != dht.received_messages_count){
                log debug "StartSync: testing message counts " + count + ", " + dht.received_messages_count
                Thread.sleep(100)
              }
              publisher ! SyncReady
            case Resume =>
              if(DistributedSimulation.simulation != null){
                log debug "Resuming simulation"
                DistributedSimulation.simulation.synchronized{
                  DistributedSimulation.simulation.paused = false
                  DistributedSimulation.simulation.notify()
                }
              }
                
          }       
          
        case Exit(simulation, reason) =>
        	reason match{
        	  case UncaughtException(actor, message, sender, thread, cause) =>
        	    cause.printStackTrace()
        	}
          
          log.debug("message: Simulation has ended back to coordinator(" + this + "), reason: " + reason)
          
        case a:Any =>
          log.debug(a)
      }
    }
    
    /*override def exit(from: AbstractActor, reason: AnyRef){
    log.debug("exiting " + this + ": " + reason)
    }*/
  }
}

object Coordinator extends App{
  //Debug.level = 9
  RemoteActor.classLoader = getClass().getClassLoader()
  var remote_port = DHT.DEFAULT_PORT
  var remote_host = "localhost"
  var config_file = ""
  
  val parser = new OptionParser("dips"){
    opt("p", "port", "the remote port", {p => remote_port = p.toInt})
    opt("h", "host", "the remote host", {h => remote_host = h})
    arg("<configfile>", "configuration file for the simulation", {f => config_file = f})
  }
    
  if(parser.parse(args)){
    val config = File.read(config_file)
    log.debug("Config file:\n\n" + config)
    val actor = RemoteActor.select(Node(remote_host, remote_port), DHT.DEFAULT_SERVICE)
    log.debug("Started remote actor: " + actor)
    
    val producer = Actor.actor{  
    	actor ! Publication('coordinate, SimulationDefinition(config))
    	/*
    	Actor.receive { 
    	  case DeliverToken => log.debug("response: Deliver token")
    	  case msg:Any => log.debug("response: " + msg)
    	}
    	*/
    	Thread.sleep(10000)
    	exit()
    }
  }
}