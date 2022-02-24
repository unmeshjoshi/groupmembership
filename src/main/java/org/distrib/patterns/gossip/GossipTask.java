package org.distrib.patterns.gossip;

import org.distrib.patterns.common.Logging;

public class GossipTask implements Runnable, Logging {
    private Gossip gossip;

    public GossipTask(Gossip gossip) {
        this.gossip = gossip;
    }

    @Override
    public void run() {
        getLogger().info("Running gossip task");
        try {
            gossip.doGossip();
        } catch (Exception e) {
           getLogger().error(e);
        }
    }
}
