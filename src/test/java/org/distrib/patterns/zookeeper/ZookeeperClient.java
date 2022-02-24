package org.distrib.patterns.zookeeper;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.distrib.patterns.common.JsonSerDes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ZookeeperClient {

    private ZkClient zkClient;

    public ZookeeperClient(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    private String LeaderPath = "/controller";

    public void tryCreatingLeaderPath(String serverId) {
        createEphemeralPath(zkClient, LeaderPath, serverId);
    }

    public void createEphemeralPath(ZkClient client, String path , String data) {
        try {
            client.createEphemeral(path, data);
        } catch(ZkNoNodeException e)  {
            createParentPath(client, path);
            client.createEphemeral(path, data);
        }
    }

    public String getLeaderId() {
        return zkClient.readData(LeaderPath);
    }

    //<codeFragment name="LeaderChangeSubscription">
    public void subscribeLeaderChangeListener(IZkDataListener listener) {
        zkClient.subscribeDataChanges(LeaderPath, listener);
    }
    //</codeFragment>

    String BrokerIdsPath = "/server/ids";
    String ControllerPath = "/controller";
    String ReplicaLeaderElectionPath = "/topics/replica/leader";

    void registerBroker(ServerDetails serverDetails) {
        var brokerData = JsonSerDes.serialize(serverDetails);
        var brokerPath = getBrokerPath(serverDetails.id);
        createEphemeralPath(zkClient, brokerPath, new String(brokerData));
    }

    Set<ServerDetails> getAllBrokers() {
        return zkClient.getChildren(BrokerIdsPath).stream().map(brokerId -> {
                String data = zkClient.readData(getBrokerPath(Integer.valueOf(brokerId)));
              return JsonSerDes.deserialize(data.getBytes(), ServerDetails.class);
        }).collect(Collectors.toSet());
    }



    ServerDetails getBrokerInfo(Integer brokerId) {
        String data = zkClient.readData(getBrokerPath(brokerId));
        return JsonSerDes.deserialize(data.getBytes(), ServerDetails.class);
    }

    List<String> subscribeBrokerChangeListener(IZkChildListener listener) {
        var result = zkClient.subscribeChildChanges(BrokerIdsPath, listener);
        return result;
    }

    private String getBrokerPath(Integer id) {
        return BrokerIdsPath + "/" + id;
    }

    private void createParentPath(ZkClient client,  String path) {
        var parentDir = path.substring(0, path.lastIndexOf('/'));
        if (parentDir.length() != 0)
            client.createPersistent(parentDir, true);
    }


    public void close() {
        zkClient.close();
    }
}
