package net.powermatcher.test.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.test.helpers.PropertieBuilder;
import net.powermatcher.test.helpers.TestClusterHelper;

import org.apache.felix.scr.ScrService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.apache.felix.scr.Component;

public class AgentTest extends TestCase {

    private final BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

    private ConfigurationAdmin configAdmin;
    private ServiceReference<?> configAdminReference;
    private ScrService scrService;
    private ServiceReference<?> scrServiceReference;
    
    //private TestClusterHelper cluster;
    
	public void testAuctioneer() throws Exception {
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
    	
    	// Check if the ConfigManager is present
    	//Component[] components = scrService.getComponents("net.powermatcher.core.auctioneer.Auctioneer");
    	Component[] components = scrService.getComponents();
    	
    	// create Auctioneer props
    	String pid = "net.powermatcher.core.auctioneer";
    	String factoryPid = "net.powermatcher.core.auctioneer.Auctioneer";
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("agentId", "auctioneer");
    	properties.put("clusterId", "DefaultCluster");
    	properties.put("commodity", "electricity");
    	properties.put("currency", "EUR");
    	properties.put("priceSteps", "100");
    	properties.put("maximumPrice", "1");
    	properties.put("bidTimeout", "600");
    	properties.put("priceUpdateRate", "30");
    	
    	Configuration config = this.configAdmin.getConfiguration(pid, null);
    	this.configAdmin.createFactoryConfiguration(factoryPid);

    	if (config != null) {
			config.update(properties);
		}
    	
    	Configuration configWithAuctioneer = configAdmin.getConfiguration("net.powermatcher.core.auctioneer");
    	//Configuration[] configurations = configAdmin.listConfigurations("(service.pid=net.powermatcher.core.auctioneer.Auctioneer)");
    	
    	components = scrService.getComponents("net.powermatcher.core.auctioneer.Auctioneer");
    	// Verify that there is exactly one Auctioneer configuration present
    	assertNotNull(configWithAuctioneer);
    	
    	MarketBasis marketbasis = new MarketBasis("electricity", "EUR", 11, 0, 10);
    	// bij toevoegen hiervan, een wiring exception
    	Auctioneer auctioneer = new Auctioneer();
        auctioneer.activate(new PropertieBuilder().agentId("auctioneer")
                                                  .clusterId("DefaultCluster")
                                                  .priceUpdateRate(30)
                                                  .marketBasis(marketbasis)
                                                  .build());
        
//        
    }
	
}
