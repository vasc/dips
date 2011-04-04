package dips.orchestration;

import java.util.List;

public abstract class Orchestrator{
    public abstract void sendMessage(String message);
    public abstract List<String> getMessages();
    
    public abstract void nextCycle();
}