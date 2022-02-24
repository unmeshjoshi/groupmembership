package org.distrib.patterns.zookeeper;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;

public class ServerImpl implements IZkDataListener {
    private String serverId;
    private ZookeeperClient zookeeperClient;
    private String currentLeader;

    public ServerImpl(String serverId, ZookeeperClient zookeeperClient) {
        this.serverId = serverId;
        this.zookeeperClient = zookeeperClient;
    }

    //<codeFragment name="election">
    public void startup() {
        zookeeperClient.subscribeLeaderChangeListener(this);
        elect();
    }

    public void elect() {
        var leaderId = serverId;
        try {
            zookeeperClient.tryCreatingLeaderPath(leaderId);
            this.currentLeader = serverId;
            onBecomingLeader();
        } catch (ZkNodeExistsException e) {
            //back off
            this.currentLeader = zookeeperClient.getLeaderId();
        }
    }
    //</codeFragment>

    public String getCurrentLeader() {
        return this.currentLeader;
    }

    private void onBecomingLeader() {
        //Do things like leader
    }

    @Override
    public void handleDataChange(String dataPath, Object data) throws Exception {

    }

    //<codeFragment name="reelection">
    @Override
    public void handleDataDeleted(String dataPath) throws Exception {
        elect();
    }
    //</codeFragment>

    public void stop() {
        this.zookeeperClient.close();
    }
}
