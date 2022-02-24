package org.distrib.patterns.zookeeper;

import com.google.common.base.Objects;
import org.distrib.patterns.net.InetAddressAndPort;

public class ServerDetails implements Comparable<ServerDetails> {
    public Integer id;
    public InetAddressAndPort address;

    public ServerDetails(Integer id, InetAddressAndPort address) {
        this.id = id;
        this.address = address;
    }

    public Integer getId() {
        return id;
    }

    public InetAddressAndPort getAddress() {
        return address;
    }

    @Override
    public int compareTo(ServerDetails o) {
        return Integer.compare(this.id, o.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerDetails serverDetails = (ServerDetails) o;
        return Objects.equal(id, serverDetails.id) && Objects.equal(address, serverDetails.address);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, address);
    }

    //for jackson
    private ServerDetails() {
    }

}
