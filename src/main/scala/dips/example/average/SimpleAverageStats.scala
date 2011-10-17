package dips.example.average


import _root_.peersim.core.{Control, Simulation}
import dips.stats.Stats
import _root_.peersim.util.IncrementalStats
import _root_.peersim.vector.SingleValue
import _root_.peersim.config.Configuration
import _root_.peersim.core.CommonState
import scala.collection.mutable.HashMap

class SimpleAverageStats(override val prefix:String) extends Stats with Control {
  lazy val pid = Configuration.getPid(prefix+".protocol")
  
  var first = true
  
  val received = new HashMap[Int, Double]
  
  override def execute() = {
	if(first){
	  first = false
	  clear("variance")
	}
    val is = new IncrementalStats();
    for (val i <- Range(0, Simulation.network.size)) {
        val protocol = Simulation.network.get(i).getProtocol(pid).asInstanceOf[SingleValue];
        is.add(protocol.getValue());
        
        
        
    }
    add("variance", is.getVar)
    System.out.println(prefix + ": " + CommonState.getTime + " " + is);
    false
  }

}