package dips.example.infection
import scala.collection.mutable.StringBuilder

import dips.core.DistributedControl
import dips.simulation.DistributedSimulation
import dips.stats.Registry
import dips.stats.StatsControl
import dips.util.Logger.log
import peersim.config.Configuration
import peersim.vector.SingleValue

class InfectionObserver(val prefix:String) extends DistributedControl{
  private val PAR_PROT = "protocol";
  private val PAR_MAX = "max";
  private val PAR_SLOTS = "slots";
  val pid = Configuration.getPid(prefix + "." + PAR_PROT);
  val max = Configuration.getInt(prefix + "." + PAR_MAX);
  val slots = Configuration.getInt(prefix + "." + PAR_SLOTS);
  
  var saved = false
  
  
  def execute() = {
    if(!saved){
      Registry.get[StatsControl]("performance").get.save("infection.degree", Configuration.getInt(prefix + ".degree"))
      saved = true
    }
    
    val values = Array.fill(slots+1){0}
    val net_size = DistributedSimulation.network.size
    
    for(node <- DistributedSimulation.network.nodes){
      val protocol = node.getProtocol(pid).asInstanceOf[SingleValue]
      val infection = protocol.getValue().toInt
      
      //log.debug(infection + "/ (" + max + "/" + slots +")" )
      values(infection/(max/slots)) += 1
    }
    
    val msg = new StringBuilder("Infection propagation: ") 
    for(i <- 0 until slots){
      msg.append( "%.2f".format(values(i)*100.0/net_size))
      msg append "%%, "
    }
    msg.append("%.2f".format(values(slots) * 100.0/net_size))
    msg append "% dead"
    
    log.info(msg)
    false
  }
}