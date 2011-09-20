package dips.communication

/*
import scala.actors.remote.RemoteActor._
import scala.actors.{ Actor, AbstractActor }
import scala.Symbol
import java.net.{InetAddress, UnknownHostException}
import scopt.OptionParser
import scala.actors.remote.TcpService
import scala.actors.remote.RemoteActor
import scala.actors.remote.Node

class Point2Point extends PostOffice {
  RemoteActor.classLoader = getClass().getClassLoader()
  
  var remote: AbstractActor = _
  var connected = false
  
  val service:Symbol = 'point
  
  val host = {
    try{ InetAddress.getLocalHost().getHostAddress() }
    catch { case e:UnknownHostException =>
      System.err.println("Unable to find host, using localhost");
      "localhost"
    }
  }
  
  val port = TcpService.generatePort
  alive(port)
  register('point, this)
  System.err.println("PostOffice listening on port: " + port)

  def connect(uri:Uri) = {
    if (!connected) {
      uri match {
        case uri: Uri => 
          this.remote = select(Node(uri.ip, uri.port), uri.service)
          println(uri)
      }
      this.remote ! Connect(Uri(host, port, service))
      connected = true
    }
  }

  def disconnect() = this.remote ! Disconnect
  def translate(dest: Any): AbstractActor = this.remote
  def routing_event(r: Routing) = {
    r match {
      case Connect(uri) =>
        this.synchronized{
	        if (!connected) {
	          this.remote = select(Node(uri.ip, uri.port), uri.service)
	          connected = true
	          println("Connected")
	        }
	        this.notify()
        }
      case Disconnect => connected = false
    }
  }
}

object Point2Point extends App {
    var port = 2457
    var remote_host:String = "localhost"
    var remote_port = -1
    
    val parser = new OptionParser("dipscomm")
    				{
    					intOpt("p", "port", "the port to register the server", {p:Int => port = p})
    					opt("h", "host", "the remote host", {h:String => remote_host = h})
    					intOpt("r", "remoteport", "the remote port to connect", {r:Int => remote_port = r})	
    				}

    if (parser.parse(args)) {
      val p2p = new Point2Point()
      p2p.start()
      
      println(remote_port)
      
      if(remote_port > 0){
        p2p.connect(Uri(remote_host, remote_port, 'point))
      }
      //p2p ! Exit

      //val remote = select(Node("localhost", 2457), 'point)
      //remote ! Connect
    }
}
*/