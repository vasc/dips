package dips.simulation

import scala.collection.mutable.LinkedList
import dips.communication.dht.DHT
import dips.communication.Addressable
import dips.communication.Message
import dips.core.DEDProtocol
import dips.core.DistributedNetwork
import dips.core.ScheduledControl
import dips.util.Logger.log
import peersim.config.Configuration
import peersim.core.CommonState
import peersim.core.Control
import peersim.core.Scheduler
import peersim.core.Simulation
import dips.util.sha1
import scala.collection.mutable.Queue


object DEDSimulator {
  val PAR_DIST = "distributed"
  var dht:DHT = _
  var controls = new LinkedList[ScheduledControl]
  var queue:Queue[Message] = _
  //val controller = Configuration.getInstance(PAR_DIST+".controller").asInstanceOf[DistributionController]
  /*val dht = {  
	  if(Configuration.contains(PAR_DIST+".connection.host")){
	    val host = Configuration.getString(PAR_DIST+".connection.host")
	    val port = Configuration.getInt(PAR_DIST+".connection.port")
	    new DHT((host, port))
	  }
	  else{ new DHT }
  }*/
	  
  //def full_network_size:Int = { controller.size }
  //private var local_network_size:Int = _
  //private var local_min:Int = _
  
  
  def isConfigurationDistributed():Boolean = {
	Configuration contains PAR_DIST
  }
  
  def newExperiment(uri:Addressable):Unit = {
    dht.connect(uri)
    newExperiment
  }
  
  def newExperiment:Unit = {
    //configureDistributedExperiment
    queue = new Queue[Message]()
    
    log.debug("Starting new simulation")
    
    log.debug("Creating network")    
    Simulation.network = new DistributedNetwork(dht)
    
    log.debug("Reseting network")
    Simulation.network.reset()
    CommonState.setTime(0)
    
    log.debug("Running initializers")
    runInitializers()
    
    log.debug("Loading controls")
    loadControls()
    
    while(processNextMessage){}
    println("Distributed simulation finished.")
  }
  
  /**
   * Main simulation cycle
   * 
   * Executes all control objects scheduled and then processes the next message in the queue
   */
  def processNextMessage:Boolean = {
    if(CommonState.getTime % 1000 == 0){
      log.debug("Processing message at " + CommonState.getTime)
    }
    if (!( controls filter { _.scheduler.active } forall { !_.control.execute } ) ) return false
    
    if(queue.isEmpty){
      queue ++= dht.get_messages
    }
    
    queue.dequeue match{
      case msg:Message =>
        CommonState.setTime(CommonState.getTime + 1)
	    DistributedSimulation.network.get(msg.destination_node_id).getProtocol(msg.pid).asInstanceOf[DEDProtocol]
		    .processEvent(
		        msg.destination_node_id,
		        msg.origin_node_id,
		        msg.pid,
		        msg.msg)
		true
      /*case None =>
        if(!dht.finished){
	        println("Event queue is empty, awaiting external events.")
	        Thread.sleep(2000)
	        true
        }
        else{
          false
        }*/
    }
  }
  
  private def runInitializers() = {
    val inits = Configuration.getInstanceArray("init");
    val names = Configuration.getNames("init");
    
    inits.zip(names) foreach { 
      case(init:Control, name) =>
        System.err.println("- Running initializer " + name + ": " + init.getClass)
        init.execute
    }
  }
  
  private def loadControls() = {
    val controls = Configuration.getInstanceArray("control");
    val names = Configuration.getNames("control");
    
    this.controls ++= controls.zip(names) map { 
      case(control:Control, name) =>
        System.err.println("- Loading control " + name + ": " + control.getClass)
        val shed = new Scheduler(name)
        new ScheduledControl(control, name, shed)
    }
  }
  
  /*private def configureDistributedExperiment = {
    controller init Configuration.getInt("distributed.size")
    Configuration.setProperty(Network.PAR_SIZE, controller.local_size.toString)
  }*/
  
  def sendMessage(srcId:Long, destId:Long, protocolId:Int, event:AnyRef) = {
    val message = new Message(srcId, destId, protocolId, event)
    
    if(dht local message.destination_node_id){
      queue enqueue message
    }
    else{
      dht send message
    }
  }
  
}