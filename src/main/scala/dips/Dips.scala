package dips

import peersim.Simulator
import peersim.config.Configuration
import scopt.OptionParser
import java.lang.Runtime
import dips.util.Logger.log
import dips.communication.dht.DHT
import dips.communication.Addressable
import dips.simulation.DEDSimulator
import dips.simulation.DistributedSimulation

object Dips extends Simulator {
  val DEDSIM = 2
  var initial_mem = Runtime.getRuntime.totalMemory 
  var dht:DHT = _
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
      DEDSimulator.dht = dht
      DEDSimulator.newExperiment
    }
    else{
      super.runExperiment(SIMID);
    }
  }
  
  def main(args: Array[String]){ 
    
    
    Simulator.setSimulator(this)
    
    var remote_port:Int = 0
    var remote_host:String = ""
    var local_port:Int = DHT.DEFAULT_PORT
      
    val parser = new OptionParser("dips"){
      opt("p", "port", "the remote port", {p => remote_port = p.toInt})
      opt("h", "host", "the remote host", {h => remote_host = h})
      opt("l", "localport", "the local port", {lp => local_port = lp.toInt})
      //arg("<configfile>", "configuration file for the simulation", {v => newargs(0) = v})
    }
    
    if(!parser.parse(args)){ return }
    
    /*
    this.parse_configuration(newargs)
    
    if (remote_host != "" && port != "") { 
      Configuration.setProperty("distributed.connection.host", remote_host)
      Configuration.setProperty("distributed.connection.port", port)
    }
    
    this.load_simulation()
    */
    log.debug("arguments parsed: " + (remote_host, remote_port, local_port) )
    
    dht = new DHT(local_port)
    DistributedSimulation.dht = dht
    
    if (remote_host != "" && remote_port != 0) { 
      log.debug("connecting to " + remote_host + ", " + remote_port )
      dht.connect(new Addressable(){
        val ip = remote_host
        val port = remote_port
      })
    }
    
    new Coordinator(dht)
  }
}
