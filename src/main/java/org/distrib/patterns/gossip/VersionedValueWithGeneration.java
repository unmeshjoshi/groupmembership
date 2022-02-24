package org.distrib.patterns.gossip;

import com.google.common.base.Objects;

public class VersionedValueWithGeneration {
    //<codeFragment name="versionedValueWithGeneration">
    int generation;
    int version;
    String value;

    public VersionedValueWithGeneration(String value, int version, int generation) {
        this.version = version;
        this.value = value;
        this.generation = generation;
    }

    public int getVersion() {
        return version;
    }

    public String getValue() {
        return value;
    }

    public int getGeneration() {
        return generation;
    }

    //</codeFragment>


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionedValueWithGeneration that = (VersionedValueWithGeneration) o;
        return generation == that.generation &&
                version == that.version &&
                Objects.equal(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(generation, version, value);
    }

    //for jaxon
    private VersionedValueWithGeneration() {
    }
}
