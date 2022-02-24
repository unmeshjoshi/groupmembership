package org.distrib.patterns.zookeeper;

import org.I0Itec.zkclient.ZkClient;
import org.distrib.patterns.common.TestUtils;
import org.junit.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertEquals;

public class ServerImplTest extends ZookeeperTestHarness {

    @Test
    public void shouldElectLeader() {
        ServerImpl m1 = new ServerImpl("1", new ZookeeperClient(new ZkClient(zookeeperConnect, zkSessionTimeout, zkConnectionTimeout, new ZKStringSerializer())));
        ServerImpl m2 = new ServerImpl("2", new ZookeeperClient(new ZkClient(zookeeperConnect, zkSessionTimeout, zkConnectionTimeout, new ZKStringSerializer())));
        m1.startup();
        m2.startup();

        assertEquals("1", m1.getCurrentLeader());
        assertEquals("1", m2.getCurrentLeader());
    }

    @Test
    public void shouldElectNewLeaderWhenExistingLeaderFails() {
        ServerImpl m1 = new ServerImpl("1", new ZookeeperClient(new ZkClient(zookeeperConnect, zkSessionTimeout, zkConnectionTimeout, new ZKStringSerializer())));
        ServerImpl m2 = new ServerImpl("2", new ZookeeperClient(new ZkClient(zookeeperConnect, zkSessionTimeout, zkConnectionTimeout, new ZKStringSerializer())));
        m1.startup();
        m2.startup();
        assertEquals("1", m1.getCurrentLeader());
        assertEquals("1", m2.getCurrentLeader());

        m1.stop();

        TestUtils.waitUntilTrue(()->{
            return m2.getCurrentLeader() == "2";
        }, "Waiting for m2 to be elected leader", Duration.of(10, ChronoUnit.SECONDS));
    }
}