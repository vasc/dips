package utils.communication.datagram;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Server implements Runnable {
	private DatagramSocket dsocket;
	private int port;
	private String hostname;

	public Server(int port, String hostname) {
		super();
		this.port = port;
		this.hostname = hostname;
	}

	@Override
	public void run() {
		try {
			dsocket = new DatagramSocket(port, InetAddress.getByName(hostname));
			dsocket.setBroadcast(true);
			while(true){
				byte[] packet = new byte[100];
				DatagramPacket p = new DatagramPacket(packet, packet.length);
				dsocket.receive(p);
				System.out.println(InetAddress.getByAddress(packet).toString());
			}
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
