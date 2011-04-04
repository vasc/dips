package dips.orchestration;

import java.util.List;

import dips.communication.*;

public class ServerOrchestrator extends Orchestrator{
    private Server server;
	
	public ServerOrchestrator(int port){
        this.server = new Server(port);
        this.server.start();
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


    
