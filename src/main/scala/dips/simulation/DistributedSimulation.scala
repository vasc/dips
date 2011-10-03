package dips.simulation
import dips.core.DistributedNetwork
import peersim.core.Simulation
import dips.communication.dht.DHT
import dips.NotImplementedException
import dips.Coordinator
import scala.concurrent.Lock
import dips.communication.Uri

case class SimulationSignal(var paused:Boolean, var status:Symbol)

object DistributedSimulation {
  private var _simulation:SimulationSignal = _
  def simulation = _simulation
  
  private var _coordinator_uri:Uri = _
  def coordinator_uri = _coordinator_uri
  
  private var _config:String = _
  def config = _config
  
  private var _migrator:Migrator = _
  def migrator = _migrator
  
  private var _idle_time = 0L
  private var _start_idle = 0L
  def idle_time = _idle_time
  
  def start_idle() { _start_idle = System.nanoTime }
  def end_idle() { _idle_time += System.nanoTime - _start_idle }
  
  def new_simulation(cu:Uri, config:String){
    _simulation = new SimulationSignal(false, 'init)
    _migrator = new Migrator(dht, dht.messages)
    _coordinator_uri = cu
    _config = config
  }
  
  def network = Simulation.network.asInstanceOf[DistributedNetwork]
  var dht:DHT = _
  
  private var coordinator:Coordinator = _
  def setCoordinator(c:Coordinator) = coordinator = c
  
  def isCoordinator = coordinator.has_token
  
  private var synchronized = false
  def isSynchronized = synchronized
  
  def requestSynchronizedState() = {
    coordinator.coordinator_start_sync()
  }
  
  def releaseSynchronizedState() = {
    coordinator.coordinator_stop_sync()
  }
  
  def migrate() {
    
  }
  
  def migration(m:Migration) {
    
  }
}