package net.powermatcher.test.osgi;

import java.util.List;

import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.examples.StoringObserver;

import org.osgi.service.cm.Configuration;

/**
 * Tests bidnumbers with various bid update rates.
 *
 * @author FAN
 * @version 2.0
 */
public class BidNumbersTests
    extends OsgiTestCase {

    /**
     * Tests a cluster with a slow and fast agent. The slow agent should receive the same bidnumber.
     */
    public void testVariableRateAgents() throws Exception {
        // Create Auctioneer and wait for it
        Configuration auctioneerConfig = clusterHelper.createAuctioneer(1000);
        clusterHelper.waitForService(auctioneerConfig);

        // Create Concentrator and wait for it
        Configuration concentratorConfig = clusterHelper.createConcentrator(1000);
        clusterHelper.waitForService(concentratorConfig);

        // Create PvPanel and wait for it
        Configuration pvPanelConfig = clusterHelper.createPvPanel(5);
        clusterHelper.waitForService(pvPanelConfig);

        // Create Freezer and wait for it
        Configuration freezerConfig = clusterHelper.createFreezer(1);
        clusterHelper.waitForService(freezerConfig);

        clusterHelper.waitForComponentToBecomeActive(auctioneerConfig.getPid());
        clusterHelper.waitForComponentToBecomeActive(concentratorConfig.getPid());
        clusterHelper.waitForComponentToBecomeActive(pvPanelConfig.getPid());
        clusterHelper.waitForComponentToBecomeActive(freezerConfig.getPid());

        // create and get the observer
        StoringObserver observer = clusterHelper.getServiceByPid(clusterHelper.createStoringObserver());

        // Checking to see if all agents send bids
        Thread.sleep(10000);
        checkBidsFullCluster(observer);
    }

    private void checkBidsFullCluster(StoringObserver observer) throws Exception {
        // Are any bids available for each agent (at all)
        assertFalse(observer.getOutgoingBidUpdateEvents(ClusterHelper.AGENT_ID_CONCENTRATOR).isEmpty());
        assertFalse(observer.getOutgoingBidUpdateEvents(ClusterHelper.AGENT_ID_PV_PANEL).isEmpty());
        assertFalse(observer.getOutgoingBidUpdateEvents(ClusterHelper.AGENT_ID_FREEZER).isEmpty());

        // Validate bidNumbers of freezer and pvPanel
        List<IncomingPriceUpdateEvent> priceUpdateEventPvPanel = observer.getIncomingPriceUpdateEvents(ClusterHelper.AGENT_ID_PV_PANEL);
        List<IncomingPriceUpdateEvent> priceUpdateEventFreezer = observer.getIncomingPriceUpdateEvents(ClusterHelper.AGENT_ID_FREEZER);
        boolean sameBidNumberPvPanel = true;

        // pvpanel will receive same bidNrs because of slow bidUpdateRate
        // freezer has high bidUpdateRate; pvpanel will receive several prices, but with same bidNr
        for (int i = 0; i < priceUpdateEventPvPanel.size() - 2; i++) {
            if (!(priceUpdateEventPvPanel.get(i).getPriceUpdate().getBidNumber() == priceUpdateEventPvPanel.get(i + 1)
                                                                                                           .getPriceUpdate()
                                                                                                           .getBidNumber())) {
                sameBidNumberPvPanel = false;
            }
            assertTrue(sameBidNumberPvPanel);
        }

        // freezer will receive different bidNrs because of high bidUpdateRate
        for (int i = 0; i < priceUpdateEventFreezer.size() - 1; i++) {
            boolean sameBidNumberFreezer = false;
            if (!(priceUpdateEventFreezer.get(i).getPriceUpdate().getBidNumber() == priceUpdateEventFreezer.get(i + 1)
                                                                                                           .getPriceUpdate()
                                                                                                           .getBidNumber())) {
                sameBidNumberFreezer = true;
            }
            assertTrue(sameBidNumberFreezer);
        }
    }
}
