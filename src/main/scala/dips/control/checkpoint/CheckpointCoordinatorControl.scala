package dips.control.checkpoint
import scala.actors.OutputChannel
import scala.annotation.serializable
import scala.collection.mutable.HashMap

import dips.communication.Uri
import dips.core.ControlMessage
import dips.core.NetworkControl
import dips.simulation.DistributedSimulation
import dips.util.Logger.log

case object MakeCheckpoint
case class CheckpointDone(uri:Uri)

class CheckpointCoordinatorControl(prefix:String) extends NetworkControl {
  private var checkpoint_done:HashMap[Uri, Boolean] = _
  
  def execute() = { 
    if(DistributedSimulation.isCoordinator){
      DistributedSimulation.requestSynchronizedState()
      checkpoint_done = new HashMap[Uri, Boolean]()
      
      for(i <- DistributedSimulation.dht.instances){
        checkpoint_done(i.uri) = false
      }
      
      log debug "Broadcasting MakeCheckpoint"
      DistributedSimulation.dht broadcast ControlMessage("control.checkpoint", MakeCheckpoint)
      //DistributedSimulation.dht broadcast new ControlMessage("control.cc", 'test)
    }
    false
  }
  
  def receive_message(cm:ControlMessage, sender:OutputChannel[Any]) = {
    cm.msg match {
      case CheckpointDone(uri) => 
        checkpoint_done(uri) = true
        if(checkpoint_done.values.forall{ _ == true })
          DistributedSimulation.releaseSynchronizedState()
    }
    
    false
  }
}