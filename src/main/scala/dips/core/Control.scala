package dips.core
import peersim.core.Control
import peersim.core.Scheduler

trait DistributedControl extends Control

case class ScheduledControl(control:Control, name:String, scheduler:Scheduler)