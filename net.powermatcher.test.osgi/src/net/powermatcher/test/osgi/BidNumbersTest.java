package net.powermatcher.test.osgi;

import java.util.List;

import junit.framework.TestCase;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.core.concentrator.Concentrator;
import net.powermatcher.examples.Freezer;
import net.powermatcher.examples.PVPanelAgent;
import net.powermatcher.examples.StoringObserver;

import org.apache.felix.scr.ScrService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class BidNumbersTest extends TestCase {

	private final BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
    private ServiceReference<?> scrServiceReference = context.getServiceReference( ScrService.class.getName());
    private ScrService scrService = (ScrService) context.getService(scrServiceReference);
    private ConfigurationAdmin configAdmin;
    
    private ClusterHelper clusterHelper;
    
    @Override 
    protected void setUp() throws Exception {
    	super.setUp();
    	
    	clusterHelper = new ClusterHelper();

    	configAdmin = clusterHelper.getService(context, ConfigurationAdmin.class);

    	// Cleanup running agents to start with clean test
    	Configuration[] configs = configAdmin.listConfigurations(null);
    	if (configs != null) {
        	for (Configuration config : configs) {
        		config.delete();
        	}
    	}
    }

    /**
     * Tests a simple buildup of a cluster in OSGI and sanity tests.
     * Custer consists of Auctioneer, Concentrator and 2 agents.
     */
    public void testSimpleClusterBuildUp() throws Exception {
    	// Create Auctioneer
    	Configuration auctioneerConfig = clusterHelper.createConfiguration(configAdmin, clusterHelper.getFactoryPidAuctioneer(), clusterHelper.getAuctioneerProperties(clusterHelper.getAgentIdAuctioneer(), 1000));

    	// Wait for Auctioneer to become active
    	clusterHelper.checkServiceByPid(context, auctioneerConfig.getPid(), Auctioneer.class);
    	
    	// Create Concentrator
    	Configuration concentratorConfig = clusterHelper.createConfiguration(configAdmin, clusterHelper.getFactoryPidConcentrator(), clusterHelper.getConcentratorProperties(clusterHelper.getAgentIdConcentrator(), clusterHelper.getAgentIdAuctioneer(), 1000));
    	
    	// Wait for Concentrator to become active
    	clusterHelper.checkServiceByPid(context, concentratorConfig.getPid(), Concentrator.class);
    	
    	// Create PvPanel
    	Configuration pvPanelConfig = clusterHelper.createConfiguration(configAdmin, clusterHelper.getFactoryPidPvPanel(), clusterHelper.getPvPanelProperties(clusterHelper.getAgentIdPvPanel() , clusterHelper.getAgentIdConcentrator(), 12));
    	
    	// Wait for PvPanel to become active
    	clusterHelper.checkServiceByPid(context, pvPanelConfig.getPid(), PVPanelAgent.class);

    	// Create Freezer
    	Configuration freezerConfig = clusterHelper.createConfiguration(configAdmin, clusterHelper.getFactoryPidFreezer(), clusterHelper.getFreezerProperties(clusterHelper.getAgentIdFreezer(), clusterHelper.getAgentIdConcentrator(), 1));
    	
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
    	// check Freezer alive
    	assertEquals(true, clusterHelper.checkActive(scrService, clusterHelper.getFactoryPidFreezer()));
    	
    	//Create StoringObserver
    	Configuration storingObserverConfig = clusterHelper.createConfiguration(configAdmin, clusterHelper.getFactoryPidObserver(), clusterHelper.getStoringObserverProperties());
    	
    	// Wait for StoringObserver to become active
    	StoringObserver observer = clusterHelper.getServiceByPid(context, storingObserverConfig.getPid(), StoringObserver.class);
    	
    	//Checking to see if all agents send bids
    	Thread.sleep(10000);
    	checkBidsFullCluster(observer);
    }

    private void checkBidsFullCluster(StoringObserver observer) throws Exception {
    	// Are any bids available for each agent (at all)
    	assertFalse(observer.getOutgoingBidEvents(clusterHelper.getAgentIdConcentrator()).isEmpty());
    	assertFalse(observer.getOutgoingBidEvents(clusterHelper.getAgentIdPvPanel()).isEmpty());
    	assertFalse(observer.getOutgoingBidEvents(clusterHelper.getAgentIdFreezer()).isEmpty());
    	
    	// Validate bidNumbers of freezer and pvPanel
    	checkBidNumbersWithDifferentPriceUpdates(observer);
    }
    
    private void checkBidNumbersWithDifferentPriceUpdates(StoringObserver observer) throws Exception {
    	List<IncomingPriceUpdateEvent> priceUpdateEventPvPanel = observer.getIncomingPriceUpdateEvents(clusterHelper.getAgentIdPvPanel());
    	List<IncomingPriceUpdateEvent> priceUpdateEventFreezer = observer.getIncomingPriceUpdateEvents(clusterHelper.getAgentIdFreezer());
    	boolean sameBidNumberPvPanel = true;
    	
    	// pvpanel will receive same bidNrs because of slow bidUpdateRate
    	// freezer has high bidUpdateRate; pvpanel will receive several prices, but with same bidNr
    	for(int i = 0; i < priceUpdateEventPvPanel.size() - 2; i++) {
    		if (!(priceUpdateEventPvPanel.get(i).getPriceUpdate().getBidNumber() == 
    				priceUpdateEventPvPanel.get(i+1).getPriceUpdate().getBidNumber())) {
    			sameBidNumberPvPanel = false;
    		}
    		assertTrue(sameBidNumberPvPanel);
    	}
    	
    	// freezer will receive different bidNrs because of high bidUpdateRate
    	for(int i = 0; i < priceUpdateEventFreezer.size() -1; i++) {
    		boolean sameBidNumberFreezer = false;
    		if (!(priceUpdateEventFreezer.get(i).getPriceUpdate().getBidNumber() == 
    				priceUpdateEventFreezer.get(i+1).getPriceUpdate().getBidNumber())) {
    			sameBidNumberFreezer = true;
    		}
    		assertTrue(sameBidNumberFreezer);
    	}
    }
}
