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
import dips.core.NetworkControl


object DEDSimulator {
  val PAR_DIST = "distributed"
  var dht:DHT = _
  var controls = new LinkedList[ScheduledControl]
  
  def isConfigurationDistributed():Boolean = {
	Configuration contains PAR_DIST
  }
  
  def newExperiment(uri:Addressable):Unit = {
    dht.connect(uri)
    newExperiment
  }
  
  def newExperiment:Unit = {
    
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
  
  def runNetworkControllers() = {
    while(! dht.control_messages.isEmpty ){
      val cm = dht.control_messages.dequeue()
      val control = controls.find( _.name == cm.name ).asInstanceOf[NetworkControl]
      control receive_message cm
    }
    false
  }
  
  def runScheduledControlers() = {
    ( controls filter { _.scheduler.active } exists { _.control.execute } ) 
  }
  
  /**
   * Main simulation cycle
   * 
   * Executes all control objects scheduled and then processes the next message in the queue
   */
  def processNextMessage:Boolean = {
    if(CommonState.getTime % 5000 == 0){
      log.debug("Processing message at " + CommonState.getTime)
    }
    
    if ( runNetworkControllers() ) return false
    
    if ( runScheduledControlers() ) return false
    
    if(dht.messages.isEmpty){
      log.debug("Waiting for messages...")
      dht.messages.synchronized{
        dht.messages.wait()
      }
    }
    
    val msg = dht.messages.dequeue
    CommonState.setTime(CommonState.getTime + 1)
    DistributedSimulation.network.get(msg.destination_node_id).getProtocol(msg.pid).asInstanceOf[DEDProtocol]
		    .processEvent(
		        msg.destination_node_id,
		        msg.origin_node_id,
		        msg.pid,
		        msg.msg)
	true
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
  
  def sendMessage(destId:Long, srcId:Long, protocolId:Int, event:Any) = {
    val message = new Message(destId, srcId, protocolId, event)
    
    if(dht local message.destination_node_id){
      //log.debug("Message " + message + " to local node " + message.destination_node_id + " routed to " + (dht route message.destination_node_id))
      dht.messages enqueue message
    }
    else{
      //log.debug("Sending message " + message + " to remote node " + message.destination_node_id + " at " + (dht route message.destination_node_id))
      dht send message
    }
  }
  
}