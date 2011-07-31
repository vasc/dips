package utils.communication.datagram;


public class ClientMain {
	public static void main(String[] args) {
		Client c = new Client(args[0]);
		c.sendIp(args[1], Integer.parseInt(args[2]));
	}
}
