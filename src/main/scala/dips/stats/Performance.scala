package dips.stats
import peersim.core.CommonState
import dips.simulation.DistributedSimulation
import peersim.config.Configuration
import dips.util.Logger.log

class Performance(prefix:String) extends StatsControl(prefix) with Sub {
	Registry.register("performance", this)
  
	val initial_time = DistributedSimulation.networkTimeMilis
    
	val average_delay = new AnyRef{
	  var delay_local = 0L
	  var delay_remote = 0L
	  
	  var remote_count = 0
	  var local_count = 0
	  
	  def apply() = { 
	    if(remote_count == 0 && local_count == 0) 0
	    else (delay_remote+delay_local)/(remote_count+local_count) 
	  }
	  def remote = { 
	    if(remote_count == 0) 0
	    else delay_remote/remote_count
	  }
	  def local = { 
	    if(local_count == 0) 0
	    else delay_local/local_count
	  }
	  def add_delay(d:Long, local:Boolean){
	    if(local){ delay_local += d; local_count += 1 }
	    else { delay_remote += d; remote_count += 1 }
	  }
	}
	
	override def execute() ={
	  val final_time = DistributedSimulation.networkTimeMilis
	  val elapsed_time = final_time - initial_time
	  val processed_events = CommonState.getTime
	  
	  save("initial.time", initial_time)
	  save("final.time", final_time)
	  save("processed.events", processed_events)
	  save("idle.time", DistributedSimulation.idle_time)
	  save("average.delay", average_delay())
	  
	  save("processed.events.remote", average_delay.remote_count)
	  save("processed.events.local", average_delay.local_count)
	  
	  save("average.delay.remote", average_delay.remote)
	  save("average.delay.local", average_delay.local)
	  
	  //save("nodes.count", DistributedSimulation.network.size)
	  //save("bundle.size", Configuration.getString("distributed.bundle.size"))
	  
	  save("routing.method", "round.robin")
	  
	  log.debug("processed.events.per.second: " + processed_events / ( (final_time - initial_time) / 1000.0 ))
	  
	  //log debug ("Node get stats: " + DistributedSimulation.network.is)
	  false
	}
	
	def receive(key:Symbol, value:Any){
	  value match{
	    case Tuple2(delay:Long, local:Boolean) if key == 'delay =>
	      average_delay add_delay (delay, local)
	  }
	}
} 