package org.distrib.patterns.zookeeper;

import org.distrib.patterns.common.TestUtils;
import org.distrib.patterns.net.InetAddressAndPort;
import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.*;

public class ZookeeperClientTest extends ZookeeperTestHarness {

    @Test
    public void registerServerDetails() {
        var s1 = new ServerDetails(1, InetAddressAndPort.create("10.10.10.10", 8000));
        var s2 = new ServerDetails(2, InetAddressAndPort.create("10.10.10.11", 8000));

        ZookeeperClient client = new ZookeeperClient(zkClient);
        client.registerServerDetails(s1);
        client.registerServerDetails(s2);

        assertEquals(client.getAllBrokers().size(), 2);
    }

    @Test
    public void getsNotificationsAboutServers() {
        var s1 = new ServerDetails(1, InetAddressAndPort.create("10.10.10.10", 8000));
        var s2 = new ServerDetails(2, InetAddressAndPort.create("10.10.10.11", 8000));

        ZookeeperClient client = new ZookeeperClient(zkClient);

        ServersChangeListener listener = new ServersChangeListener(client);
        client.subscribeBrokerChangeListener(listener);
        client.registerServerDetails(s1);
        client.registerServerDetails(s2);

        TestUtils.waitUntilTrue(()->{
            System.out.println("listener = " + listener.getLiveBrokers());
            return listener.getLiveBrokers().size()== 2;
        }, "Waiting for listener to be notified", Duration.ofSeconds(5));
    }



}