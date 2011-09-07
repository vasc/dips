package dips.communication

import scala.actors.AbstractActor

class DHT extends PostOffice {

  def connect(uri: Any) {  }

  def disconnect() {  }

  def translate(dest: Any): AbstractActor = { null }

  def routing_event(event: Routing) {  }

}