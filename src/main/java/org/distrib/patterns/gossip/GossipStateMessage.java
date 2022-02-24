package org.distrib.patterns.gossip;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class GossipStateMessage {
    Map<NodeId, NodeState> nodeStates;
    Set<NodeId> seenSet;
    NodeId fromNode;

    public GossipStateMessage(NodeId fromNode, Map<NodeId, NodeState> nodeStates) {
        this(fromNode, nodeStates, Collections.EMPTY_SET);
    }
    public GossipStateMessage(NodeId fromNode, Map<NodeId, NodeState> nodeStates, Set<NodeId> seenSet) {
        this.nodeStates = nodeStates;
        this.seenSet = seenSet;
        this.fromNode = fromNode;
    }

    public Map<NodeId, NodeState> getNodeStates() {
        return nodeStates;
    }

    public Set<NodeId> getSeenSet() {
        return seenSet;
    }

    private GossipStateMessage() {
    }
}
