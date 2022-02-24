package org.distrib.patterns.heartbeat;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractFailureDetector<T> {
    private Map<T, ServerState> serverStates = new HashMap<T, ServerState>();
    public boolean isAlive(T serverId) {
        return serverStates.get(serverId) == ServerState.UP;
    }
    public void markUp(T serverId) {
        serverStates.put(serverId, ServerState.UP);
    }

    public void markDown(T serverId) {
        serverStates.put(serverId, ServerState.DOWN);
    }

    //<codeFragment name="failureDetectorScheduler">
    private HeartBeatScheduler heartbeatScheduler = new HeartBeatScheduler(this::heartBeatCheck, 100l);

    abstract void heartBeatCheck();
    abstract void heartBeatReceived(T serverId);
    //</codeFragment>

    public void start() {
        heartbeatScheduler.start();
    }

    public void stop() {
        heartbeatScheduler.stop();
    }


    public <T> boolean isMonitoring(T address) {
        return serverStates.containsKey(address);
    }

    public void remove(T address) {
        serverStates.remove(address);
    }
}
