package dips.communication;

import java.util.concurrent.Semaphore;

public class Server extends Thread{
    private Semaphore sem;
    private Mina.Server server;
    
    public Semaphore getSem(){
        return this.sem;
    }
    
    public Server(int port){
        this.sem = new Semaphore(0);
        this.server = new Mina.Server();
    }
    
    @Override
    public void run(){
        this.server.init(this.port);
    }
}