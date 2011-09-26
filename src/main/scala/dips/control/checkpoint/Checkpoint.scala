package dips.control.checkpoint

import dips.core.ControlMessage
import dips.core.NetworkControl
import scala.collection.mutable.Queue
import dips.communication.Message
import dips.communication.dht.MessageBundle
import dips.communication.Uri
import peersim.core.Node
import dips.core.ScheduledControl
import dips.simulation.DistributedSimulation
import scala.actors.OutputChannel
import dips.util.Logger.log
import peersim.core.Control

case class Checkpoint(
    val incoming:List[Message],
    val outgoing:List[Message],
    val nodes:List[Node],
    val controls:List[Control],
    val time:Long,
    val name:String,
    val seed:Long
)

