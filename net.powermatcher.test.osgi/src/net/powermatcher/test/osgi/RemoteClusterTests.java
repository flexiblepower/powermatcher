package net.powermatcher.test.osgi;

import static net.powermatcher.test.osgi.ClusterHelper.AGENT_ID_AUCTIONEER;

import java.util.List;

import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingBidUpdateEvent;
import net.powermatcher.core.bidcache.AggregatedBid;
import net.powermatcher.core.concentrator.Concentrator;
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

    private Concentrator concentrator;

    /**
     * Tests a simple buildup of a cluster in OSGI and sanity tests. Custer consists of Auctioneer, Concentrator, 1
     * local agent and 1 remote agents.
     */
    public void testSimpleClusterBuildUpWithRemoteAgent() throws Exception {
        // Setup default remote enabled cluster
        setupRemoteCluster();

        // Checking to see if all agents send bids
        Thread.sleep(10000);
        checkBidsFullCluster(observer);
    }

    /**
     * Tests whether agent removal actually makes the bid obsolete of this agent The agent should also not receive any
     * price updates. After attaching the remote agent, bids must be complete again.
     */
    public void testAgentRemoval() throws Exception {
        // Setup default remote enabled cluster
        setupRemoteCluster();

        // Checking to see if all agents send bids
        Thread.sleep(10000);
        checkBidsFullCluster(observer);

        // disconnect Freezer
        logger.info("Disconnecting the freezer");
        clusterHelper.getComponent(freezerConfig.getPid()).disable();

        // Checking to see if the Freezer is no longer participating
        observer.clearEvents();
        Thread.sleep(10000);
        checkBidsClusterNoFreezer(observer, concentrator);

        // Re-add Freezer agent, it should not receive bids from previous freezer
        logger.info("Resconnecting the freezer");
        clusterHelper.getComponent(freezerConfig.getPid()).enable();

        observer.clearEvents();
        Thread.sleep(10000);
        checkBidsFullCluster(observer);
    }

    private void setupRemoteCluster() throws Exception {
        // Create simple cluster
        Configuration auctioneerConfig = clusterHelper.createAuctioneer(5000);
        Configuration concentratorConfig = clusterHelper.createConcentrator(5000);
        concentrator = clusterHelper.getServiceByPid(concentratorConfig);

        // Create agent proxy
        Configuration agentProxyConfiguration = clusterHelper.createConfiguration(FACTORY_PID_AGENT_PROXY,
                                                                                  getAgentProxyProperties(AGENT_ID_AGENT_PROXY,
                                                                                                          ClusterHelper.AGENT_ID_CONCENTRATOR,
                                                                                                          "remoteAgentEndpointId"));
        clusterHelper.waitForService(agentProxyConfiguration);

        // Create matcher proxy
        Configuration matcherProxyConfiguration = clusterHelper.createConfiguration(FACTORY_PID_MATCHER_PROXY,
                                                                                    getMatcherProxyProperties(AGENT_ID_MATCHER_PROXY,
                                                                                                              AGENT_ID_AGENT_PROXY));
        clusterHelper.waitForService(matcherProxyConfiguration);

        // Create local PvPanel
        pvPanelConfig = clusterHelper.createPvPanel(4);
        clusterHelper.waitForService(pvPanelConfig);

        // Create remove Freezer -> connected to matcher endpoint proxy
        freezerConfig = clusterHelper.createFreezer(ClusterHelper.AGENT_ID_FREEZER, AGENT_ID_MATCHER_PROXY, 4);
        clusterHelper.waitForService(freezerConfig);

        clusterHelper.waitForComponentToBecomeActive(auctioneerConfig.getPid());
        clusterHelper.waitForComponentToBecomeActive(concentratorConfig.getPid());
        clusterHelper.waitForComponentToBecomeActive(agentProxyConfiguration.getPid());
        clusterHelper.waitForComponentToBecomeActive(matcherProxyConfiguration.getPid());
        clusterHelper.waitForComponentToBecomeActive(pvPanelConfig.getPid());
        clusterHelper.waitForComponentToBecomeActive(freezerConfig.getPid());

        // Create StoringObserver
        observer = clusterHelper.getServiceByPid(clusterHelper.createStoringObserver());
    }

    private void checkBidsFullCluster(StoringObserver observer) {
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

    private void checkBidsClusterNoFreezer(StoringObserver observer, Concentrator concentrator) {
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

    private PropertiesBuilder getMatcherProxyProperties(String agentId, String desiredConnectionId) {
        return new PropertiesBuilder().agentId(agentId)
                                      .add("desiredConnectionId", desiredConnectionId)
                                      .add("powermatcherUrl",
                                           "ws://localhost:8181/powermatcher/websockets/agentendpoint")
                                      .add("reconnectTimeout", 30)
                                      .add("connectTimeout", 60);
    }
}
