package dips

import peersim.Simulator
import peersim.config.Configuration
import scopt.OptionParser

object Dips extends Simulator {
  val DEDSIM = 2
  simName = simName :+ "dips.dedsim.DEDSimulator"
  
  override def getSimID():Int = {
    if(DEDSimulator.isConfigurationDistributed()){
      DEDSIM
    }
    else{
      super.getSimID()
    }
  }
  
  override def runExperiment(SIMID:Int) = {
    if(SIMID == DEDSIM){
      DEDSimulator.newExperiment
    }
    else{
      super.runExperiment(SIMID);
    }
  }
  
  def main(args: Array[String]): Unit = { 
    Simulator.setSimulator(this)
    
    var port:String = ""
    var remote_host:String = ""
    var newargs = new Array[String](1)
      
    val parser = new OptionParser("dips"){
      opt("p", "port", "the remote port", {p => port = p})
      opt("h", "host", "the remote host", {h => remote_host = h})
      arg("<configfile>", "configuration file for the simulation", {v => newargs(0) = v})
    }
    
    if(!parser.parse(args)){ return }
    
    this.parse_configuration(newargs)
    
    if (remote_host != "" && port != "") { 
      Configuration.setProperty("distributed.connection.host", remote_host)
      Configuration.setProperty("distributed.connection.port", port)
    }
    
    this.load_simulation()
  }
}
