package net.powermatcher.core.test.osgi;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.apache.felix.scr.Component;
import org.apache.felix.scr.ScrService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Implements the resilience test cases for the Communication Framework testing
 * of the Concentrator. The test scenarios focus on adding, updating and removing
 * concentrator configurations.
 */
public class ResilienceTestCCF extends TestCase {

	private final BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
    private ServiceReference configAdminReference;
    private ConfigurationAdmin configAdmin;
    private ServiceReference scrServiceReference; 
    private ScrService scrService;
    
    @Override
	protected void setUp() throws Exception {
		super.setUp();
		
		configAdminReference = context.getServiceReference(ConfigurationAdmin.class.getName());
    	if (configAdminReference != null) {
    		configAdmin = (ConfigurationAdmin) context.getService(configAdminReference);
    	}
    	assert(configAdmin != null);
    	
    	// Wait for the processing to initialize
	    Thread.sleep(5000);
    	
    	scrServiceReference = context.getServiceReference( ScrService.class.getName() );
    	if (scrServiceReference != null) {
    		scrService = (ScrService) context.getService(scrServiceReference);
    	}
    	assert(scrService != null);
	}

    
	/**
	 * Starts with an initial empty configuration (no agents) and
	 * then adds the configuration for a single concentrator.
	 * 
	 * Expected behaviour: Concentrator component is instantiated and
	 * activated.
	 * 
	 * @throws Exception
	 */
	public void test_CCF1_Concentrator_only() throws Exception {
        // First load an empty configuration 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CCF1_init.xml");

		// Check if the ConfigManager is present
		assert(isConfigManagerComponentPresent());
    			
    	// Verify that there is no Concentrator configuration present
    	Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.core.agent.concentrator.Concentrator)");
    	assert(configurations == null || configurations.length == 0 );

    	    	
    	// Load configuration that adds an Auctioneer configuration
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CCF1_add_concentrator.xml");
    	
    	// Verify that there is exactly one Concentrator configuration present
    	configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.core.agent.concentrator.Concentrator)");
    	assert(configurations != null && configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("concentrator"));
    		assert(agentProps != null && agentProps.get("log.listener.id") != null && agentProps.get("log.listener.id").equals("csvlogging"));
    	}
    	
    	// Check that the Concentrator is active
    	Component[] components = scrService.getComponents();
    	assert(checkComponentState(components, "net.powermatcher.core.agent.concentrator.Concentrator", Component.STATE_ACTIVE));
    	
    }
	
	/**
	 * Starts with an initial configuration that defines an Auctioneer,
	 * CSVLoggingAgent and a TestAgent. Then a configuration for a single concentrator
	 * is added.
	 * 
	 * Expected behaviour: (1) Auctioneer, CSVLoggingAgent and TestAgent are
	 * active but exchange not price and bid information. (2)After creating the
	 * concentrator configuration a Concentrator is active. (3) The agents
	 * exchange their information and the bids and price information is logged. 
	 * 
	 * 
	 * @throws Exception
	 */
	public void test_CCF2_Add_Concentrator() throws Exception {
		// First initial configuration: Auctioneer, CSVLoggingAgent and TestAgent. 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CCF2_all_agents_no_concentrator.xml");

		// Check if the ConfigManager is present
    	assert(isConfigManagerComponentPresent());
    			
    	// Verify that there is no Concentrator configuration present
    	Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.core.agent.concentrator.Concentrator)");
    	assert(configurations == null || configurations.length == 0 );

    	    	
    	// Load configuration that adds a Concentrator configuration
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CCF2_all_agents_add_concentrator.xml");
    	
    	// Verify that there is exactly one Concentrator configuration present
    	configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.core.agent.concentrator.Concentrator)");
    	assert(configurations != null && configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("concentrator"));
    		assert(agentProps != null && agentProps.get("update.interval") != null && agentProps.get("update.interval").equals(30));
    	}
    	
    	// Check that the Concentrator and other components are running
    	Component[] components = scrService.getComponents();
    	assert(checkComponentState(components, "net.powermatcher.core.agent.concentrator.Concentrator", Component.STATE_ACTIVE));
    	assert(checkComponentState(components, "net.powermatcher.core.agent.auctioneer.Auctioneer", Component.STATE_ACTIVE));
    	assert(checkComponentState(components, "net.powermatcher.core.agent.test.TestAgent", Component.STATE_ACTIVE));
    	assert(checkComponentState(components, "net.powermatcher.core.agent.logging.CSVLoggingAgent", Component.STATE_ACTIVE));
    	
    }
	
	/**
	 * Starts with an initial configuration that defines an Auctioneer, Concentrator,
	 * CSVLoggingAgent and a TestAgent. Then some configuration properties for
	 * the concentrator are updated.
	 * 
	 * Expected behaviour: (1) Auctioneer, Concentrator, CSVLoggingAgent and TestAgent are
	 * active and exchange price and bid information. (2) After processing the
	 * configuration update the concentrator properties are updated (3) The
	 * agents remain active after the update. 
	 * 
	 * @throws Exception
	 */
	public void test_CCF3_Update_Concentrator() throws Exception {
        // First initial configuration: Auctioneer, Concentrator, CSVLoggingAgent and TestAgent. 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CCF3_init.xml");

		// Check if the ConfigManager is present
    	assert(isConfigManagerComponentPresent());
    			
    	// Verify that there is one Concentrator configuration present with the
    	// correct property values as defined in the xml
    	Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.core.agent.concentrator.Concentrator)");
    	assert(configurations == null || configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("concentrator"));
    		assert(agentProps != null && agentProps.get("update.interval") != null && agentProps.get("update.interval").equals(30));
    		assert(agentProps != null && agentProps.get("bid.expiration.time") != null && agentProps.get("bid.expiration.time").equals(300));
    	}
    	    	
    	// Load configuration that updates the Concentrator configuration
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CCF3_update_concentrator.xml");
    	
    	// Verify that there is exactly one Concentrator configuration present that
    	// contains the updated property values.
    	configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.core.agent.concentrator.Concentrator)");
    	assert(configurations != null && configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("concentrator"));
    		assert(agentProps != null && agentProps.get("update.interval") != null && agentProps.get("update.interval").equals(35));
    		assert(agentProps != null && agentProps.get("bid.expiration.time") != null && agentProps.get("bid.expiration.time").equals(500));
    	}
    	
    	// Check that the components are running
    	Component[] components = scrService.getComponents();
    	assert(checkComponentState(components, "net.powermatcher.core.agent.concentrator.Concentrator", Component.STATE_ACTIVE));
    	assert(checkComponentState(components, "net.powermatcher.core.agent.auctioneer.Auctioneer", Component.STATE_ACTIVE));
    	assert(checkComponentState(components, "net.powermatcher.core.agent.test.TestAgent", Component.STATE_ACTIVE));
    	assert(checkComponentState(components, "net.powermatcher.core.agent.logging.CSVLoggingAgent", Component.STATE_ACTIVE));
    	
    }
	
	/**
	 * Starts with an initial configuration that defines an Auctioneer, Concentrator,
	 * CSVLoggingAgent and a TestAgent. Then some configuration properties for
	 * the concentrator are updated.
	 * 
	 * Expected behaviour: (1) Auctioneer, Concentrator, CSVLoggingAgent and TestAgent are
	 * active and exchange price and bid information. (2) After processing the
	 * configuration update the concentrator confiugration is removed and the component
	 * is stopped (state is 'unsatisfied'). (3) The agents stop exchanging their bid and price
	 * information.
	 * 
	 * @throws Exception
	 */
	public void test_CCF4_Remove_Concentrator() throws Exception {
        // First initial configuration: Auctioneer, Concentrator, CSVLoggingAgent and TestAgent. 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CCF4_init.xml");

		// Check if the ConfigManager is present
    	assert(isConfigManagerComponentPresent());
    			
    	// Verify that there is exactly one Concentrator configuration present that
    	// contains the updated property values.
    	Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.core.agent.concentrator.Concentrator)");
    	assert(configurations != null && configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("concentrator"));
    		assert(agentProps != null && agentProps.get("update.interval") != null && agentProps.get("update.interval").equals(35));
    		assert(agentProps != null && agentProps.get("bid.expiration.time") != null && agentProps.get("bid.expiration.time").equals(500));
    	}
    	
    	// Check that the Concentrator and the other agent components are running
    	Component[] components = scrService.getComponents();
    	assert(checkComponentState(components, "net.powermatcher.core.agent.concentrator.Concentrator", Component.STATE_ACTIVE));
    	assert(checkComponentState(components, "net.powermatcher.core.agent.auctioneer.Auctioneer", Component.STATE_ACTIVE));
    	assert(checkComponentState(components, "net.powermatcher.core.agent.test.TestAgent", Component.STATE_ACTIVE));
    	assert(checkComponentState(components, "net.powermatcher.core.agent.logging.CSVLoggingAgent", Component.STATE_ACTIVE));
    	
    	   	    	
    	// Load configuration that where the Auctioneer configuration is removed
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CCF4_concentrator_removed.xml");
    	
    	// Verify that there no Concentrator configuration present
    	configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.core.agent.concentrator.Concentrator)");
    	assert(configurations == null || configurations.length == 0 );
    	
    	// Verify that there is no Concentrator component running, (state is is not active but 'unsatisfied')
    	// and the other agent components remain active.
    	components = scrService.getComponents();
    	assert(checkComponentState(components, "net.powermatcher.core.agent.concentrator.Concentrator", Component.STATE_UNSATISFIED));
    	assert(checkComponentState(components, "net.powermatcher.core.agent.auctioneer.Auctioneer", Component.STATE_ACTIVE));
    	assert(checkComponentState(components, "net.powermatcher.core.agent.test.TestAgent", Component.STATE_ACTIVE));
    	assert(checkComponentState(components, "net.powermatcher.core.agent.logging.CSVLoggingAgent", Component.STATE_ACTIVE));
    	
    }
	
	private boolean checkComponentState(Component[] components, String pid, int state) {
		if (components != null ) {
			for (Component c : components) {
				if (c.getName().equals(pid) && c.getState() == state) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	/**
	 * Verifies if a valid config manager component is running.
	 * 
	 * @return Returns true if there is exactly one config manager component running, otherwise false;
	 */
	private boolean isConfigManagerComponentPresent() {
		// Check if the ConfigManager is present
    	Component[] components = scrService.getComponents("net.powermatcher.core.config.management.agent.ConfigManager");
    	if (components != null && components.length == 1 && components[0].getState() == Component.STATE_ACTIVE) {
    		return true;
    	}
    	return false;
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
    	context.ungetService(configAdminReference);
    	context.ungetService(scrServiceReference);
	}
	
	private Configuration createOrUpdateConfigManagerConfiguration(final String configFile) throws IOException, InterruptedException {
		String pid = "net.powermatcher.core.config.management.agent.ConfigManager";    	
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();

    	properties.put("configuration.data.url", configFile);
    	properties.put("update.interval", 5);
    	
		Configuration config = this.configAdmin.getConfiguration(pid, null);
		if (config != null) {
			config.update(properties);
			
			// Wait for the processing by the ConfigManager
	    	Thread.sleep(10000);
	    
	    	properties.put("update.interval", 300);
	    	config.update(properties);
		}
    	
		return config;
	}
}
