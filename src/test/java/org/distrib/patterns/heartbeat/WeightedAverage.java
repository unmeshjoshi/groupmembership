package org.distrib.patterns.heartbeat;

//<codeFragment name="weightedAverage">
public class WeightedAverage {
    long averageLatencyMs = 0;
    public void update(long heartbeatRequestLatency) {
        //Example implementation of weighted average as used in Mongodb
        //The running, weighted average round trip time for heartbeat messages to the target node.
        // Weighted 80% to the old round trip time, and 20% to the new round trip time.
        averageLatencyMs = averageLatencyMs == 0
                ? heartbeatRequestLatency
                : (averageLatencyMs * 4 + heartbeatRequestLatency) / 5;
    }

    public long getAverageLatency() {
        return averageLatencyMs;
    }
}
//</codeFragment>