package org.distrib.patterns.heartbeat;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.distrib.patterns.common.JsonSerDes;
import org.distrib.patterns.common.RequestOrResponse;
import org.distrib.patterns.net.InetAddressAndPort;
import org.distrib.patterns.net.SingleSocketChannel;

import java.io.IOException;


public class SendingServer {
    private static Logger logger = LogManager.getLogger(SendingServer.class.getName());
    private Integer serverId;
    private InetAddressAndPort receiverIp;
    private Long heartbeatIntervalMs;

    private HeartBeatScheduler heartBeatscheduler;
    private int correlationId = 0;
    private SingleSocketChannel socketChannel;

    //<codeFragment name="sender">
    public SendingServer(Integer serverId, InetAddressAndPort receiverIp, Long heartbeatIntervalMs) {
        this.serverId = serverId;
        this.receiverIp = receiverIp;
        this.heartbeatIntervalMs = heartbeatIntervalMs;
        this.heartBeatscheduler = new HeartBeatScheduler(() -> {
            try {
                sendHeartbeat();
            } catch (IOException e) {
                logger.error(e);
            }
        }, heartbeatIntervalMs);
    }
    //</codeFragment>
    public void start() {
        try {
            this.socketChannel = new SingleSocketChannel(receiverIp, Math.toIntExact(heartbeatIntervalMs));
            heartBeatscheduler.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //<codeFragment name="sendHeartbeat">
    private void sendHeartbeat() throws IOException {
        socketChannel.blockingSend(newHeartbeatRequest(serverId));
    }
    //</codeFragment>

    private RequestOrResponse newHeartbeatRequest(Integer serverId) {
        return new RequestOrResponse(requestId(), serialize(new HeartbeatRequest(serverId)), correlationId++);
    }

    private int requestId() {
        return HeartBeatRequestKeys.HeartBeatRequest.ordinal();
    }

    private byte[] serialize(HeartbeatRequest heartbeatRequest) {
        return JsonSerDes.serialize(heartbeatRequest);
    }

    public void stop() {
        heartBeatscheduler.stop();
    }
}
