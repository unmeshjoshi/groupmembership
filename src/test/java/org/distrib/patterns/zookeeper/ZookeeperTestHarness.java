package org.distrib.patterns.zookeeper;

import org.I0Itec.zkclient.ZkClient;
import org.junit.After;
import org.junit.Before;

public class ZookeeperTestHarness {
    String zookeeperConnect = "127.0.0.1:2182";
    EmbeddedZookeeper zookeeper;
    ZkClient zkClient;
    int zkConnectionTimeout = 10000;
    int zkSessionTimeout = 15000;

    @Before
    public void beforeEach() {
        zookeeper = new EmbeddedZookeeper(zookeeperConnect);
        zkClient = new ZkClient(zookeeper.connectString, zkSessionTimeout, zkConnectionTimeout, new ZKStringSerializer());
    }

    @After
    public void afterEach() {
        zkClient.close();
        zookeeper.shutdown();
    }
}
