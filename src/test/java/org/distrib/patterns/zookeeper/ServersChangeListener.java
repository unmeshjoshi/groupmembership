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
      logger.info("Server change listener called for path " + parentPath + " invoked with " + currentChildren);

      Set<ServerDetails> current = currentChildren.stream().map(b -> {
            return client.getServerDetails(Integer.parseInt(b));
        }).collect(Collectors.toSet());

    //1)diff and find the list of new servers.
      //find the difference between liveServerDetails set and the currentChildrenSet.
      //The new changes should be stored in liveServerDetails Set.

    //2) diff and find the list of new servers.
    //find the difference between liveServerDetails set and the currentChildrenSet.
    //The missing server details should be removed from liveServerDetails Set.
        var newBrokers = new TreeSet();
        var disconnectedBrokers = new TreeSet<ServerDetails>();

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
