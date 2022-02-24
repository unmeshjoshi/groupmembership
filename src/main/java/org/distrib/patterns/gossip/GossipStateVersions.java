package org.distrib.patterns.gossip;

import java.util.Map;

public class GossipStateVersions {
    Map<NodeId, Long> knownNodeStateVersions;

    public GossipStateVersions(Map<NodeId, Long> knownNodeStateVersions) {
        this.knownNodeStateVersions = knownNodeStateVersions;
    }

    public Map<NodeId, Long> getKnownNodeStateVersions() {
        return knownNodeStateVersions;
    }

    //for jaxon
    private GossipStateVersions() {
    }
}
