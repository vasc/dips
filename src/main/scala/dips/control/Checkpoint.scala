package dips.control

import dips.core.ControlMessage
import dips.core.NetworkControl
import scala.collection.mutable.Queue
import dips.communication.Message
import dips.communication.dht.MessageBundle
import dips.communication.Uri
import peersim.core.Node
import dips.core.ScheduledControl

case class Checkpoint(
    val incoming:Queue[Message],
    val outgoing:List[Message],
    val nodes:Map[Int, Node],
    val controls:List[ScheduledControl],
    val time:Long
)

class CheckpointControl extends NetworkControl {
  def execute() = { false }
  def receive_message(cm:ControlMessage) = { false }
  
  def checkpoint() = {
    
  }
}