package org.distrib.patterns.gossip;

import com.google.common.base.Objects;

public class NodeId implements Comparable<NodeId> {
    private String id;

    public NodeId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeId nodeId = (NodeId) o;
        return Objects.equal(id, nodeId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "[" +
                id  +
                ']';
    }

    //for jaxon
    private NodeId() {
    }

    @Override
    public int compareTo(NodeId o) {
        return this.id.compareTo(o.id);
    }
}
