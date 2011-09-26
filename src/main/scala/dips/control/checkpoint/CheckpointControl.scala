package dips.control.checkpoint
import java.io.FileOutputStream
import java.io.ObjectOutputStream

import scala.actors.OutputChannel

import dips.core.ControlMessage
import dips.core.NetworkControl
import dips.simulation.DEDSimulator
import dips.simulation.DistributedSimulation
import dips.util.Logger.log
import peersim.config.Configuration
import peersim.core.CommonState

class CheckpointControl(prefix:String) extends NetworkControl {
  def execute() = { false }
  
  def receive_message(cm:ControlMessage, sender:OutputChannel[Any]) = {
    cm.msg match{
      case MakeCheckpoint => 
        log debug "Creating Checkpoint"
        val checkpoint = new Checkpoint(
          DistributedSimulation.dht.messages.toList,
          DistributedSimulation.dht.instances.toList.map{ _.mb.messages }.flatten,
          DistributedSimulation.network.nodes.toList,
          DEDSimulator.controls.map{ _.control }.toList,
          CommonState.getTime,
          Configuration getString "simulation.name",
          Configuration getLong CommonState.PAR_SEED
      )
          
	    val name = checkpoint.name + "[" + checkpoint.seed + "]" + "." + checkpoint.time + ".dips"
	    val os = new ObjectOutputStream(new FileOutputStream(name))
	    os.writeObject(checkpoint)
	    os.close()
	    
	    val coordinator = DistributedSimulation.dht.instances.find { DistributedSimulation.coordinator_uri.hash == _.hash }.get
	    
	    DistributedSimulation.dht.send_to(
	        coordinator, 
	        ControlMessage("control.checkpointcoordinator", CheckpointDone(DistributedSimulation.dht.local_addr)))
	  }
    false
  }
}