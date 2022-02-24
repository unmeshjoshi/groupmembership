package org.distrib.patterns.gossip;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NodeStateTest {

    @Test
    public void shouldReturnMaxVersion() {
        NodeState state = new NodeState();
        state.add("nodeId", new VersionedValue("node1", 1));
        state.add("store1", new VersionedValue("s1", 2));
        state.add("store2", new VersionedValue("s2", 3));

        assertEquals(3, state.maxVersion());
    }

}