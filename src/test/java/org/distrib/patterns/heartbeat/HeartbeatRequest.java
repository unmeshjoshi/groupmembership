package org.distrib.patterns.heartbeat;

public class HeartbeatRequest {
    private Integer serverId;

    //For Jaxon
    private HeartbeatRequest(){}

    public HeartbeatRequest(Integer serverId) {
        this.serverId = serverId;
    }

    public Integer getServerId() {
        return serverId;
    }
}
