package org.distrib.patterns.heartbeat;

import org.distrib.patterns.common.Config;
import org.distrib.patterns.common.TestUtils;
import org.distrib.patterns.net.InetAddressAndPort;
import org.distrib.patterns.net.Networks;
import org.junit.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class HeartbeatTest {

    @Test
    public void shouldMarkSenderAsFailedAfterTimeoutPeriod() {
        var localhost = new Networks().ipv4Address().getHostAddress();
        var receiverIp = InetAddressAndPort.create(localhost, TestUtils.getRandomPort());
        Config config = new Config(TestUtils.tempDir("HeartbeatTest").getAbsolutePath());
        TimeoutBasedFailureDetector<Integer> failureDetector = new TimeoutBasedFailureDetector<Integer>(TimeUnit.MILLISECONDS.toNanos(200));
        ReceivingServer receivingServer = new ReceivingServer(config, receiverIp, failureDetector);
        receivingServer.start();

        int senderId = 0;
        SendingServer sendingServer = new SendingServer(senderId, receiverIp, 100l);
        sendingServer.start();

        TestUtils.waitUntilTrue(()->{
            return failureDetector.isAlive(senderId);
        }, "Waiting for sender to be marked alive", Duration.of(2, ChronoUnit.SECONDS));

        sendingServer.stop();

        TestUtils.waitUntilTrue(()->{
            return failureDetector.isAlive(senderId) == false;
        }, "Waiting for sender to be marked failed", Duration.of(2, ChronoUnit.SECONDS));
        receivingServer.stop();
    }

    @Test
    public void shouldMarkSenderAsFailedAfterPhiThreshold() throws InterruptedException {
        var localhost = new Networks().ipv4Address().getHostAddress();
        var receiverIp = InetAddressAndPort.create(localhost, TestUtils.getRandomPort());

        int senderId = 0;

        Config config = new Config(TestUtils.tempDir("HeartbeatTest").getAbsolutePath());
        AbstractFailureDetector<Integer> failureDetector = new PhiAccrualFailureDetector<Integer>();
        ReceivingServer receivingServer = new ReceivingServer(config, receiverIp, failureDetector);
        receivingServer.start();

        SendingServer sendingServer = new SendingServer(senderId, receiverIp, 100l);
        sendingServer.start();

        TestUtils.waitUntilTrue(()->{
            return failureDetector.isAlive(senderId);
        }, "Waiting for sender to be marked alive", Duration.of(10, ChronoUnit.SECONDS));

        Thread.sleep(1000); //take some time for multiple heartbeats to reach receiver to calculate phi values better
        sendingServer.stop();

        TestUtils.waitUntilTrue(()->{
            return failureDetector.isAlive(senderId) == false;
        }, "Waiting for sender to be marked failed", Duration.of(10, ChronoUnit.SECONDS));

        receivingServer.stop();
    }
}