package net.powermatcher.test.osgi;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.core.concentrator.Concentrator;

import org.apache.felix.scr.Component;
import org.apache.felix.scr.ScrService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

import net.powermatcher.examples.PVPanelAgent;

public class ClusterTest extends TestCase {

	private final BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
    private ServiceReference<?> scrServiceReference = context.getServiceReference( ScrService.class.getName());
    private ScrService scrService = (ScrService) context.getService(scrServiceReference);
    
    public void testAddAgentsForCluster() throws Exception {
	    ConfigurationAdmin configAdmin = getService(ConfigurationAdmin.class);

	    // Create Auctioneer
    	String auctioneerFactoryPid = "net.powermatcher.core.auctioneer.Auctioneer";
    	Configuration auctioneerConfig = configAdmin.createFactoryConfiguration(auctioneerFactoryPid, null);
    	setAuctioneerProperties(auctioneerConfig);

    	// Wait for Auctioneer to become active
    	Auctioneer auctioneer = getServiceByPid(auctioneerConfig.getPid(), Auctioneer.class);
    	
    	assertNotNull(auctioneer);
    	Thread.sleep(1000);
    	
    	// Create Concentrator
    	String concentratorFactoryPid = "net.powermatcher.core.concentrator.Concentrator";
    	Configuration concentratorConfig = configAdmin.createFactoryConfiguration(concentratorFactoryPid, null);
    	setConcentratorProperties(concentratorConfig);
    	
    	// Wait for Concentrator to become active
    	Concentrator concentrator = getServiceByPid(concentratorConfig.getPid(), Concentrator.class);
    	
    	assertNotNull(concentrator);
    	Thread.sleep(1000);
    	
    	// Create PvPanel
    	String pvPanelFactoryPid = "net.powermatcher.examples.PVPanelAgent";
    	Configuration pvPanelConfig = configAdmin.createFactoryConfiguration(pvPanelFactoryPid, null);
    	setPvPanelProperties(pvPanelConfig);
    	
    	// Wait for PvPanel to become active
    	PVPanelAgent pvPanel = getServiceByPid(pvPanelConfig.getPid(), PVPanelAgent.class);
    	
    	assertNotNull(pvPanel);
    	Thread.sleep(1000);
    	
    	// check Auctioneer alive
    	assertEquals(true, auctioneerActive());
    	// check Concentrator alive
    	assertEquals(true, concentratorActive());
    	// check PvPanel alive
    	assertEquals(true, pvPanelActive());
    	
    	// disconnect Auctioneer 
    	this.disconnectAuctioneer(configAdmin);
    	Thread.sleep(10000);
    	
    	// check Concentrator and PvPanel are unsatisfied, because Auctioneer is NOT active
    	boolean[] activeAgents = this.testConcentratorPvPanelNotActive();
    	assertFalse(activeAgents[0]);
    	assertFalse(activeAgents[1]);
    }

	private boolean[] testConcentratorPvPanelNotActive() throws Exception {
		Component[] components = scrService.getComponents();
		boolean[] activeAgents = {true, true};
		
		for (Component comp : components) {
			if (comp.getConfigurationPid().equals("net.powermatcher.core.concentrator.Concentrator")) {
				if (comp.getState() == Component.STATE_UNSATISFIED) {
					activeAgents[0] = false;
				}
			}
			if (comp.getConfigurationPid().equals("net.powermatcher.examples.PVPanelAgent")) {
				if (comp.getState() == Component.STATE_UNSATISFIED) {
					activeAgents[1] = false;
				}
			}
		}
		
		return 	activeAgents;
	}
	
	private void disconnectAuctioneer(ConfigurationAdmin configAdmin) throws Exception, InvalidSyntaxException {
		String factoryPid = "net.powermatcher.core.auctioneer.Auctioneer";
		
		for (Configuration c : configAdmin.listConfigurations(null)) { 
			if (factoryPid.equals(c.getFactoryPid())) {
	            String agentId = (String) c.getProperties().get("agentId"); 
				if (agentId.equals("auctioneer")) {
					c.delete();
				}
			}
		}
    }
	
	private boolean auctioneerActive() throws Exception {
		Component[] components = scrService.getComponents("net.powermatcher.core.auctioneer.Auctioneer");
		boolean activeAuctioneer = false;
	
		for (Component comp : components) {
			if (comp.getConfigurationPid().equals("net.powermatcher.core.auctioneer.Auctioneer")) {
				if (comp.getState() == Component.STATE_ACTIVE) {
					activeAuctioneer = true;
				}
			}
		}
		return activeAuctioneer;
	}
    
	private boolean concentratorActive() throws Exception {
		Component[] components = scrService.getComponents("net.powermatcher.core.concentrator.Concentrator");
		boolean activeConcentrator = false;
	
		for (Component comp : components) {
			if (comp.getConfigurationPid().equals("net.powermatcher.core.concentrator.Concentrator")) {
				if (comp.getState() == Component.STATE_ACTIVE) {
					activeConcentrator = true;
				}
			}
		}
		return activeConcentrator;
	}

	private boolean pvPanelActive() throws Exception {
		Component[] components = scrService.getComponents("net.powermatcher.examples.PVPanelAgent");
		boolean activePvPanel = false;
	
		for (Component comp : components) {
			if (comp.getConfigurationPid().equals("net.powermatcher.examples.PVPanelAgent")) {
				if (comp.getState() == Component.STATE_ACTIVE) {
					activePvPanel = true;
				}
			}
		}
		return activePvPanel;
	}

    private <T> T getService(Class<T> type) throws InterruptedException {
        ServiceTracker<T, T> serviceTracker = 
                new ServiceTracker<T, T>(context, type, null);
        serviceTracker.open();
        T result = (T)serviceTracker.waitForService(10000);

        assertNotNull(result);
        
        return result;
    }

    private <T> T getServiceByPid(String pid, Class<T> type) throws InterruptedException {
    	String filter = "(" + Constants.SERVICE_PID + "=" + pid + ")";
    	
        ServiceTracker<T, T> serviceTracker;
        T result = null;
		try {
			serviceTracker = new ServiceTracker<T, T>(context, FrameworkUtil.createFilter(filter), null);
		
	        serviceTracker.open();
	        result = type.cast(serviceTracker.waitForService(10000));
		} catch (InvalidSyntaxException e) {
			fail(e.getMessage());
		}
		
		return result;
    }

    private void setPvPanelProperties(Configuration pvPanelConfig) throws Exception {
    	// create PvPanel props
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("agentId", "pvpanel");
    	properties.put("desiredParentId", "concentrator");
    	properties.put("bidUpdateRate", "30");
    	properties.put("minimumDemand", "-700");
    	properties.put("maximumDemand", "-600");
    	pvPanelConfig.update(properties);
    }
    
    private void setConcentratorProperties(Configuration concentratorConfig) throws Exception {
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("agentId", "concentrator");
    	properties.put("desiredParentId", "auctioneer");
    	properties.put("bidUpdateRate", "60");
    	properties.put("minTimeBetweenBidUpdates", "1000");
    	
    	concentratorConfig.update(properties);
    }
    
    private void setAuctioneerProperties(Configuration auctioneerConfig) throws Exception {
    	// create Auctioneer props
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("agentId", "auctioneer");
    	properties.put("clusterId", "DefaultCluster");
    	properties.put("commodity", "electricity");
    	properties.put("currency", "EUR");
    	properties.put("priceSteps", 100);
    	properties.put("minimumPrice", 0.0);
    	properties.put("maximumPrice", 1.0);
    	properties.put("bidTimeout", 600);
    	properties.put("priceUpdateRate", 30l);
    	properties.put("minTimeBetweenPriceUpdates", 1000);
    	auctioneerConfig.update(properties);
    }
    
}
