package net.powermatcher.test.osgi;

import java.rmi.activation.Activator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.test.helpers.TestClusterHelper;

import org.apache.felix.scr.Component;
import org.apache.felix.scr.ScrService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class AgentTest extends TestCase {

    private final BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

    private ConfigurationAdmin configAdmin;
    private ServiceReference<?> configAdminReference;
    private ScrService scrService;
    private ServiceReference<?> scrServiceReference;
    
    private ServiceTracker serviceTracker;  
    
    private ServiceRegistration registration;
    
   // private TestClusterHelper cluster;
    //private TestClusterHelper cluster;
    
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testAddAuctioneer() throws Exception {
		super.setUp();
		
		configAdminReference = context.getServiceReference(ConfigurationAdmin.class.getName());
    	if (configAdminReference != null) {
    		configAdmin = (ConfigurationAdmin) context.getService(configAdminReference);
    	}
    	assertNotNull(configAdmin);
    	
    	scrServiceReference = context.getServiceReference( ScrService.class.getName() );
    	if (scrServiceReference != null) {
    		scrService = (ScrService) context.getService(scrServiceReference);
    	}

    	
    	// context.registerService(Greeting.class.getName(), new GreetingImpl(), null);
    	
    	assertNotNull(scrService);
    	
    	// create Auctioneer props
    	UUID id = UUID.randomUUID();
    	String pid = "net.powermatcher.core.auctioneer.Auctioneer." + id;
    	String factoryPid = "net.powermatcher.core.auctioneer.Auctioneer";

    	Dictionary<String, Object> properties = new Hashtable<String, Object>();

    	properties.put("agentId", "auctioneer");
    	properties.put("clusterId", "DefaultCluster");
    	properties.put("commodity", "electricity");
    	properties.put("currency", "EUR");
    	properties.put("priceSteps", "100");
    	properties.put("maximumPrice", "1.0");
    	properties.put("bidTimeout", "600");
    	properties.put("priceUpdateRate", "30");
    	properties.put("minimumPrice", "0.0");
    	properties.put("component.id", "0");
    	properties.put("component.name", factoryPid);
    	properties.put("service.factoryPid", "net.powermatcher.core.auctioneer.Auctioneer");
    	properties.put("service.pid", pid);
    	
    	Configuration config = this.configAdmin.getConfiguration(pid, null);

    	if (config != null) {
			config.update(properties);
		}
    	assertNotNull(scrService);
    	this.configAdmin.createFactoryConfiguration(factoryPid);
    	
    	Auctioneer auctioneer = new Auctioneer();
    	context.registerService(Auctioneer.class.getName(), auctioneer, properties);
    	
    	serviceTracker = new ServiceTracker(context, Auctioneer.class.getName(), null);  
    	serviceTracker.open();
    	
    	registration = context.registerService(Auctioneer.class.getName(), auctioneer, properties);  
    	
    	Configuration configWithAuctioneer = configAdmin.getConfiguration("net.powermatcher.core.auctioneer");
    	//Configuration[] configurations = configAdmin.listConfigurations("(service.pid=net.powermatcher.core.auctioneer.Auctioneer)");
    	
    	Component[] components = scrService.getComponents();
    	
    	for (Component comp : components) {
    		
    		if (comp.getConfigurationPid().equals("net.powermatcher.core.auctioneer.Auctioneer")) {
    			comp.enable();
    			boolean activateDeclared = comp.isActivateDeclared();
    			String active = comp.getActivate();
    			System.out.println(comp.getConfigurationPid());
    		}
    	}
    	
    	Component[] componentsAuctioneer = scrService.getComponents("net.powermatcher.core.auctioneer.Auctioneer");
    	
    	for (Component comp : componentsAuctioneer) {
    		if (comp.getConfigurationPid().equals("net.powermatcher.core.auctioneer.Auctioneer")) {
    			comp.enable();
    			String active = comp.getActivate();
    			System.out.println(comp.getConfigurationPid());
    		}
    	}
    	
    	
    	
    	// Verify that there is exactly one Auctioneer configuration present
    	assertNotNull(configWithAuctioneer);
    	
//    	Auctioneer auctioneer = new Auctioneer();
//    	auctioneer.activate((Map<String, ?>) properties);
//    	MarketBasis marketBasis = new MarketBasis("electricity","EUR",100,0,1);
//    	TestClusterHelper cluster = new TestClusterHelper(marketBasis, auctioneer);
    	
    	
    }
	
	public void testAddAgent() throws Exception {
		super.setUp();
		
		configAdminReference = context.getServiceReference(ConfigurationAdmin.class.getName());
    	if (configAdminReference != null) {
    		configAdmin = (ConfigurationAdmin) context.getService(configAdminReference);
    	}
    	assertNotNull(configAdmin);
    	
    	scrServiceReference = context.getServiceReference( ScrService.class.getName() );
    	if (scrServiceReference != null) {
    		scrService = (ScrService) context.getService(scrServiceReference);
    	}
    	
    	assertNotNull(scrService);
	}
	
}
