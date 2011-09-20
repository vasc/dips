package dips.communication
import scala.actors.Actor
import scala.actors.AbstractActor
import dips.communication.dht.Instance
import scala.actors.OutputChannel

trait Routing


case class Connect(uri:Uri) extends Routing
case object Disconnect extends Routing
case class Anounce(uri:Uri) extends Routing

trait Router extends Actor{
  def connect(uri:Uri)
  def disconnect()
  def translate(instance:Instance):AbstractActor
  protected def routing_event(event:Routing)
}