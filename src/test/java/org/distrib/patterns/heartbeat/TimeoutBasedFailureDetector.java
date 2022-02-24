package org.distrib.patterns.heartbeat;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TimeoutBasedFailureDetector<T> extends AbstractFailureDetector<T> {
    private Map<T, Long> heartbeatReceivedTimes = new ConcurrentHashMap<>();
    private Long timeoutNanos;

    public TimeoutBasedFailureDetector(Long timeoutNanos) {
        this.timeoutNanos = timeoutNanos;
    }

    //<codeFragment name="TimeoutBasedFailureDetection">
    @Override
    void heartBeatCheck() {
        Long now = System.nanoTime();
        Set<T> serverIds = heartbeatReceivedTimes.keySet();
        for (T serverId : serverIds) {
            Long lastHeartbeatReceivedTime = heartbeatReceivedTimes.get(serverId);
            Long timeSinceLastHeartbeat = now - lastHeartbeatReceivedTime;
            if (timeSinceLastHeartbeat >= timeoutNanos) {
                markDown(serverId);
            }
        }
    }
    //</codeFragment>
    //<codeFragment name="recordHearbeatTimestamp">
    @Override
    public void heartBeatReceived(T serverId) {
        Long currentTime = System.nanoTime();
        heartbeatReceivedTimes.put(serverId, currentTime);
        markUp(serverId);
    }
    //</codeFragment>

}
