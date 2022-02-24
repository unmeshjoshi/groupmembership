package org.distrib.patterns.zookeeper;

import org.apache.zookeeper.server.NIOServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.distrib.patterns.common.TestUtils;

import java.io.File;
import java.net.InetSocketAddress;

class EmbeddedZookeeper {
    File snapshotDir = TestUtils.tempDir("zkSnap");
    File logDir = TestUtils.tempDir("zkLog");
    NIOServerCnxnFactory factory = new NIOServerCnxnFactory();
    int tickTime = 500;
    String connectString;

    public EmbeddedZookeeper(String connectString) {
        this.connectString = connectString;
        try {
            ZooKeeperServer zookeeper = new ZooKeeperServer(snapshotDir, logDir, tickTime);
            String port = connectString.split(":")[1];
            factory.configure(new InetSocketAddress("127.0.0.1", Integer.valueOf(port)), 60);
            factory.startup(zookeeper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        factory.shutdown();
        logDir.delete();
        snapshotDir.delete();
    }
}