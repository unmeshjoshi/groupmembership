package org.distrib.patterns.gossip;

import com.google.common.base.Objects;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeState {
    //<codeFragment name="nodeStatemap">
    Map<String, VersionedValue> values = new HashMap<>();
    //</codeFragment>


    public NodeState() {
    }

    public NodeState(Map<String, VersionedValue> values) {
        this.values = values;
    }

    public void add(String key, VersionedValue versionedValue) {
        values.put(key, versionedValue);
    }

    public NodeState diff(NodeState toState) {
        NodeState diffStates = new NodeState();
        for (String s : values.keySet()) {
            if (!toState.containsKey(s)) {
                diffStates.add(s, values.get(s));
            } else {
                VersionedValue fromValue = toState.get(s);
                VersionedValue toValue = values.get(s);
                if (fromValue.version > toValue.version) {
                    diffStates.add(s, fromValue);
                }
            }
        }
        return diffStates;
    }


    //<codeFragment name="maxVersion">
    public long maxVersion() {
        return values.values().stream().map(v -> v.getVersion()).max(Comparator.naturalOrder()).orElse(Long.valueOf(0));
    }
    //</codeFragment>

    //<codeFragment name="stateFromVersion">
    public NodeState fromVersion(int maxVersion) {
        NodeState diffStates = new NodeState();
        for (String key : values.keySet()) {
            VersionedValue versionedValue = values.get(key);
            if (versionedValue.getVersion() > maxVersion) {
                diffStates.add(key, versionedValue);
            }
        }
        return diffStates;
    }
    //</codeFragment>


    VersionedValue get(String s) {
        return values.get(s);
    }

    private boolean containsKey(String s) {
        return values.containsKey(s);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public void putAll(NodeState nodeState) {
        this.values.putAll(nodeState.values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeState that = (NodeState) o;
        return Objects.equal(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(values);
    }

    @Override
    public String toString() {
        return "NodeState{" +
                "values=" + values +
                '}';
    }

    public NodeState statesGreaterThan(Long maxVersion) {
        Map<String, VersionedValue> map =  values.entrySet().stream().filter(e -> e.getValue().getVersion() > maxVersion).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        return new NodeState(map);
    }
}
