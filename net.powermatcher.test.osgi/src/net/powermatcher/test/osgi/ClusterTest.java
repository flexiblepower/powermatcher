package net.powermatcher.test.osgi;

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
    
    public void testAddAuctioneer() throws Exception {
	    ConfigurationAdmin configAdmin = getService(ConfigurationAdmin.class);

	    // Create Auctioneer
    	String auctioneerFactoryPid = "net.powermatcher.core.auctioneer.Auctioneer";
    	Configuration auctioneerConfig = configAdmin.createFactoryConfiguration(auctioneerFactoryPid, null);

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

    	// Wait for Auctioneer to become active
    	Auctioneer auctioneer = getServiceByPid(auctioneerConfig.getPid(), Auctioneer.class);
    	
    	assertNotNull(auctioneer);
    	Thread.sleep(1000);
    }

    public void testAddConcentrator() throws Exception {
	    ConfigurationAdmin configAdmin = getService(ConfigurationAdmin.class);

	    // Create Concentrator
    	String concentratorFactoryPid = "net.powermatcher.core.concentrator.Concentrator";
    	Configuration concentratorConfig = configAdmin.createFactoryConfiguration(concentratorFactoryPid, null);

    	// create Auctioneer props
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	
    	properties.put("agentId", "concentrator");
    	properties.put("desiredParentId", "auctioneer");
    	properties.put("bidUpdateRate", "60");
    	properties.put("minTimeBetweenBidUpdates", "1000");
    	
    	concentratorConfig.update(properties);
    	
    	// Wait for Concentrator to become active
    	Concentrator concentrator = getServiceByPid(concentratorConfig.getPid(), Concentrator.class);
    	
    	assertNotNull(concentrator);
    	Thread.sleep(1000);
    }

    public void testAddPvPanel() throws Exception {
	    ConfigurationAdmin configAdmin = getService(ConfigurationAdmin.class);

	    // Create PvPanel
    	String pvPanelFactoryPid = "net.powermatcher.examples.PVPanelAgent";
    	Configuration pvPanelConfig = configAdmin.createFactoryConfiguration(pvPanelFactoryPid, null);

    	// create PvPanel props
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("agentId", "pvpanel");
    	properties.put("desiredParentId", "concentrator");
    	properties.put("bidUpdateRate", "30");
    	properties.put("minimumDemand", "-700");
    	properties.put("maximumDemand", "-600");
    	pvPanelConfig.update(properties);
    	
    	// Wait for PvPanel to become active
    	PVPanelAgent pvPanel = getServiceByPid(pvPanelConfig.getPid(), PVPanelAgent.class);
    	
    	assertNotNull(pvPanel);
    	Thread.sleep(100);
    }
    
	public void testAuctioneerActive() throws Exception {
		Component[] components = scrService.getComponents("net.powermatcher.core.auctioneer.Auctioneer");
		boolean activeAuctioneer = false;
	
		for (Component comp : components) {
			if (comp.getConfigurationPid().equals("net.powermatcher.core.auctioneer.Auctioneer")) {
				if (comp.getState() == Component.STATE_ACTIVE) {
					activeAuctioneer = true;
				}
			}
		}
		assertEquals(true, activeAuctioneer);
	}
    
	public void testConcentratorActive() throws Exception {
		Component[] components = scrService.getComponents("net.powermatcher.core.concentrator.Concentrator");
		boolean activeConcentrator = false;
	
		for (Component comp : components) {
			if (comp.getConfigurationPid().equals("net.powermatcher.core.concentrator.Concentrator")) {
				if (comp.getState() == Component.STATE_ACTIVE) {
					activeConcentrator = true;
				}
			}
		}
		assertEquals(true, activeConcentrator);
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
	
	        assertNotNull(result);
		} catch (InvalidSyntaxException e) {
			fail(e.getMessage());
		}
		
		return result;
    }

    
//    public void testConfiguration()  {
////	//	super.setUp();
////		
////		//configAdminReference = context.getServiceReference(ConfigurationAdmin.class.getName());
////    	if (configAdminReference != null) {
////    		configAdmin = (ConfigurationAdmin) context.getService(configAdminReference);
////    	}
////    	assertNotNull(configAdminReference);
////    	
////    	scrServiceReference = context.getServiceReference( ScrService.class.getName() );
////    	if (scrServiceReference != null) {
////    		scrService = (ScrService) context.getService(scrServiceReference);
////    	}
//    	assertNotNull(scrServiceReference);
//    	assertNotNull(scrService);
//    }
//	
//	public void testAddAuctioneer() throws Exception {
//		Dictionary<String, Object> properties = getPropertiesAuctioneer();
//		String pid = "net.powermatcher.core.auctioneer.Auctioneer";
//		
//    	Configuration config = this.configAdmin.getConfiguration(pid, null);
//    	if (config != null) {
//			config.update(properties);
//		}
//    	
//    	// Verify there is one Auctioneer configuration present
//    	Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.core.auctioneer.Auctioneer)");
//    	assertEquals(1, configurations.length);
//    }
//
//	public void testAddConcentrator() throws Exception {
//		Dictionary<String, Object> properties = getPropertiesConcentrator();
//		String pid = "net.powermatcher.core.concentrator.Concentrator";
//		
//    	Configuration config = this.configAdmin.getConfiguration(pid, null);
//    	if (config != null) {
//			config.update(properties);
//		}
//    	
//    	// Verify there is one Concentrator configuration present
//    	Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.core.concentrator.Concentrator)");
//    	assertEquals(1, configurations.length);
//    }
//
//	public void testAddPvPanelA() throws Exception {
//		Dictionary<String, Object> properties = getPropertiesPvPanelA();
//		String pid = "net.powermatcher.examples.PVPanelAgent";
//		
//    	Configuration config = this.configAdmin.getConfiguration(pid, null);
//    	if (config != null) {
//			config.update(properties);
//		}
//    	
//    	// Verify there is one PvPanelAgent configuration present
//    	Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.examples.PVPanelAgent)");
//    	assertEquals(1, configurations.length);
//    }
//
//	public void testAddPvPanelB() throws Exception {
//		Dictionary<String, Object> properties = getPropertiesPvPanelB();
//		String pid = "net.powermatcher.examples.PVPanelAgent";
//		
//    	Configuration config = this.configAdmin.getConfiguration(pid, null);
//    	if (config != null) {
//			config.update(properties);
//		}
//    	
//    	// Verify there are two PvPanelAgent configurations present
//    	Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.examples.PVPanelAgent)");
//    	assertEquals(2, configurations.length);
//    }
//
//	public void testAddFreezer() throws Exception {
//		Dictionary<String, Object> properties = getPropertiesFreezer();
//		String pid = "net.powermatcher.examples.Freezer";
//		
//    	Configuration config = this.configAdmin.getConfiguration(pid, null);
//    	if (config != null) {
//			config.update(properties);
//		}
//    	
//    	// Verify there are two PvPanelAgent configurations present
//    	Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.examples.Freezer)");
//    	assertEquals(1, configurations.length);
//    }
//
//	public void testAuctioneerComponent() throws Exception {
//		Component[] components = scrService.getComponents("net.powermatcher.core.auctioneer.Auctioneer");
//		boolean foundAuctioneerComponent = false;
//		
//		for (Component comp : components) {
//			if (comp.getConfigurationPid().equals("net.powermatcher.core.auctioneer.Auctioneer")) {
//				foundAuctioneerComponent = true;
//			}
//		}
//		assertTrue(foundAuctioneerComponent);
//	}
//
//	public void testConcentratorComponent() throws Exception {
//		Component[] components = scrService.getComponents("net.powermatcher.core.concentrator.Concentrator");
//		boolean foundConcentratorComponent = false;
//		
//		for (Component comp : components) {
//			if (comp.getConfigurationPid().equals("net.powermatcher.core.concentrator.Concentrator")) {
//				foundConcentratorComponent = true;
//			}
//		}
//		assertTrue(foundConcentratorComponent);
//	}
//
//	public void testPvPanelComponents() throws Exception {
//		Component[] components = scrService.getComponents("net.powermatcher.examples.PVPanelAgent");
//		int numberPvPanels = 0;
//		
//		for (Component comp : components) {
//			if (comp.getConfigurationPid().equals("net.powermatcher.examples.PVPanelAgent")) {
//				numberPvPanels += numberPvPanels;
//			}
//		}
//		
//		// check there are 2 PvPanelAgents
//		assertEquals(2, numberPvPanels);
//	}
//
//	public void testFreezerComponent() throws Exception {
//		Component[] components = scrService.getComponents("net.powermatcher.examples.Freezer");
//		boolean foundFreezerComponent = false;
//		
//		for (Component comp : components) {
//			if (comp.getConfigurationPid().equals("net.powermatcher.examples.Freezer")) {
//				foundFreezerComponent = true;
//			}
//		}
//		assertTrue(foundFreezerComponent);
//	}
//
//	public void testAuctioneerActive() throws Exception {
//		Component[] components = scrService.getComponents("net.powermatcher.core.auctioneer.Auctioneer");
//		boolean activeAuctioneer = false;
//		
//		for (Component comp : components) {
//			if (comp.getConfigurationPid().equals("net.powermatcher.core.auctioneer.Auctioneer")) {
//				if (comp.getState() == Component.STATE_ACTIVE) {
//					activeAuctioneer = true;
//				}
//			}
//		}
//		assertEquals(true, activeAuctioneer);
//	}
//
//	public void testConcentratorActive() throws Exception {
//		Component[] components = scrService.getComponents("net.powermatcher.core.concentrator.Concentrator");
//		boolean activeConcentrator = false;
//		
//		for (Component comp : components) {
//			if (comp.getConfigurationPid().equals("net.powermatcher.core.concentrator.Concentrator")) {
//				if (comp.getState() == Component.STATE_ACTIVE) {
//					activeConcentrator = true;
//				}
//			}
//		}
//		assertEquals(true, activeConcentrator);
//	}
//
//	public void testPvPanelsActive() throws Exception {
//		Component[] components = scrService.getComponents("net.powermatcher.examples.PVPanelAgent");
//		int numberActivePvPanels = 0;
//		
//		for (Component comp : components) {
//			if (comp.getConfigurationPid().equals("net.powermatcher.examples.PVPanelAgent")) {
//				if (comp.getState() == Component.STATE_ACTIVE) {
//					numberActivePvPanels += numberActivePvPanels;
//				}
//			}
//		}
//		assertEquals(2, numberActivePvPanels);
//	}
//
//	public void testFreezerActive() throws Exception {
//		Component[] components = scrService.getComponents("net.powermatcher.examples.Freezer");
//		boolean activeFreezer = false;
//		
//		for (Component comp : components) {
//			if (comp.getConfigurationPid().equals("net.powermatcher.examples.Freezer")) {
//				if (comp.getState() == Component.STATE_ACTIVE) {
//					activeFreezer = true;
//				}
//			}
//		}
//		assertEquals(true, activeFreezer);
//	}
//
//	public void testDeleteConfFreezer() throws Exception {
//		String factoryPid = "net.powermatcher.examples.Freezer";
//		boolean deleteFreezerConfig = false;
//		
//		for (Configuration c : configAdmin.listConfigurations(null)) { 
//			if (factoryPid.equals(c.getFactoryPid())) {
//	            String agentId = (String) c.getProperties().get("agentId"); 
//				if (agentId.equals("freezer")) {
//					c.delete();
//					deleteFreezerConfig = true;
//				}
//			}
//		}
//		
//		assertEquals(true, deleteFreezerConfig);
//	}
//
//	public void testFreezerNotActive() throws Exception {
//		Component[] components = scrService.getComponents("net.powermatcher.examples.Freezer");
//		boolean activeFreezer = true;
//		
//		for (Component comp : components) {
//			if (comp.getConfigurationPid().equals("net.powermatcher.examples.Freezer")) {
//				if (comp.getState() == Component.STATE_UNSATISFIED) {
//					activeFreezer = false;
//				}
//			}
//		}
//		assertEquals(false, activeFreezer);
//	}
//
//	public void testActiveAgents() throws Exception {
//		Component[] components = scrService.getComponents("net.powermatcher.examples.Freezer");
//		int numberAgents = 0;
//		
//		for (Component comp : components) {
//				if (comp.getState() == Component.STATE_ACTIVE) {
//					numberAgents += numberAgents;
//				}
//		}
//		
//		// check cluster with active agents: Auctioneer, Concentrator, PvPanelA, PvPanelB
//		assertEquals(4, numberAgents);
//	}
//	
//	private Dictionary<String, Object> getPropertiesAuctioneer() throws Exception {
//    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
//    	
//    	String factoryPid = "net.powermatcher.core.auctioneer.Auctioneer";
//    	properties.put("agentId", "auctioneer");
//    	properties.put("clusterId", "DefaultCluster");
//    	properties.put("commodity", "electricity");
//    	properties.put("currency", "EUR");
//    	properties.put("priceSteps", "100");
//    	properties.put("maximumPrice", "1.0");
//    	properties.put("bidTimeout", "600");
//    	properties.put("priceUpdateRate", "30");
//    	properties.put("minimumPrice", "0.0");
//    	properties.put("component.name", factoryPid);
////    	properties.put("service.factoryPid", "net.powermatcher.core.auctioneer.Auctioneer");
////    	properties.put("service.pid", pid);
//    	this.configAdmin.createFactoryConfiguration(factoryPid);
//    	
//		return properties;
//	}
//
//	private Dictionary<String, Object> getPropertiesConcentrator() throws Exception {
//    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
//    	
//    	String factoryPid = "net.powermatcher.core.concentrator.Concentrator";
//    	properties.put("agentId", "concentrator");
//    	properties.put("desiredParentId", "auctioneer");
//    	properties.put("bidUpdateRate", "60");
////    	properties.put("component.id", "1");
////    	properties.put("component.name", factoryPid);
//
//    	this.configAdmin.createFactoryConfiguration(factoryPid);
//		
//		return properties;
//	}
//
//	private Dictionary<String, Object> getPropertiesPvPanelA() throws Exception {
//    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
//    	
//    	String factoryPid = "net.powermatcher.examples.PVPanelAgent";
//    	properties.put("agentId", "pvpanelA");
//    	properties.put("desiredParentId", "concentrator");
//    	properties.put("bidUpdateRate", "30");
//    	properties.put("minimumDemand", "-700");
//    	properties.put("maximumDemand", "-600");
//    	properties.put("component.name", factoryPid);
//    	
//    	this.configAdmin.createFactoryConfiguration(factoryPid);
//    	
//		return properties;
//	}
//	
//	private Dictionary<String, Object> getPropertiesPvPanelB() throws Exception  {
//    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
//    	
//    	String factoryPid = "net.powermatcher.examples.PVPanelAgent";
//    	properties.put("agentId", "pvpanelB");
//    	properties.put("desiredParentId", "concentrator");
//    	properties.put("bidUpdateRate", "30");
//    	properties.put("minimumDemand", "-700");
//    	properties.put("maximumDemand", "-600");
//    	properties.put("component.name", factoryPid);
//    	
//    	this.configAdmin.createFactoryConfiguration(factoryPid);
//    	
//		return properties;
//	}
//
//	private Dictionary<String, Object> getPropertiesFreezer() {
//    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
//    	
//    	String factoryPid = "net.powermatcher.examples.Freezer";
//    	properties.put("agentId", "freezer");
//    	properties.put("desiredParentId", "concentrator");
//    	properties.put("bidUpdateRate", "30");
//    	properties.put("minimumDemand", "100");
//    	properties.put("maximumDemand", "121");
//    	properties.put("component.name", factoryPid);
//    	
//		return properties;
//	}
}
