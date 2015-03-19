package net.powermatcher.test.osgi;

import java.util.List;
import java.util.Map;

import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingBidUpdateEvent;
import net.powermatcher.core.BaseMatcherEndpoint;
import net.powermatcher.core.bidcache.AggregatedBid;
import net.powermatcher.core.bidcache.BidCache;
import net.powermatcher.core.concentrator.Concentrator;
import net.powermatcher.examples.StoringObserver;

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
    private Concentrator concentrator;
    private StoringObserver observer;

    /**
     * Tests a simple buildup of a cluster in OSGI and sanity tests. Cluster consists of Auctioneer, Concentrator and 2
     * agents.
     */
    public void testSimpleClusterBuildUp() throws Exception {
        // Create simple cluster
        setupCluster();

        // Checking to see if all agents send bids
        Thread.sleep(10000);
        checkBidsFullCluster();
    }

    /**
     * Tests whether agent removal actually makes the bid obsolete of this agent The agent should also not receive any
     * price updates.
     */
    public void testAgentRemoval() throws Exception {
        // Create simple cluster
        setupCluster();

        // Checking to see if all agents send bids
        Thread.sleep(10000);
        checkBidsFullCluster();

        // disconnect Freezer
        clusterHelper.getComponent(freezerConfig.getPid()).disable();

        // Checking to see if the Freezer is no longer participating
        observer.clearEvents();
        Thread.sleep(10000);
        checkBidsClusterNoFreezer();

        // Re-add Freezer agent, it should not receive bids from previous freezer
        observer.clearEvents();
        clusterHelper.getComponent(freezerConfig.getPid()).enable();

        Thread.sleep(10000);
        checkBidsFullCluster();
    }

    /**
     * Tests whether auctioneer removal stops complete cluster but continues when Auctioneer is started again.
     */
    public void testAuctioneerRemoval() throws Exception {
        // Create simple cluster
        setupCluster();

        // Checking to see if all agents send bids
        Thread.sleep(10000);
        checkBidsFullCluster();

        // disconnect Auctioneer
        clusterHelper.getComponent(auctioneerConfig.getPid()).disable();

        // Checking to see if any bids were sent when the auctioneer was down.
        observer.clearEvents();
        Thread.sleep(10000);
        checkBidsClusterNoAuctioneer();

        // connect auctioneer, bid should start again
        clusterHelper.getComponent(auctioneerConfig.getPid()).enable();

        Thread.sleep(10000);
        checkBidsFullCluster();
    }

    /**
     * Disconnect Concentrator and reconnect Concentrator. Check if agents will receive bidUpdates again. Cluster
     * consists of Auctioneer, Concentrator and 2 agents.
     */
    public void testConcentratorRemoval() throws Exception {
        // Create simple cluster
        setupCluster();

        // Checking to see if all agents send bids
        Thread.sleep(10000);
        checkBidsFullCluster();

        // disconnect Concentrator
        clusterHelper.getComponent(concentratorConfig.getPid()).disable();

        // Checking to see if any bids were sent when the concentrator was down.
        observer.clearEvents();
        Thread.sleep(10000);

        checkBidsClusterNoConcentrator();

        // Connect concentrator, bid should start again
        clusterHelper.getComponent(concentratorConfig.getPid()).enable();

        Thread.sleep(10000);
        checkBidsFullCluster();
    }

    private void setupCluster() throws Exception {
        // Create Auctioneer and wait for it
        auctioneerConfig = clusterHelper.createAuctioneer(5000);
        clusterHelper.waitForService(auctioneerConfig);

        // Create Concentrator and wait for it
        concentratorConfig = clusterHelper.createConcentrator(5000);
        concentrator = clusterHelper.getServiceByPid(concentratorConfig);

        // Create PvPanel and wait for it
        pvPanelConfig = clusterHelper.createPvPanel(4);
        clusterHelper.waitForService(pvPanelConfig);

        // Create Freezer
        freezerConfig = clusterHelper.createFreezer(4);
        clusterHelper.waitForService(freezerConfig);

        // check if all components are alive
        clusterHelper.waitForComponentToBecomeActive(auctioneerConfig.getPid());
        clusterHelper.waitForComponentToBecomeActive(concentratorConfig.getPid());
        clusterHelper.waitForComponentToBecomeActive(pvPanelConfig.getPid());
        clusterHelper.waitForComponentToBecomeActive(freezerConfig.getPid());

        // Create StoringObserver
        observer = clusterHelper.getServiceByPid(clusterHelper.createStoringObserver());
    }

    private void checkBidsFullCluster() {
        // Are any bids available for each agent (at all)
        assertFalse(observer.getOutgoingBidUpdateEvents(ClusterHelper.AGENT_ID_CONCENTRATOR).isEmpty());
        assertFalse(observer.getOutgoingBidUpdateEvents(ClusterHelper.AGENT_ID_PV_PANEL).isEmpty());
        assertFalse(observer.getOutgoingBidUpdateEvents(ClusterHelper.AGENT_ID_FREEZER).isEmpty());

        // Validate bidnumbers
        checkBidNumbers(observer, ClusterHelper.AGENT_ID_CONCENTRATOR);
        checkBidNumbers(observer, ClusterHelper.AGENT_ID_FREEZER);
        checkBidNumbers(observer, ClusterHelper.AGENT_ID_PV_PANEL);
    }

    private void checkBidNumbers(StoringObserver observer, String agentId) {
        // Validate bidnumber incoming from concentrator for correct agent
        List<OutgoingBidUpdateEvent> agentBids = observer.getOutgoingBidUpdateEvents(agentId);
        List<IncomingPriceUpdateEvent> receivedPrices = observer.getIncomingPriceUpdateEvents(agentId);

        for (IncomingPriceUpdateEvent priceEvent : receivedPrices) {
            int priceBidnumber = priceEvent.getPriceUpdate().getBidNumber();
            boolean validBidNumber = false;

            for (OutgoingBidUpdateEvent bidEvent : agentBids) {
                if (bidEvent.getBidUpdate().getBidNumber() == priceBidnumber) {
                    validBidNumber = true;
                }
            }

            assertTrue("Price bidnumber " + priceBidnumber + " is unknown in bids for agent " + agentId, validBidNumber);
        }
    }

    private void checkBidsClusterNoFreezer() {
        assertFalse(observer.getOutgoingBidUpdateEvents(ClusterHelper.AGENT_ID_CONCENTRATOR).isEmpty());
        assertFalse(observer.getOutgoingBidUpdateEvents(ClusterHelper.AGENT_ID_PV_PANEL).isEmpty());
        assertTrue(observer.getOutgoingBidUpdateEvents(ClusterHelper.AGENT_ID_FREEZER).isEmpty());

        // Check aggregated bid does no longer contain freezer, by checking last aggregated against panel bids
        List<OutgoingBidUpdateEvent> concentratorBids = observer.getOutgoingBidUpdateEvents(ClusterHelper.AGENT_ID_CONCENTRATOR);
        List<OutgoingBidUpdateEvent> panelBids = observer.getOutgoingBidUpdateEvents(ClusterHelper.AGENT_ID_PV_PANEL);

        OutgoingBidUpdateEvent concentratorBid = concentratorBids.get(concentratorBids.size() - 1);
        boolean foundBid = false;
        for (OutgoingBidUpdateEvent panelBid : panelBids) {
            if (panelBid.getBidUpdate()
                        .getBid()
                        .toArrayBid()
                        .equals(concentratorBid.getBidUpdate().getBid().toArrayBid())) {
                foundBid = true;
            }
        }

        assertTrue("Concentrator still contains freezer bid", foundBid);

        // Validate last aggregated bid contains only pvPanel
        BaseMatcherEndpoint matcherPart = clusterHelper.getPrivateField(concentrator,
                                                                        "matcherPart",
                                                                        BaseMatcherEndpoint.class);
        BidCache bidCache = clusterHelper.getPrivateField(matcherPart, "bidCache", BidCache.class);
        AggregatedBid lastBid = clusterHelper.getPrivateField(bidCache, "lastBid", AggregatedBid.class);
        assertTrue(lastBid.getAgentBidReferences().containsKey(ClusterHelper.AGENT_ID_PV_PANEL));
        assertFalse(lastBid.getAgentBidReferences().containsKey(ClusterHelper.AGENT_ID_FREEZER));

        // Validate bidcache no longer conatins any reference to Freezer
        @SuppressWarnings("unchecked")
        Map<String, BidUpdate> agentBids = (Map<String, BidUpdate>) clusterHelper.getPrivateField(bidCache,
                                                                                                  "agentBids",
                                                                                                  Object.class);
        assertFalse(agentBids.containsKey(ClusterHelper.AGENT_ID_FREEZER));
        assertTrue(agentBids.containsKey(ClusterHelper.AGENT_ID_PV_PANEL));
    }

    private void checkBidsClusterNoAuctioneer() {
        assertTrue(observer.getOutgoingBidUpdateEvents(ClusterHelper.AGENT_ID_CONCENTRATOR).isEmpty());
        assertTrue(observer.getOutgoingBidUpdateEvents(ClusterHelper.AGENT_ID_PV_PANEL).isEmpty());
        assertTrue(observer.getOutgoingBidUpdateEvents(ClusterHelper.AGENT_ID_FREEZER).isEmpty());
    }

    private void checkBidsClusterNoConcentrator() {
        assertTrue(observer.getOutgoingBidUpdateEvents(ClusterHelper.AGENT_ID_PV_PANEL).isEmpty());
        assertTrue(observer.getOutgoingBidUpdateEvents(ClusterHelper.AGENT_ID_FREEZER).isEmpty());
    }
}
