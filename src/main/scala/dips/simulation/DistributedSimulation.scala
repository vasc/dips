package dips.simulation
import dips.core.DistributedNetwork
import peersim.core.Simulation
import dips.communication.dht.DHT

object DistributedSimulation {
  def network = Simulation.network.asInstanceOf[DistributedNetwork]
  var dht:DHT = _
  var isCoordinator = false
}