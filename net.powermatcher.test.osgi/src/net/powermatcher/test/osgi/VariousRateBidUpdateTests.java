package net.powermatcher.test.osgi;

import java.util.List;

import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingBidEvent;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.core.concentrator.Concentrator;
import net.powermatcher.examples.Freezer;
import net.powermatcher.examples.PVPanelAgent;
import net.powermatcher.examples.StoringObserver;

import org.osgi.service.cm.Configuration;

public class VariousRateBidUpdateTests extends OsgiTestCase {

	/**
     * Tests a simple buildup of a cluster in OSGI and sanity tests.
     * Custer consists of Auctioneer, Concentrator and 2 agents.
     */
    public void testSimpleClusterBuildUp() throws Exception {
    	// Create Auctioneer
    	Configuration auctioneerConfig = clusterHelper.createConfiguration(configAdmin, clusterHelper.getFactoryPidAuctioneer(), clusterHelper.getAuctioneerProperties(clusterHelper.getAgentIdAuctioneer(), 5000));
    	
    	// Wait for Auctioneer to become active
    	clusterHelper.checkServiceByPid(context, auctioneerConfig.getPid(), Auctioneer.class);
    	
    	// Create Concentrator
    	Configuration concentratorConfig = clusterHelper.createConfiguration(configAdmin, clusterHelper.getFactoryPidConcentrator(), clusterHelper.getConcentratorProperties(clusterHelper.getAgentIdConcentrator(), clusterHelper.getAgentIdAuctioneer(), 5000));
    	
    	// Wait for Concentrator to become active
    	clusterHelper.checkServiceByPid(context, concentratorConfig.getPid(), Concentrator.class);
    	
    	// Create PvPanel
    	Configuration pvPanelConfig = clusterHelper.createConfiguration(configAdmin, clusterHelper.getFactoryPidPvPanel(), clusterHelper.getPvPanelProperties(clusterHelper.getAgentIdPvPanel(), clusterHelper.getAgentIdConcentrator(), 4));
    	
    	// Wait for PvPanel to become active
    	clusterHelper.checkServiceByPid(context, pvPanelConfig.getPid(), PVPanelAgent.class);

    	// Create Freezer
    	Configuration freezerConfig = clusterHelper.createConfiguration(configAdmin, clusterHelper.getFactoryPidFreezer(), clusterHelper.getFreezerProperties(clusterHelper.getAgentIdFreezer(), clusterHelper.getAgentIdConcentrator(), 4));
    	
    	// Wait for Freezer to become active
    	clusterHelper.checkServiceByPid(context, freezerConfig.getPid(), Freezer.class);

    	// Wait a little time for all components to become satisfied / active
    	Thread.sleep(2000);
    	
    	// check Auctioneer alive
    	assertEquals(true, clusterHelper.checkActive(scrService, clusterHelper.getFactoryPidAuctioneer()));
    	// check Concentrator alive
    	assertEquals(true, clusterHelper.checkActive(scrService, clusterHelper.getFactoryPidConcentrator()));
    	// check PvPanel alive
    	assertEquals(true, clusterHelper.checkActive(scrService, clusterHelper.getFactoryPidPvPanel()));
    	
    	//Create StoringObserver
    	Configuration storingObserverConfig = clusterHelper.createConfiguration(configAdmin, clusterHelper.getFactoryPidObserver(), clusterHelper.getStoringObserverProperties());
    	
    	// Wait for StoringObserver to become active
    	StoringObserver observer = clusterHelper.getServiceByPid(context, storingObserverConfig.getPid(), StoringObserver.class);
    	
    	//Checking to see if all agents send bids
    	Thread.sleep(10000);
    	checkBidsFullCluster(observer);
    }

    private void checkBidsFullCluster(StoringObserver observer) {
    	// Are any bids available for each agent (at all)
    	assertFalse(observer.getOutgoingBidEvents(clusterHelper.getAgentIdConcentrator()).isEmpty());
    	assertFalse(observer.getOutgoingBidEvents(clusterHelper.getAgentIdPvPanel()).isEmpty());
    	assertFalse(observer.getOutgoingBidEvents(clusterHelper.getAgentIdFreezer()).isEmpty());
    	
    	// Validate bidnumbers
    	checkBidNumbers(observer, clusterHelper.getAgentIdConcentrator());
    	checkBidNumbers(observer, clusterHelper.getAgentIdFreezer());
    	checkBidNumbers(observer, clusterHelper.getAgentIdPvPanel());
    }
    
    private void checkBidNumbers(StoringObserver observer, String agentId) {
    	// Validate bidnumber incoming from concentrator for correct agent
    	List<OutgoingBidEvent> agentBids = observer.getOutgoingBidEvents(agentId);
    	List<IncomingPriceUpdateEvent> receivedPrices = observer.getIncomingPriceUpdateEvents(agentId);

    	for (IncomingPriceUpdateEvent priceEvent : receivedPrices) {
    		int priceBidnumber = priceEvent.getPriceUpdate().getBidNumber();
    		boolean validBidNumber = false;
    		
    		for (OutgoingBidEvent bidEvent : agentBids) {
    			if (bidEvent.getBidUpdate().getBidNumber() == priceBidnumber) {
    				validBidNumber = true;
    			}
    		}

    		assertTrue("Price bidnumber " + priceBidnumber + " is unknown in bids for agent " + agentId, validBidNumber);
    	}
    }
}
