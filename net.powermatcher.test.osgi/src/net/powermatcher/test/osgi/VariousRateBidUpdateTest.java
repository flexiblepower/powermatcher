package net.powermatcher.test.osgi;

import java.util.List;

import junit.framework.TestCase;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingBidEvent;
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

public class VariousRateBidUpdateTest extends TestCase {

	private final String FACTORY_PID_AUCTIONEER = "net.powermatcher.core.auctioneer.Auctioneer";
	private final String FACTORY_PID_CONCENTRATOR = "net.powermatcher.core.concentrator.Concentrator";
	private final String FACTORY_PID_PV_PANEL = "net.powermatcher.examples.PVPanelAgent";
	private final String FACTORY_PID_FREEZER = "net.powermatcher.examples.Freezer";
	private final String FACTORY_PID_OBSERVER = "net.powermatcher.examples.StoringObserver";
	
	private final String AGENT_ID_AUCTIONEER = "auctioneer";
	private final String AGENT_ID_CONCENTRATOR = "concentrator";
	private final String AGENT_ID_PV_PANEL = "pvPanel";
	private final String AGENT_ID_FREEZER = "freezer";
	
	private final BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
    private ServiceReference<?> scrServiceReference = context.getServiceReference( ScrService.class.getName());
    private ScrService scrService = (ScrService) context.getService(scrServiceReference);
    private ConfigurationAdmin configAdmin;
    
    private ClusterHelper cluster;
    
    @Override 
    protected void setUp() throws Exception {
    	super.setUp();
    	cluster = new ClusterHelper();
    	
    	configAdmin = cluster.getService(context, ConfigurationAdmin.class);

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
    	Configuration auctioneerConfig = cluster.createConfiguration(configAdmin, FACTORY_PID_AUCTIONEER, cluster.getAuctioneerProperties(AGENT_ID_AUCTIONEER, 5000));
    	
    	// Wait for Auctioneer to become active
    	cluster.checkServiceByPid(context, auctioneerConfig.getPid(), Auctioneer.class);
    	
    	// Create Concentrator
    	Configuration concentratorConfig = cluster.createConfiguration(configAdmin, FACTORY_PID_CONCENTRATOR, cluster.getConcentratorProperties(AGENT_ID_CONCENTRATOR, AGENT_ID_AUCTIONEER, 5000));
    	
    	// Wait for Concentrator to become active
    	cluster.checkServiceByPid(context, concentratorConfig.getPid(), Concentrator.class);
    	
    	// Create PvPanel
    	Configuration pvPanelConfig = cluster.createConfiguration(configAdmin, FACTORY_PID_PV_PANEL, cluster.getPvPanelProperties(AGENT_ID_PV_PANEL, AGENT_ID_CONCENTRATOR, 4));
    	
    	// Wait for PvPanel to become active
    	cluster.checkServiceByPid(context, pvPanelConfig.getPid(), PVPanelAgent.class);

    	// Create Freezer
    	Configuration freezerConfig = cluster.createConfiguration(configAdmin, FACTORY_PID_FREEZER, cluster.getFreezerProperties(AGENT_ID_FREEZER, AGENT_ID_CONCENTRATOR, 4));
    	
    	// Wait for Freezer to become active
    	cluster.checkServiceByPid(context, freezerConfig.getPid(), Freezer.class);

    	// Wait a little time for all components to become satisfied / active
    	Thread.sleep(2000);
    	
    	// check Auctioneer alive
    	assertEquals(true, cluster.checkActive(scrService, FACTORY_PID_AUCTIONEER));
    	// check Concentrator alive
    	assertEquals(true, cluster.checkActive(scrService, FACTORY_PID_CONCENTRATOR));
    	// check PvPanel alive
    	assertEquals(true, cluster.checkActive(scrService, FACTORY_PID_PV_PANEL));
    	
    	//Create StoringObserver
    	Configuration storingObserverConfig = cluster.createConfiguration(configAdmin, FACTORY_PID_OBSERVER, cluster.getStoringObserverProperties());
    	
    	// Wait for StoringObserver to become active
    	StoringObserver observer = cluster.getServiceByPid(context, storingObserverConfig.getPid(), StoringObserver.class);
    	
    	//Checking to see if all agents send bids
    	Thread.sleep(10000);
    	checkBidsFullCluster(observer);
    }

    private void checkBidsFullCluster(StoringObserver observer) {
    	// Are any bids available for each agent (at all)
    	assertFalse(observer.getOutgoingBidEvents(AGENT_ID_CONCENTRATOR).isEmpty());
    	assertFalse(observer.getOutgoingBidEvents(AGENT_ID_PV_PANEL).isEmpty());
    	assertFalse(observer.getOutgoingBidEvents(AGENT_ID_FREEZER).isEmpty());
    	
    	// Validate bidnumbers
    	checkBidNumbers(observer, AGENT_ID_CONCENTRATOR);
    	checkBidNumbers(observer, AGENT_ID_FREEZER);
    	checkBidNumbers(observer, AGENT_ID_PV_PANEL);
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
