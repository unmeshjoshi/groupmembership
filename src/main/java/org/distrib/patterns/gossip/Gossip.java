package org.distrib.patterns.gossip;

import org.distrib.patterns.common.JsonSerDes;
import org.distrib.patterns.common.Logging;
import org.distrib.patterns.common.RequestOrResponse;
import org.distrib.patterns.net.InetAddressAndPort;
import org.distrib.patterns.net.RequestConsumer;
import org.distrib.patterns.net.SocketClient;
import org.distrib.patterns.net.nioserver.zkstyle.NIOSocketListener;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class GossipKeys {
    public static final String ADDRESS = "address";
    public static final String HEARTBEAT = "heartbeat";
}

public class Gossip implements Logging {
    private int generation;
    private int correlationId;
    //<codeFragment name="stateVersion">
    private int gossipStateVersion = 1;


    private int incremenetVersion() {
        return gossipStateVersion++;
    }
    //</codeFragment>

    private final NIOSocketListener socketServer;
    private final InetAddressAndPort listenAddress;
    private final List<InetAddressAndPort> seedNodes;
    NodeId nodeId;

    //<codeFragment name="gossipInstanceInitialization">
    public Gossip(InetAddressAndPort listenAddress,
                  List<InetAddressAndPort> seedNodes,
                  String nodeId) throws IOException {
        this.listenAddress = listenAddress;
        //filter this node itself in case its part of the seed nodes
        this.seedNodes = removeSelfAddress(seedNodes);
        this.nodeId = new NodeId(nodeId);
        addLocalState(GossipKeys.ADDRESS, listenAddress.toString());

        this.socketServer = new NIOSocketListener(newGossipRequestConsumer(), listenAddress);
    }

    private void addLocalState(String key, String value) {
        NodeState nodeState = clusterMetadata.get(listenAddress);
        if (nodeState == null) {
            nodeState = new NodeState();
            clusterMetadata.put(nodeId, nodeState);
        }
        nodeState.add(key, new VersionedValue(value, incremenetVersion()));
    }
    //</codeFragment>

    private RequestConsumer newGossipRequestConsumer() {
        return request -> {
            handleRequest(request);
        };
    }

    private void handleRequest(org.distrib.patterns.common.Message<RequestOrResponse> request) {
        RequestId requestId = request.getRequestId();
        if (requestId == RequestId.PushPullGossipState) {
            handleGossipRequest(request);
        }
        else if (requestId == RequestId.GossipVersions) {
            GossipStateVersions knownVersions = JsonSerDes.deserialize(request.getRequest().getMessageBodyJson(), GossipStateVersions.class);
            getMissingAndNodeStatesHigherThan(knownVersions.knownNodeStateVersions);

        }
    }
    //<codeFragment name="handleGossipMessage">
    private void handleGossipRequest(org.distrib.patterns.common.Message<RequestOrResponse> request) {
        GossipStateMessage gossipStateMessage = deserialize(request.getRequest());
        Map<NodeId, NodeState> gossipedState = gossipStateMessage.getNodeStates();
        getLogger().info("Merging state from " + request.getClientSocket());
        merge(gossipedState);

        Map<NodeId, NodeState> diff = delta(this.clusterMetadata, gossipedState);
        GossipStateMessage diffResponse = new GossipStateMessage(this.nodeId, diff);
        getLogger().info("Sending diff response " + diff);
        request.getClientSocket().write(new RequestOrResponse(RequestId.PushPullGossipState.getId(),
                        JsonSerDes.serialize(diffResponse),
                        request.getRequest().getCorrelationId()));
    }
    //</codeFragment>

    //<codeFragment name="getMissingAndNodeStatesHigherThan">
    Map<NodeId, NodeState> getMissingAndNodeStatesHigherThan(Map<NodeId, Long> nodeMaxVersions) {
        Map<NodeId, NodeState> delta = new HashMap<>();
        delta.putAll(higherVersionedNodeStates(nodeMaxVersions));
        delta.putAll(missingNodeStates(nodeMaxVersions));
        return delta;
    }

    private Map<NodeId, NodeState> missingNodeStates(Map<NodeId, Long> nodeMaxVersions) {
        Map<NodeId, NodeState> delta = new HashMap<>();
        List<NodeId> missingKeys = clusterMetadata.keySet().stream().filter(key -> !nodeMaxVersions.containsKey(key)).collect(Collectors.toList());
        for (NodeId missingKey : missingKeys) {
            delta.put(missingKey, clusterMetadata.get(missingKey));
        }
        return delta;
    }

    private Map<NodeId, NodeState> higherVersionedNodeStates(Map<NodeId, Long> nodeMaxVersions) {
        Map<NodeId, NodeState> delta = new HashMap<>();
        Set<NodeId> keySet = nodeMaxVersions.keySet();
        for (NodeId node : keySet) {
            Long maxVersion = nodeMaxVersions.get(node);
            NodeState nodeState = clusterMetadata.get(node);
            if (nodeState == null) {
                continue;
            }
            NodeState deltaState = nodeState.statesGreaterThan(maxVersion);
            if (!deltaState.isEmpty()) {
                delta.put(node, deltaState);
            }
        }
        return delta;
    }
    //</codeFragment>

    //<codeFragment name="clusterMetadata">
    Map<NodeId, NodeState> clusterMetadata = new HashMap<>();
    //</codeFragment>

    //<codeFragment name="gossipScheduler">
    private ScheduledThreadPoolExecutor gossipExecutor = new ScheduledThreadPoolExecutor(1);
    private long gossipIntervalMs = 1000;
    private ScheduledFuture<?> taskFuture;
    public void start() {
        socketServer.start();
        taskFuture = gossipExecutor.scheduleAtFixedRate(()-> doGossip(),
                    gossipIntervalMs,
                    gossipIntervalMs,
                    TimeUnit.MILLISECONDS);
    }
    //</codeFragment>

    public void stop() {
        socketServer.shudown();
        if (taskFuture != null) {
            taskFuture.cancel(true);
        }
    }

    public Map<NodeId, NodeState> getClusterMetadata() {
        return clusterMetadata;
    }


    public void pushPullState() {
        //
    }

    //<codeFragment name="diff">
    public Map<NodeId, NodeState> delta(Map<NodeId, NodeState> fromMap, Map<NodeId, NodeState> toMap) {
        Map<NodeId, NodeState> delta = new HashMap<>();
        for (NodeId key : fromMap.keySet()) {
            if (!toMap.containsKey(key)) {
                delta.put(key, fromMap.get(key));
                continue;
            }
            NodeState fromStates = fromMap.get(key);
            NodeState toStates = toMap.get(key);
            NodeState diffStates = fromStates.diff(toStates);
            if (!diffStates.isEmpty()) {
                delta.put(key, diffStates);
            }
        }
        return delta;
    }
    //</codeFragment>
    //<codeFragment name="merge">
    public void merge(Map<NodeId, NodeState> otherState) {
        Map<NodeId, NodeState> diff = delta(otherState, this.clusterMetadata);
        //check the metadata so see if there is a key in this.clusterMetadata
        // and put it in there if absent.
    }
    //</codeFragment>
    private int gossipFanout = 2;
    //<codeFragment name="doGossip">
    public void doGossip() {
        List<InetAddressAndPort> knownClusterNodes = liveNodes();
        if (knownClusterNodes.isEmpty()) {
            sendGossip(seedNodes, gossipFanout);
        } else {
            sendGossip(knownClusterNodes, gossipFanout);
        }
    }

    private List<InetAddressAndPort> liveNodes() {
        Set<InetAddressAndPort> nodes
                = clusterMetadata.values()
                .stream()
                .map(n -> InetAddressAndPort.parse(n.get(GossipKeys.ADDRESS).getValue()))
                .collect(Collectors.toSet());
        return removeSelfAddress(nodes);
    }
    //</codeFragment>

    private List<InetAddressAndPort> removeSelfAddress(Collection<InetAddressAndPort> nodes) {
        return nodes.stream().filter(address -> !address.equals(listenAddress)).collect(Collectors.toList());
    }


    private void updateHeartbeat() {
        NodeState stateMap = this.clusterMetadata.get(nodeId);
        VersionedValue heartbeat = stateMap.get(GossipKeys.HEARTBEAT);
        if (heartbeat == null) {
            heartbeat = new VersionedValue("1", incremenetVersion());
        }
        stateMap.add(GossipKeys.HEARTBEAT, heartbeat.updateVersion());
    }

    //<codeFragment name="sendGossip"/>
    private void sendGossip(List<InetAddressAndPort> knownClusterNodes, int gossipFanout) {
        if (knownClusterNodes.isEmpty()) {
            return;
        }

        for (int i = 0; i < gossipFanout; i++) {
            InetAddressAndPort nodeAddress = pickRandomNode(knownClusterNodes);
            sendGossipTo(nodeAddress);
        }
    }

    private void sendGossipTo(InetAddressAndPort nodeAddress) {
        try {
            getLogger().info("Sending gossip state to " + nodeAddress);
            SocketClient<RequestOrResponse> socketClient = new SocketClient(nodeAddress);
            GossipStateMessage gossipStateMessage
                    = new GossipStateMessage(this.nodeId, this.clusterMetadata);
            RequestOrResponse request
                    = createGossipStateRequest(gossipStateMessage);
            byte[] responseBytes = socketClient.blockingSend(request);
            GossipStateMessage responseState = deserialize(responseBytes);
            merge(responseState.getNodeStates());

        } catch (IOException e) {
            getLogger().error("IO error while sending gossip state to " + nodeAddress, e);
        }
    }

    private RequestOrResponse createGossipStateRequest(GossipStateMessage gossipStateMessage) {
        return new RequestOrResponse(RequestId.PushPullGossipState.getId(),
                JsonSerDes.serialize(gossipStateMessage), correlationId++);
    }
    //</codeFragment>
    //<codeFragment name="pickRandomNode">
    private Random random = new Random();
    private InetAddressAndPort pickRandomNode(List<InetAddressAndPort> knownClusterNodes) {
        int randomNodeIndex = random.nextInt(knownClusterNodes.size());
        InetAddressAndPort gossipTo = knownClusterNodes.get(randomNodeIndex);
        return gossipTo;
    }
    //</codeFragment>


    //<codeFragment name="sendMaxKnownVersions">
    private void sendKnownVersions(InetAddressAndPort gossipTo) throws IOException {
        Map<NodeId, Long> maxKnownNodeVersions = getMaxKnownNodeVersions();
        RequestOrResponse knownVersionRequest = new RequestOrResponse(RequestId.GossipVersions.getId(),
                JsonSerDes.serialize(new GossipStateVersions(maxKnownNodeVersions)), 0);
        SocketClient<RequestOrResponse> socketClient = new SocketClient(gossipTo);
        byte[] knownVersionResponseBytes = socketClient.blockingSend(knownVersionRequest);
    }

    private Map<NodeId, Long> getMaxKnownNodeVersions() {
        return clusterMetadata.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().maxVersion()));
    }
    //</codeFragment>


    private GossipStateMessage deserialize(byte[] responseBytes) {
        RequestOrResponse deltaResponse = JsonSerDes.deserialize(responseBytes, RequestOrResponse.class);
        GossipStateMessage gossipStateMessage = deserialize(deltaResponse);
        return gossipStateMessage;
    }


    private GossipStateMessage deserialize(RequestOrResponse deltaResponse) {
        return JsonSerDes.deserialize(deltaResponse.getMessageBodyJson(), GossipStateMessage.class);
    }
}
