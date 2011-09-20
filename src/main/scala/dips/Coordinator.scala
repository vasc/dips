package dips
import scala.actors.Actor._
import scala.actors.remote.Node
import scala.actors.remote.RemoteActor
import scala.actors.Actor
import scala.actors.Exit
import dips.communication.dht.DHT
import dips.communication.Publication
import dips.communication.Subscription
import dips.util.Logger.log
import dips.util.File
import scopt.OptionParser
import scala.actors.AbstractActor
import dips.communication.Ack
import scala.actors.UncaughtException


//TODO: Coordinator

sealed trait Coordination

case class GetToken extends Coordination 
case object DeliverToken extends Coordination
case class StartSimulation(config:String) extends Coordination
case class SimulationDefinition(config:String) extends Coordination

class Coordinator(val dht:DHT) extends Actor{
  var has_token = false
  log.debug("running Coordinator")
  dht.subscribe('coordinate, this)
  this.start()
  dht.start()
    
  def start_simulation(config:String) {
    log.debug("starting simulation")
          
	link(body = {
      log.debug("Started simulation actor: " + this)
	  
      
	  Dips.parse_configuration(Array[String](config))

	  Dips.load_simulation()
	  //throw new Exception("teste")
	})
  }
  
  /*override def exit(from: AbstractActor, reason: AnyRef){
    log.debug("exiting " + this + ": " + reason)
  }*/
  
  def act{
    trapExit = true
    log.debug("running Coordinator act method")
    while(true){
      receive {
        case Subscription(name, GetToken, publisher) =>
          log.debug("message: GetToken")
          publisher ! DeliverToken
          log.debug("replied: DeliverToken to " + publisher)
          
        case Subscription('coordinate, StartSimulation(config), publisher) =>
          log.debug("message: StartSimulation")
          publisher ! true
          this start_simulation config
          
        case Subscription(name, SimulationDefinition(config), publisher) => 
          log.debug("message: SimulationDefinition")
          dht.broadcast(Publication('coordinate, StartSimulation(config)))
          //this start_simulation config
          
        case Subscription('coordinate, DeliverToken, output) => log.debug("message: DeliverToken")
        
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