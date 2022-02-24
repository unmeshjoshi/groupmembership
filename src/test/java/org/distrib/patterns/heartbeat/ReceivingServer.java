package org.distrib.patterns.heartbeat;

import org.distrib.patterns.common.Config;
import org.distrib.patterns.common.JsonSerDes;
import org.distrib.patterns.common.Message;
import org.distrib.patterns.common.RequestOrResponse;
import org.distrib.patterns.net.InetAddressAndPort;
import org.distrib.patterns.net.SocketListener;

public class ReceivingServer {
    private Config config;
    private InetAddressAndPort listenAddress;
    private SocketListener listener;
    private AbstractFailureDetector failureDetector;

    //<codeFragment name="Receiver">
    public ReceivingServer(Config config, InetAddressAndPort listenAddress, AbstractFailureDetector failureDetector) {
        this.config = config;
        this.listenAddress = listenAddress;
        this.failureDetector = failureDetector;
        this.listener = new SocketListener(this::handleRequest, listenAddress, config);
    }
    //</codeFragment>
    public void start() {
        listener.start();
        failureDetector.start();
    }

    //<codeFragment name="heartbetReceived">
    private void handleRequest(Message<RequestOrResponse> request) {
        RequestOrResponse clientRequest = request.getRequest();
        if (isHeartbeatRequest(clientRequest)) {
            HeartbeatRequest heartbeatRequest = JsonSerDes.deserialize(clientRequest.getMessageBodyJson(), HeartbeatRequest.class);
            failureDetector.heartBeatReceived(heartbeatRequest.getServerId());
            sendResponse(request);
        } else {
            //processes other requests
        }
    }
    //</codeFragment>

    private boolean isHeartbeatRequest(RequestOrResponse clientRequest) {
        return clientRequest.getRequestId() == HeartBeatRequestKeys.HeartBeatRequest.ordinal();
    }

    private void sendResponse(Message<RequestOrResponse> request) {
        var response = new RequestOrResponse(HeartBeatRequestKeys.HeartBeatRequest.ordinal(), JsonSerDes.serialize(""), request.getRequest().getCorrelationId());
        request.getClientSocket().write(response);
    }

    public void stop() {
        listener.stop();
        failureDetector.stop();
    }
}
