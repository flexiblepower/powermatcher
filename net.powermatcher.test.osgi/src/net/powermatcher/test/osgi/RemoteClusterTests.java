package net.powermatcher.test.osgi;

import static net.powermatcher.test.osgi.ClusterHelper.AGENT_ID_CONCENTRATOR;
import static net.powermatcher.test.osgi.ClusterHelper.AGENT_ID_FREEZER;
import static net.powermatcher.test.osgi.ClusterHelper.AGENT_ID_PV_PANEL;
import net.powermatcher.api.data.Price;
import net.powermatcher.test.helpers.PropertiesBuilder;
import net.powermatcher.test.helpers.TestingObserver;

import org.osgi.service.cm.Configuration;

public class RemoteClusterTests
    extends OsgiTestCase {

    private final String PID_PM_WEBSOCKET = "net.powermatcher.remote.websockets.server.PowermatcherWebSocketServlet";
    private final String FACTORY_PID_WEBSOCKET_CLIENT = "net.powermatcher.remote.websockets.client.WebsocketClient";
    private final String AGENT_ID_WEBSOCKET_CLIENT = "websocket-client";
    private final String AGENT_ID_WEBSOCKET_SERVER = "remote-127.0.0.1-websocket-client";

    private TestingObserver observer;

    private Configuration pvPanelConfig, freezerConfig, concentratorConfig, auctioneerConfig;

    /**
     * Tests a simple buildup of a cluster in OSGI and sanity tests. Custer consists of Auctioneer, Concentrator, 1
     * local agent and 1 remote agents.
     */
    public void testSimpleClusterBuildUpWithRemoteAgent() throws Exception {
        LOGGER.info("TEST: testSimpleClusterBuildUpWithRemoteAgent");
        setupRemoteCluster();
        checkBidsFullCluster();
    }

    /**
     * Tests whether agent removal actually makes the bid obsolete of this agent The agent should also not receive any
     * price updates. After attaching the remote agent, bids must be complete again.
     */
    public void testAgentRemoval() throws Exception {
        LOGGER.info("TEST: testAgentRemoval");
        setupRemoteCluster();
        checkBidsFullCluster();

        // disconnect Freezer
        LOGGER.info("Disconnecting the freezer");
        clusterHelper.getComponent(freezerConfig.getPid()).disable();
        checkBidsClusterNoFreezer();

        // Re-add Freezer agent, it should not receive bids from previous freezer
        LOGGER.info("Reconnecting the freezer");
        clusterHelper.getComponent(freezerConfig.getPid()).enable();
        checkBidsFullCluster();
    }

    /**
     * Tests whether concentrator removal disables the whole cluster.
     */
    public void testConcentratorRemoval() throws Exception {
        LOGGER.info("TEST: testConcentratorRemoval");
        setupRemoteCluster();
        checkBidsFullCluster();

        LOGGER.info("Disconnecting the concentrator");
        clusterHelper.getComponent(concentratorConfig.getPid()).disable();
        checkBidsNoCluster();

        LOGGER.info("Reconnecting the concentrator");
        clusterHelper.getComponent(concentratorConfig.getPid()).enable();
        checkBidsFullCluster();
    }

    private void setupRemoteCluster() throws Exception {
        // Create simple cluster
        auctioneerConfig = clusterHelper.createAuctioneer(0);
        concentratorConfig = clusterHelper.createConcentrator(0);
        clusterHelper.waitForService(concentratorConfig);

        // Create Powermatcher Websocket
        Configuration serverConfiguration = clusterHelper.createConfiguration(PID_PM_WEBSOCKET,
                                                                              getPmSocketProperties(AGENT_ID_CONCENTRATOR));

        // Create matcher proxy
        Configuration websocketClientConfiguration = clusterHelper.createConfiguration(FACTORY_PID_WEBSOCKET_CLIENT,
                                                                                       getWebsocketClientProperties(AGENT_ID_WEBSOCKET_CLIENT));
        clusterHelper.waitForService(websocketClientConfiguration);

        // Create local PvPanel
        pvPanelConfig = clusterHelper.createPvPanel(1);
        clusterHelper.waitForService(pvPanelConfig);

        // Create remove Freezer -> connected to matcher endpoint proxy
        freezerConfig = clusterHelper.createFreezer(ClusterHelper.AGENT_ID_FREEZER, AGENT_ID_WEBSOCKET_CLIENT, 1);
        clusterHelper.waitForService(freezerConfig);

        clusterHelper.waitForComponentToBecomeActive(auctioneerConfig.getPid());
        clusterHelper.waitForComponentToBecomeActive(concentratorConfig.getPid());
        clusterHelper.waitForComponentToBecomeActive(serverConfiguration.getPid());
        clusterHelper.waitForComponentToBecomeActive(websocketClientConfiguration.getPid());
        clusterHelper.waitForComponentToBecomeActive(pvPanelConfig.getPid());
        clusterHelper.waitForComponentToBecomeActive(freezerConfig.getPid());

        // Create StoringObserver
        observer = clusterHelper.getServiceByPid(clusterHelper.createStoringObserver());
    }

    private void checkBidsFullCluster() throws InterruptedException {
        Thread.sleep(300); // Just to be sure everything has shut down correctly
        observer.expectBidsFrom(10,
                                AGENT_ID_CONCENTRATOR,
                                AGENT_ID_PV_PANEL,
                                AGENT_ID_FREEZER,
                                AGENT_ID_WEBSOCKET_SERVER);
        observer.expectReceivingPriceUpdate(10,
                                            new Price(ClusterHelper.DEFAULT_MARKETBASIS, 1),
                                            AGENT_ID_CONCENTRATOR,
                                            AGENT_ID_WEBSOCKET_SERVER,
                                            AGENT_ID_PV_PANEL,
                                            AGENT_ID_FREEZER);
    }

    private void checkBidsClusterNoFreezer() throws InterruptedException {
        Thread.sleep(300); // Just to be sure everything has shut down correctly
        observer.expectBidsFrom(10, AGENT_ID_CONCENTRATOR, AGENT_ID_PV_PANEL);
        observer.expectReceivingPriceUpdate(10,
                                            new Price(ClusterHelper.DEFAULT_MARKETBASIS, 0),
                                            AGENT_ID_CONCENTRATOR,
                                            AGENT_ID_WEBSOCKET_SERVER,
                                            AGENT_ID_PV_PANEL);
    }

    private void checkBidsNoCluster() throws InterruptedException {
        Thread.sleep(300); // Just to be sure everything has shut down correctly
        observer.expectNothing(5);
    }

    private PropertiesBuilder getPmSocketProperties(String desiredParentId) {
        return new PropertiesBuilder().desiredParentId(desiredParentId).add("alias", "/powermatcher/websocket");
    }

    private PropertiesBuilder getWebsocketClientProperties(String agentId) {
        return new PropertiesBuilder().agentId(agentId)
                                      .minTimeBetweenBidUpdates(0)
                                      .add("powermatcherUrl",
                                           "ws://localhost:8181/powermatcher/websocket")
                                      .add("reconnectTimeout", 1)
                                      .add("connectTimeout", 60);
    }
}
