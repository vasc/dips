package dips.communication;

import java.net.InetAddress;

public class Client{
    private Mina.Client client;
    
    public Client(InetAddress ip, int port){
        this.client = new Mina.Client(ip, port);
    }
    
    public void sendMessage(String message){
        if(!this.client.isConnected()) this.client.connect();
        
        this.client.send(message);
    }
}