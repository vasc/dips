package utils.communication.datagram;

public class ServerMain {
	public static void main(String[] args) throws InterruptedException {
		Server s = new Server(Integer.parseInt(args[1]), args[0]);
		Thread t = new Thread(s);
		
		t.run();
		t.join();
	}
}
