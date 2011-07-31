package utils.communication.datagram;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {
	private DatagramSocket dsocket;

	public Client(String hostname) {
		try {
			dsocket = new DatagramSocket(0, InetAddress.getByName(hostname));
			dsocket.setBroadcast(true);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void sendIp(String hostname, int dport) {
		byte packet[] =	dsocket.getInetAddress().getAddress();
		DatagramPacket p = new DatagramPacket(packet, packet.length);
		try {
			p.setSocketAddress(new InetSocketAddress(InetAddress.getByName(hostname), dport));
			dsocket.send(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
