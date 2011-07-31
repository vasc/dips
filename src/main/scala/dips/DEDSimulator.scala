package dips

import dips.communication.{Uri, Message}

import peersim.config.Configuration
import peersim.core.{Network, Control, Protocol, CommonState, Scheduler}

import scala.collection.mutable.{Queue, LinkedList}

trait DEDProtocol extends Protocol{
  def processEvent(dest:Int, src:Int, pid:Int, event:AnyRef)
}

trait DistributedControl extends Control

case class ScheduledControl(control:Control, name:String, scheduler:Scheduler)
case class MessageWrapper(msg:AnyRef, sender:Int, destination:Int, pid:Int) extends Message

object DEDSimulator {
  val PAR_DIST = "distributed"
  //
  var controls = new LinkedList[ScheduledControl]
  val controller = Configuration.getInstance(PAR_DIST+".controller").asInstanceOf[DistributionController]
  
  if(Configuration.contains(PAR_DIST+".connection.host")){
    val host = Configuration.getString(PAR_DIST+".connection.host")
    val port = Configuration.getInt(PAR_DIST+".connection.port")
    controller.make_connection(Uri(host, port, 'point))
  }
  
  def full_network_size:Int = { controller.size }
  //private var local_network_size:Int = _
  //private var local_min:Int = _
  
  
  def isConfigurationDistributed():Boolean = {
	Configuration contains PAR_DIST
  }
  
  def newExperiment(uri:Uri):Unit = {
    controller.make_connection(uri)
    newExperiment
  }
  
  def newExperiment:Unit = {
    configureDistributedExperiment
    
    Network.reset
    CommonState.setTime(0)
    runInitializers
    loadControls
    
    while(processNextMessage){}
    controller.kill
    println("Distributed simulation finished.")
  }
  
  def processNextMessage:Boolean = {
    if (!( controls filter { _.scheduler.active } forall { !_.control.execute } ) ) return false
    controller.dequeue match{
      case Some(msg) =>
        CommonState.setTime(CommonState.getTime + 1)
	    Network.get(msg.destination).getProtocol(msg.pid).asInstanceOf[DEDProtocol]
		    .processEvent(
		        msg.destination,
		        msg.sender,
		        msg.pid,
		        msg.msg)
		true
      case None =>
        if(!controller.finished){
	        println("Event queue is empty, awaiting external events.")
	        Thread.sleep(2000)
	        true
        }
        else{
          false
        }
    }
  }
  
  private def runInitializers = {
    val inits = Configuration.getInstanceArray("init");
    val names = Configuration.getNames("init");
    
    inits.zip(names) foreach { 
      case(init:Control, name) =>
        System.err.println("- Running initializer " + name + ": " + init.getClass)
        init.execute
    }
  }
  
  private def loadControls = {
    val controls = Configuration.getInstanceArray("control");
    val names = Configuration.getNames("control");
    
    this.controls ++= controls.zip(names) map { 
      case(control:Control, name) =>
        System.err.println("- Loading control " + name + ": " + control.getClass)
        val shed = new Scheduler(name)
        new ScheduledControl(control, name, shed)
    }
  }
  
  private def configureDistributedExperiment = {
    controller init Configuration.getInt("distributed.size")
    Configuration.setProperty(Network.PAR_SIZE, controller.local_size.toString)
  }
  
  def sendMessage(srcId:Int, destId:Int, pid:Int, event:AnyRef) = {
    controller.send(MessageWrapper(event, srcId, destId, pid))
  }
  
}