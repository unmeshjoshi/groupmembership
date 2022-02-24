package org.distrib.patterns.zookeeper;

import org.I0Itec.zkclient.IZkChildListener;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ServersChangeListener implements IZkChildListener {
    private static Logger logger = LogManager.getLogger(ServersChangeListener.class);

    private Set<ServerDetails> liveServerDetails = new TreeSet<>();
    private ZookeeperClient client;

    public ServersChangeListener(ZookeeperClient client) {
        this.client = client;
    }

    @Override
    public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception {
      logger.info("Broker change listener fired for path " + parentPath + " invoked with " + currentChildren);
        Set<ServerDetails> current = currentChildren.stream().map(b -> {
            return client.getBrokerInfo(Integer.parseInt(b));
        }).collect(Collectors.toSet());

        var newBrokers = new TreeSet<ServerDetails>(current);
        newBrokers.removeAll(liveServerDetails); //list of new brokers.

        var disconnectedBrokers = new TreeSet<>(liveServerDetails);
        disconnectedBrokers.removeAll(current); //list of removed brokers.

        addNewBrokers(newBrokers);
        removeDeadBrokers(disconnectedBrokers);
    }

    private void removeDeadBrokers(TreeSet<ServerDetails> disconnectedServerDetails) {
        liveServerDetails.addAll(disconnectedServerDetails);
    }

    private void addNewBrokers(TreeSet<ServerDetails> newServerDetails) {
        liveServerDetails.addAll(newServerDetails);
    }

    public Set<ServerDetails> getLiveBrokers() {
        return liveServerDetails;
    }
}
