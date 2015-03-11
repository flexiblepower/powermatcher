package net.powermatcher.test.osgi;

import junit.framework.TestCase;
import net.powermatcher.core.auctioneer.Auctioneer;

import org.apache.felix.scr.ScrService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.apache.felix.scr.Component;

public class AgentTest extends TestCase {

    private final BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

    private ConfigurationAdmin configAdmin;
    private ServiceReference<?> configAdminReference;
    private ScrService scrService;
    private ServiceReference scrServiceReference; 
    
	public void testExample() throws Exception {
		super.setUp();
		
		configAdminReference = context.getServiceReference(ConfigurationAdmin.class.getName());
    	if (configAdminReference != null) {
    		configAdmin = (ConfigurationAdmin) context.getService(configAdminReference);
    	}
    	assert(configAdmin != null);
    	assertNotNull(configAdmin);
    	
    	scrServiceReference = context.getServiceReference( ScrService.class.getName() );
    	if (scrServiceReference != null) {
    		scrService = (ScrService) context.getService(scrServiceReference);
    	}

    	assertNotNull(scrService);
    	
    	// Check if the ConfigManager is present
    	Component[] components = scrService.getComponents("net.powermatcher.core.config.management.agent.ConfigManager");
    }
	
}
