package org.distrib.patterns.gossip;

import org.distrib.patterns.common.TestUtils;
import org.distrib.patterns.net.InetAddressAndPort;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GossipTest {

    @Test
    public void clusterStateReconcilesAcrossMultipleNodes() throws IOException {
        InetAddressAndPort seed = TestUtils.randomAddress();
        Gossip g1 = new Gossip(seed, Collections.emptyList(), "seed");
        g1.start();

        var clusterSize = 10;
        List<Gossip> clusterNodes = new ArrayList<>();
        for (int i = 1; i < clusterSize; i++) {
            InetAddressAndPort listenAddress = TestUtils.randomAddress();
            Gossip g = new Gossip(listenAddress, Arrays.asList(seed), "node" + i);
            g.start();
            clusterNodes.add(g);
        }

        List<NodeId> nodeIds = clusterNodes.stream().map(n -> n.nodeId).collect(Collectors.toList());

        TestUtils.waitUntilTrue(()->{
            return clusterNodes.stream().allMatch(n -> n.clusterMetadata.keySet().containsAll(nodeIds));

        }, "waiting for gossip state to converge", Duration.ofSeconds(20));
    }
}