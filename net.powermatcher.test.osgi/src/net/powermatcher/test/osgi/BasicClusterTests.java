package net.powermatcher.test.osgi;

import static net.powermatcher.test.osgi.ClusterHelper.AGENT_ID_CONCENTRATOR;
import static net.powermatcher.test.osgi.ClusterHelper.AGENT_ID_FREEZER;
import static net.powermatcher.test.osgi.ClusterHelper.AGENT_ID_PV_PANEL;
import net.powermatcher.api.data.Price;
import net.powermatcher.test.helpers.TestingObserver;

import org.osgi.service.cm.Configuration;

/**
 * Basic cluster tests and tests buildup and agent removal.
 *
 * @author FAN
 * @version 2.0
 */
public class BasicClusterTests
    extends OsgiTestCase {

    private Configuration auctioneerConfig, concentratorConfig, pvPanelConfig, freezerConfig;
    private TestingObserver observer;

    /**
     * Tests a simple buildup of a cluster in OSGI and sanity tests. Cluster consists of Auctioneer, Concentrator and 2
     * agents.
     */
    public void testSimpleClusterBuildUp() throws Exception {
        LOGGER.info("TEST: testSimpleClusterBuildUp");
        setupCluster();
        checkBidsFullCluster();
    }

    /**
     * Tests whether agent removal actually makes the bid obsolete of this agent The agent should also not receive any
     * price updates.
     */
    public void testAgentRemoval() throws Exception {
        LOGGER.info("TEST: testAgentRemoval");
        setupCluster();
        checkBidsFullCluster();

        // disconnect Freezer
        clusterHelper.getComponent(freezerConfig.getPid()).disable();
        checkBidsClusterNoFreezer();

        // Re-add Freezer agent, it should not receive bids from previous freezer
        clusterHelper.getComponent(freezerConfig.getPid()).enable();
        checkBidsFullCluster();
    }

    /**
     * Tests whether auctioneer removal stops complete cluster but continues when Auctioneer is started again.
     */
    public void testAuctioneerRemoval() throws Exception {
        LOGGER.info("TEST: testAuctioneerRemoval");
        setupCluster();
        checkBidsFullCluster();

        // disconnect Auctioneer
        clusterHelper.getComponent(auctioneerConfig.getPid()).disable();
        checkBidsNoCluster();

        // connect auctioneer, bid should start again
        clusterHelper.getComponent(auctioneerConfig.getPid()).enable();
        checkBidsFullCluster();
    }

    /**
     * Disconnect Concentrator and reconnect Concentrator. Check if agents will receive bidUpdates again. Cluster
     * consists of Auctioneer, Concentrator and 2 agents.
     */
    public void testConcentratorRemoval() throws Exception {
        LOGGER.info("TEST: testConcentratorRemoval");
        setupCluster();
        checkBidsFullCluster();

        // disconnect Concentrator
        clusterHelper.getComponent(concentratorConfig.getPid()).disable();
        checkBidsNoCluster();

        // Connect concentrator, bid should start again
        clusterHelper.getComponent(concentratorConfig.getPid()).enable();
        checkBidsFullCluster();
    }

    private void setupCluster() throws Exception {
        // Create Auctioneer and wait for it
        auctioneerConfig = clusterHelper.createAuctioneer(0);
        clusterHelper.waitForService(auctioneerConfig);

        // Create Concentrator and wait for it
        concentratorConfig = clusterHelper.createConcentrator(0);
        clusterHelper.waitForService(concentratorConfig);

        // Create PvPanel and wait for it
        pvPanelConfig = clusterHelper.createPvPanel(1);
        clusterHelper.waitForService(pvPanelConfig);

        // Create Freezer
        freezerConfig = clusterHelper.createFreezer(1);
        clusterHelper.waitForService(freezerConfig);

        // check if all components are alive
        clusterHelper.waitForComponentToBecomeActive(auctioneerConfig.getPid());
        clusterHelper.waitForComponentToBecomeActive(concentratorConfig.getPid());
        clusterHelper.waitForComponentToBecomeActive(pvPanelConfig.getPid());
        clusterHelper.waitForComponentToBecomeActive(freezerConfig.getPid());

        // Create StoringObserver
        observer = clusterHelper.getServiceByPid(clusterHelper.createStoringObserver());
        observer.clearEvents();
    }

    private void checkBidsFullCluster() throws InterruptedException {
        Thread.sleep(300); // Just to be sure everything has shut down correctly
        observer.expectBidsFrom(10, AGENT_ID_CONCENTRATOR, AGENT_ID_PV_PANEL, AGENT_ID_FREEZER);
        observer.expectReceivingPriceUpdate(10,
                                            new Price(ClusterHelper.DEFAULT_MARKETBASIS, 1),
                                            AGENT_ID_CONCENTRATOR,
                                            AGENT_ID_FREEZER,
                                            AGENT_ID_PV_PANEL);
    }

    private void checkBidsClusterNoFreezer() throws InterruptedException {
        Thread.sleep(300); // Just to be sure everything has shut down correctly
        observer.expectBidsFrom(10, AGENT_ID_CONCENTRATOR, AGENT_ID_PV_PANEL);
        observer.expectReceivingPriceUpdate(10,
                                            new Price(ClusterHelper.DEFAULT_MARKETBASIS, 0),
                                            AGENT_ID_CONCENTRATOR,
                                            AGENT_ID_PV_PANEL);
    }

    private void checkBidsNoCluster() throws InterruptedException {
        Thread.sleep(300); // Just to be sure everything has shut down correctly
        observer.expectNothing(5);
    }
}
