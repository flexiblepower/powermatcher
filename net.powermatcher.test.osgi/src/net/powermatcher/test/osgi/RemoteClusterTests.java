package net.powermatcher.test.osgi;

import static net.powermatcher.test.osgi.ClusterHelper.AGENT_ID_AUCTIONEER;
import static net.powermatcher.test.osgi.ClusterHelper.AGENT_ID_CONCENTRATOR;
import static net.powermatcher.test.osgi.ClusterHelper.AGENT_ID_FREEZER;
import static net.powermatcher.test.osgi.ClusterHelper.AGENT_ID_PV_PANEL;

import java.util.List;

import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingBidUpdateEvent;
import net.powermatcher.core.bidcache.AggregatedBid;
import net.powermatcher.examples.StoringObserver;
import net.powermatcher.test.helpers.PropertiesBuilder;

import org.osgi.service.cm.Configuration;

public class RemoteClusterTests
    extends OsgiTestCase {

    private final String FACTORY_PID_AGENT_PROXY = "net.powermatcher.remote.websockets.AgentEndpointProxyWebsocket";
    private final String FACTORY_PID_MATCHER_PROXY = "net.powermatcher.remote.websockets.MatcherEndpointProxyWebsocket";
    private final String AGENT_ID_AGENT_PROXY = "aep1";
    private final String AGENT_ID_MATCHER_PROXY = "mep1";

    private StoringObserver observer;

    private Configuration pvPanelConfig;
    private Configuration freezerConfig;

    /**
     * Tests a simple buildup of a cluster in OSGI and sanity tests. Custer consists of Auctioneer, Concentrator, 1
     * local agent and 1 remote agents.
     */
    public void testSimpleClusterBuildUpWithRemoteAgent() throws Exception {
        LOGGER.info("TEST: testSimpleClusterBuildUpWithRemoteAgent");

        // Setup default remote enabled cluster
        setupRemoteCluster();

        // Checking to see if all agents send bids
        Thread.sleep(10000);
        checkBidsFullCluster();
    }

    /**
     * Tests whether agent removal actually makes the bid obsolete of this agent The agent should also not receive any
     * price updates. After attaching the remote agent, bids must be complete again.
     */
    public void testAgentRemoval() throws Exception {
        LOGGER.info("TEST: testAgentRemoval");

        // Setup default remote enabled cluster
        setupRemoteCluster();

        // Checking to see if all agents send bids
        Thread.sleep(10000);
        checkBidsFullCluster();

        // disconnect Freezer
        LOGGER.info("Disconnecting the freezer");
        clusterHelper.getComponent(freezerConfig.getPid()).disable();

        // Checking to see if the Freezer is no longer participating
        observer.clearEvents();
        Thread.sleep(10000);
        checkBidsClusterNoFreezer();

        // Re-add Freezer agent, it should not receive bids from previous freezer
        LOGGER.info("Reconnecting the freezer");
        clusterHelper.getComponent(freezerConfig.getPid()).enable();

        observer.clearEvents();
        Thread.sleep(10000);
        checkBidsFullCluster();
    }

    private void setupRemoteCluster() throws Exception {
        // Create simple cluster
        Configuration auctioneerConfig = clusterHelper.createAuctioneer(5000);
        Configuration concentratorConfig = clusterHelper.createConcentrator(5000);
        clusterHelper.waitForService(concentratorConfig);

        // Create matcher proxy
        Configuration matcherProxyConfiguration = clusterHelper.createConfiguration(FACTORY_PID_MATCHER_PROXY,
                                                                                    getMatcherProxyProperties(AGENT_ID_MATCHER_PROXY));
        clusterHelper.waitForService(matcherProxyConfiguration);

        // Create local PvPanel
        pvPanelConfig = clusterHelper.createPvPanel(4);
        clusterHelper.waitForService(pvPanelConfig);

        // Create remove Freezer -> connected to matcher endpoint proxy
        freezerConfig = clusterHelper.createFreezer(ClusterHelper.AGENT_ID_FREEZER, AGENT_ID_MATCHER_PROXY, 4);
        clusterHelper.waitForService(freezerConfig);

        clusterHelper.waitForComponentToBecomeActive(auctioneerConfig.getPid());
        clusterHelper.waitForComponentToBecomeActive(concentratorConfig.getPid());
        // TODO check for new agent proxy
        // clusterHelper.waitForComponentToBecomeActive(agentProxyConfiguration.getPid());
        clusterHelper.waitForComponentToBecomeActive(matcherProxyConfiguration.getPid());
        clusterHelper.waitForComponentToBecomeActive(pvPanelConfig.getPid());
        clusterHelper.waitForComponentToBecomeActive(freezerConfig.getPid());

        // Create StoringObserver
        observer = clusterHelper.getServiceByPid(clusterHelper.createStoringObserver());
    }

    private PriceUpdate getLastObservedPriceUpdate(String agentId) {
        return getLast(observer.getIncomingPriceUpdateEvents(agentId)).getPriceUpdate();
    }

    private void checkBidsFullCluster() {
        // Are any bids available for each agent (at all)
        assertNotEmpty(observer.getOutgoingBidUpdateEvents(AGENT_ID_CONCENTRATOR));
        assertNotEmpty(observer.getOutgoingBidUpdateEvents(AGENT_ID_PV_PANEL));
        assertNotEmpty(observer.getOutgoingBidUpdateEvents(AGENT_ID_FREEZER));

        // Did all the parts receive a price?
        assertNotEmpty(observer.getIncomingPriceUpdateEvents(AGENT_ID_CONCENTRATOR));
        assertNotEmpty(observer.getIncomingPriceUpdateEvents(AGENT_ID_PV_PANEL));
        assertNotEmpty(observer.getIncomingPriceUpdateEvents(AGENT_ID_FREEZER));

        assertEquals(1, getLastObservedPriceUpdate(AGENT_ID_CONCENTRATOR).getPrice().getPriceValue(), 0);
        assertEquals(1, getLastObservedPriceUpdate(AGENT_ID_PV_PANEL).getPrice().getPriceValue(), 0);
        assertEquals(1, getLastObservedPriceUpdate(AGENT_ID_FREEZER).getPrice().getPriceValue(), 0);

        // Validate bidnumbers
        checkBidNumbers(AGENT_ID_CONCENTRATOR);
        checkBidNumbers(AGENT_ID_PV_PANEL);
        checkBidNumbers(AGENT_ID_FREEZER);
    }

    private void checkBidNumbers(String agentId) {
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
        AggregatedBid lastBid = (AggregatedBid) getLast(observer.getIncomingBidUpdateEvents(AGENT_ID_AUCTIONEER)).getBidUpdate()
                                                                                                                 .getBid();
        assertTrue(lastBid.getAgentBidReferences().containsKey(ClusterHelper.AGENT_ID_PV_PANEL));
        assertFalse(lastBid.getAgentBidReferences().containsKey(AGENT_ID_AGENT_PROXY));
    }

    private PropertiesBuilder getAgentProxyProperties(String agentId,
                                                      String agentIdMatcher,
                                                      String remoteAgentEndpointId) {
        return new PropertiesBuilder().agentId(agentId).desiredParentId(agentIdMatcher)
                                      .add("remoteAgentEndpointId", remoteAgentEndpointId);
    }

    private PropertiesBuilder getMatcherProxyProperties(String agentId) {
        return new PropertiesBuilder().agentId(agentId)
                                      .minTimeBetweenBidUpdates(1000)
                                      .add("powermatcherUrl",
                                           "ws://localhost:8181/powermatcher/websockets/agentendpoint")
                                      .add("reconnectTimeout", 1)
                                      .add("connectTimeout", 60);
    }
}
