package dips.core
import peersim.core.Control
import peersim.core.Scheduler
import dips.communication.Communication

case class ControlMessage(val name:String, val msg:Any) extends Communication

trait DistributedControl extends Control

trait NetworkControl extends DistributedControl{
  def receive_message(cm:ControlMessage):Boolean
}

case class ScheduledControl(control:Control, name:String, scheduler:Scheduler)