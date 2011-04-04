package dips.orchestration;

import java.net.InetAddress;
import java.util.List;

import dips.communication.*;

public class ClientOrchestrator extends Orchestrator{
	private Client client;
	
    public ClientOrchestrator(InetAddress ip, int port){
        this.client = new Client(ip, port);
    }
    
    @Override
    public void sendMessage(String Message){
        
    }
    
    @Override
    public List<String> getMessages(){
		return null;
    }
    
    @Override
    public void nextCycle(){
        
    }
}







