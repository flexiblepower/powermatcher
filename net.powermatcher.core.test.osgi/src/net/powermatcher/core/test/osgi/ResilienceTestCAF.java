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

public class ResilienceTestCAF extends TestCase {

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

//	public void test_CAF1_Auctioneer_only() throws Exception {
//		// First load an empty configuration
//		createOrUpdateConfigManagerConfiguration("file:resources/config_CAF1_empty_config.xml");
//
//		// Check if the ConfigManager is present
//		assert (isConfigManagerComponentPresent());
//
//		// Verify that there is no Auctioneer configuration present
//		assert (!containsFactoryConfiguration(AUCTIONEER_AGENT_PID));
//
//		// Load configuration that adds an Auctioneer configuration
//		createOrUpdateConfigManagerConfiguration("file:resources/config_CAF1_add_auctioneer.xml");
//
//		// Verify that there is exactly one Auctioneer configuration present
//		Configuration[] configurations = listFactoryConfigurations(AUCTIONEER_AGENT_PID);
//		assert (configurations != null && configurations.length == 1);
//		if (configurations.length == 1) {
//			// Verify the properties
//			Configuration agentConfig = configurations[0];
//			Dictionary<?, ?> agentProps = agentConfig.getProperties();
//			assert (agentProps != null && agentProps.get("id") != null && agentProps
//					.get("id").equals("auctioneer"));
//			assert (agentProps != null
//					&& agentProps.get("update.interval") != null && agentProps
//					.get("update.interval").equals(30));
//		}
//
//		// Verify that the Auctioneer is active
//		Component[] components = getComponents(AUCTIONEER_AGENT_PID);
//		assert (checkComponentState(components, AUCTIONEER_AGENT_PID,
//				"auctioneer", Component.STATE_ACTIVE));
//	}
    
	public void test_CAF1_Auctioneer_only() throws Exception {
        // First load an empty configuration 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CAF1_empty_config.xml");

		// Check if the ConfigManager is present
    	Component[] components = scrService.getComponents("net.powermatcher.core.config.management.agent.ConfigManager");
    	assert(components != null && components.length >= 1 );
    			
    	// Verify that there is no Auctioneer configuration present
    	Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.core.agent.auctioneer.Auctioneer)");
    	assert(configurations == null || configurations.length == 0 );

    	    	
    	// Load configuration that adds an Auctioneer configuration
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CAF1_add_auctioneer.xml");
    	
    	// Verify that there is exactly one Auctioneer configuration present
    	configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.core.agent.auctioneer.Auctioneer)");
    	assert(configurations != null && configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("auctioneer"));
    		assert(agentProps != null && agentProps.get("update.interval") != null && agentProps.get("update.interval").equals(30));
    	}

    	// Verify that the Auctioneer is active
    	components = scrService.getComponents("net.powermatcher.core.agent.auctioneer.Auctioneer");
    	assert(components != null && components.length == 1 );
    	Component auctioneer = components[0];
    	assert(auctioneer.getState() == Component.STATE_ACTIVE);
    	
    	
    }
	
	public void test_CAF2_Add_Auctioneer() throws Exception {
        // First load an empty configuration 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CAF2_noauctioneer.xml");

		// Check if the ConfigManager is present
    	Component[] components = scrService.getComponents("net.powermatcher.core.config.management.agent.ConfigManager");
    	assert(components != null && components.length >= 1 );
    			
    	// Verify that there is no Auctioneer configuration present
    	Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.core.agent.auctioneer.Auctioneer)");
    	assert(configurations == null || configurations.length == 0 );

    	    	
    	// Load configuration that adds an Auctioneer configuration
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CAF2_agents_new_auctioneer.xml");
    	
    	// Verify that there is exactly one Auctioneer configuration present
    	configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.core.agent.auctioneer.Auctioneer)");
    	assert(configurations != null && configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("auctioneer"));
    		assert(agentProps != null && agentProps.get("update.interval") != null && agentProps.get("update.interval").equals(30));
    	}

    	// Verify that the Auctioneer is active
    	components = scrService.getComponents("net.powermatcher.core.agent.auctioneer.Auctioneer");
    	assert(components != null && components.length == 1 );
    	Component auctioneer = components[0];
    	assert(auctioneer.getState() == Component.STATE_ACTIVE);
    	
    	
    }
	
	public void test_CAF3_Update_Auctioneer() throws Exception {
        // First load an empty configuration 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CAF3_init.xml");

		// Check if the ConfigManager is present
    	Component[] components = scrService.getComponents("net.powermatcher.core.config.management.agent.ConfigManager");
    	assert(components != null && components.length >= 1 );
    			
    	// Verify that there is no Auctioneer configuration present
    	Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.core.agent.auctioneer.Auctioneer)");
    	assert(configurations == null || configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("auctioneer"));
    		assert(agentProps != null && agentProps.get("update.interval") != null && agentProps.get("update.interval").equals(30));
    		assert(agentProps != null && agentProps.get("bid.expiration.time") != null && agentProps.get("bid.expiration.time").equals(300));
    	}
    	    	
    	// Load configuration that adds an Auctioneer configuration
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CAF3_update_auctioneer.xml");
    	
    	// Verify that there is exactly one Auctioneer configuration present
    	configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.core.agent.auctioneer.Auctioneer)");
    	assert(configurations != null && configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("auctioneer"));
    		assert(agentProps != null && agentProps.get("update.interval") != null && agentProps.get("update.interval").equals(35));
    		assert(agentProps != null && agentProps.get("bid.expiration.time") != null && agentProps.get("bid.expiration.time").equals(500));
    	}

    	// Verify that the Auctioneer is active
    	components = scrService.getComponents("net.powermatcher.core.agent.auctioneer.Auctioneer");
    	assert(components != null && components.length == 1 );
    	Component auctioneer = components[0];
    	assert(auctioneer.getState() == Component.STATE_ACTIVE);
    	
    	
    }
	
	public void test_CAF4_Remove_Auctioneer() throws Exception {
        // First load an empty configuration 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CAF4_init.xml");

		// Check if the ConfigManager is present
    	Component[] components = scrService.getComponents("net.powermatcher.core.config.management.agent.ConfigManager");
    	assert(components != null && components.length >= 1 );
    			
    	// Verify that there is one Auctioneer configuration present
    	Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.core.agent.auctioneer.Auctioneer)");
    	assert(configurations != null && configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("auctioneer"));
    		assert(agentProps != null && agentProps.get("update.interval") != null && agentProps.get("update.interval").equals(30));
    		assert(agentProps != null && agentProps.get("bid.expiration.time") != null && agentProps.get("bid.expiration.time").equals(300));
    	}
    	
    	// Verify that the Auctioneer is active
    	components = scrService.getComponents("net.powermatcher.core.agent.auctioneer.Auctioneer");
    	assert(checkComponentState(components, "net.powermatcher.core.agent.auctioneer.Auctioneer", Component.STATE_ACTIVE)); 
    	
    	// Load configuration that where the Auctioneer configuration is removed
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CAF4_auctioneer_removed.xml");
    	
    	// Verify that there no Auctioneer configuration present
    	configurations = configAdmin.listConfigurations("(service.factoryPid=net.powermatcher.core.agent.auctioneer.Auctioneer)");
    	assert(configurations == null || configurations.length == 0 );
    	    		
    	// Verify that there is no Auctioneer component is not active but 'unsatisfied' and others are active.
    	components = scrService.getComponents();
    	assert(checkComponentState(components, "net.powermatcher.core.agent.auctioneer.Auctioneer", Component.STATE_UNSATISFIED));
    	assert(checkComponentState(components, "net.powermatcher.core.agent.concentrator.Concentrator", Component.STATE_ACTIVE));
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
