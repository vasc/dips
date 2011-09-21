package dips.communication

import java.net.InetAddress

import scala.actors.AbstractActor
import scala.actors.Actor
import scala.annotation.serializable
import scala.collection.mutable.HashMap


trait Communication
case object Exit extends Communication
case object Retrieve extends Communication
case object Ack extends Communication

case class Envelope[T](msg:T, origin:Uri) extends Communication

object Envelope{
  implicit def envelope2routable(env:Envelope[Routable]) = env.msg
}










