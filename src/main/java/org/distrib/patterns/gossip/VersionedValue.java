package org.distrib.patterns.gossip;

import com.google.common.base.Objects;

public class VersionedValue {
    //<codeFragment name="versionedValue">
    long version;
    String value;

    public VersionedValue(String value, long version) {
        this.version = version;
        this.value = value;
    }

    public long getVersion() {
        return version;
    }

    public String getValue() {
        return value;
    }
    //</codeFragment>
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionedValue that = (VersionedValue) o;
        return version == that.version &&
                Objects.equal(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(version, value);
    }

    @Override
    public String toString() {
        return "VersionedValue{" +
                "version=" + version +
                ", value=" + value +
                '}';
    }

    //for jaxon
    private VersionedValue() {
    }

    public VersionedValue updateVersion() {
        return new VersionedValue(this.value, this.version + 1);
    }
}
