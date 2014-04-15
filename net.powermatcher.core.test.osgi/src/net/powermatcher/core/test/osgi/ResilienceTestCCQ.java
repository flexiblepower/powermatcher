package net.powermatcher.core.test.osgi;

import java.util.Dictionary;

import net.powermatcher.core.test.util.CSVLogReader;

import org.apache.felix.scr.Component;
import org.osgi.service.cm.Configuration;

/**
 * Resilience test CCQ containing scenarios  for Adding, Updating or Removing 
 * Concentrators. Tests are part of the Quality tests for the within the 
 * Communication Framework Testing catgegory. 
 *
 */
public class ResilienceTestCCQ extends ResilienceTestOSGi {
   
    	
	/**
	 * Add an concentrator to a cluster with a concentrator with the same identifier already present.
	 * <p>
	 * Test starts with an initial configuration that defines an Auctioneer, Concentrator,
	 * CSVLoggingAgent and a TestAgent. Then an identical configuration xml that 
	 * includes a second concentrator with the same id is loaded.
	 * <p>
	 * Expected behaviour: (1) Adding second concentrator fails. (2) The
	 * second auctioneer is not active and cluster remains working correctly.
	 *
	 * @throws Exception
	 */
	public void test_CCQ1_Add_Duplicate_Concentrator() throws Exception {
		// First initial configuration: Auctioneer, Concentrator, CSVLoggingAgent and TestAgent. 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CCQ1_init.xml");

		// Check if the ConfigManager is present
		assert(isConfigManagerComponentPresent());
    			
		// Verify that there is an Concentrator configuration present
    	Configuration[] configurations = listFactoryConfigurations(CONCENTRATOR_AGENT_PID);
    	assert(configurations == null || configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("concentrator"));
    	}
    	
    	// Verify that the Concentrator is active
    	Component[] components = getComponents(CONCENTRATOR_AGENT_PID);
    	assert(components != null && components.length == 1 );
    	assert(checkComponentState(components, CONCENTRATOR_AGENT_PID, "concentrator", Component.STATE_ACTIVE));
    	    	
    	// Load configuration that adds a duplicate Concentrator configuration 
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CCQ1_add_duplicate_concentrator.xml");
    	
    	// Verify that there are two Concentrator configurations present
    	configurations = listFactoryConfigurations(CONCENTRATOR_AGENT_PID);
    	assert(configurations != null && configurations.length == 2 );

    	// Verify that not both concentrators are active.
    	components = getComponents(CONCENTRATOR_AGENT_PID);
    	assert(components != null && components.length == 2 );
    	for (Component c : components) {
    		// Verify the concentrators have the same id
			assert( c.getProperties() != null && c.getProperties().get("id") != null 
					&& c.getProperties().get("id").equals("concentrator"));
		}
    	assert(components[0].getState() != Component.STATE_ACTIVE || components[1].getState() != Component.STATE_ACTIVE);
       	
    	
    	// Let the agents exchange some bids and price info
    	Thread.sleep(30000);
    }
	
	/**
	 * Update a concentrator with bad configuration data (i.e. configuration data containing 
	 * parameters which are not inside allowed limits).
	 * <p>
	 * Starts with an initial configuration that defines an Auctioneer, Concentrator,
	 * CSVLoggingAgent and a TestAgent. Then an identical configuration xml where 
	 * some properties of the concentrator are changed with invalid data.
	 * <p>
	 * Expected behaviour: (1) The updated values can bee seen in the configuration of the concentrator.
	 * (2) the concentrator does not use the invalid data and falls back to its default values. 
	 *
	 * @throws Exception
	 */
	public void test_CCQ2_Invalid_Update_Concentrator() throws Exception {
		// First initial configuration: Auctioneer, Concentrator, CSVLoggingAgent and TestAgent. 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CCQ2_init.xml");

		// Check if the ConfigManager is present
		assert(isConfigManagerComponentPresent());
    			
    	// Verify that there is an Concentrator configuration present
    	Configuration[] configurations = listFactoryConfigurations(CONCENTRATOR_AGENT_PID);
    	assert(configurations == null || configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("concentrator"));
    	}
    	    	
    	// Load configuration that updates the Concentrator configuration with invalid values
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CCQ2_invalid_update_concentrator.xml");
    	
    	// Verify that there is exactly one Concentrator configuration present
    	configurations = listFactoryConfigurations(CONCENTRATOR_AGENT_PID);
    	assert(configurations != null && configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("concentrator"));
    		assert(agentProps != null && agentProps.get("update.interval") != null && agentProps.get("update.interval").equals(-35));
    		assert(agentProps != null && agentProps.get("bid.expiration.time") != null && agentProps.get("bid.expiration.time").equals(-500));
    	}

    	// Verify that the Concentrator is active
    	Component[] components = getComponents(CONCENTRATOR_AGENT_PID);
    	assert(components != null && components.length == 1 );
    	assert(checkComponentState(components, CONCENTRATOR_AGENT_PID, "concentrator", Component.STATE_ACTIVE));
    	
    	// Verify the component properties
    	Component concentrator = getComponent(CONCENTRATOR_AGENT_PID, "concentrator");
    	assert(concentrator != null);
    	Dictionary<?, ?> props = concentrator.getProperties();
    	assert(props != null && props.get("update.interval") != null && !props.get("update.interval").equals(-35));
		assert(props != null && props.get("bid.expiration.time") != null && !props.get("bid.expiration.time").equals(-500));
		
    	// Let the agents exchange some bids and price info
    	Thread.sleep(40000);

    }
	
	
	/**
	 * Ungracefully remove a concentrator from a cluster.
	 * <p>
	 * Starts with an initial configuration that defines an Auctioneer, Concentrator,
	 * CSVLoggingAgent and a TestAgent. Then the concentrator is 'ungracefully' removed
	 * by removing the configuration. 
	 * <p>
	 * Expected behaviour: (1) The cluster is operational and functions without any
	 * problems. (2) After the update the concentrator configuration is removed and
	 * concentrator component is not active. (3) The test agent is not publishing bids,
	 * only the auctioneer is logging last aggregated bid.
	 *
	 * @throws Exception
	 */
	public void test_CCQ3_Ungracefully_Remove_Concentrator() throws Exception {
		
		// First initial configuration: Auctioneer, Concentrator, CSVLoggingAgent and TestAgent. 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CCQ3_init.xml");

		// Check if the ConfigManager is present
		assert(isConfigManagerComponentPresent());
    			
    	// Verify that there is a Concentrator configuration present
    	Configuration[] configurations = listFactoryConfigurations(CONCENTRATOR_AGENT_PID);
    	assert(configurations != null && configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("concentrator"));
    	}
    	    	
    	// Let the agents exchange some bids and price info
    	Thread.sleep(60000);   	
    	
    	// Load configuration that adds an Concentrator configuration
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CCQ3_ungracefully_remove_concentrator.xml");
    	
    	// Verify that there is no concentrator configuration present
    	configurations = listFactoryConfigurations(CONCENTRATOR_AGENT_PID);
    	assert(configurations == null || configurations.length == 0 );

    	// Verify that the Concentrator component is not active (unsatisfied).
    	Component[] components = getComponents(CONCENTRATOR_AGENT_PID);
    	assert(components != null && components.length == 1 );
    	assert(components[0].getState() == Component.STATE_UNSATISFIED);
		
    	// Let the agents exchange some bids and price info
    	Thread.sleep(60000);
    	    	
    	// Check log files. After the update only the auctioneer is logging price info.
    	String priceLogFile = getPriceLogFileName("CCQ3_POST");
    	assert(!CSVLogReader.containsLogLinesForToken(priceLogFile, "concentrator"));
    	assert(CSVLogReader.containsLogLinesForToken(priceLogFile, "auctioneer"));
    	
    	// After the update the agents (testagent1, concentrator) will not
    	// log any published bids.
    	String bidLogFile = getBidLogFileName("CCQ3_POST");
    	assert(!CSVLogReader.containsLogLinesForToken(bidLogFile, "concentrator"));
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "auctioneer"));
    	assert(!CSVLogReader.containsLogLinesForToken(bidLogFile, "testagent1"));
    	
    }
	
	/**
	 * Ungracefully remove a concentrator from a cluster, and after a set time, 
	 * re-add a concentrator to the cluster.
	 * <p>
	 * Starts with an initial configuration that defines an Auctioneer, Concentrator,
	 * CSVLoggingAgent and a TestAgent. Then the concentrator is 'ungracefully' removed
	 * by removing the configuration. A third version of the configuration file will
	 * restore the concentrator configuration. 
	 * <p>
	 * Expected behaviour: (1) The cluster is operational and functions without any
	 * problems. (2) After the update the concentrator configuration is removed and
	 * concentrator component is not active. (3) The test agent is not publishing bids,
	 * only the auctioneer is logging last aggregated bid. (4) The second update
	 * restores the situation and the cluster works fine again. 
	 *
	 * @throws Exception
	 */
	public void test_CAQ4_ReAddConcentratorAfterUngracefulRemoval() throws Exception {
		
		// First initial configuration: Auctioneer, Concentrator, CSVLoggingAgent and TestAgent. 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CCQ4_init.xml");

		// Check if the ConfigManager is present
		assert(isConfigManagerComponentPresent());
    			
    	// Verify that there is an Concentrator configuration present
    	Configuration[] configurations = listFactoryConfigurations(CONCENTRATOR_AGENT_PID);
    	assert(configurations == null || configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("concentrator"));
    	}
    	    	
    	// Let the agents exchange some bids and price info
    	Thread.sleep(60000);   	
    	
    	// Load configuration that removes the concentrator configuration
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CCQ4_ungracefully_remove_concentrator.xml");
    	
    	// Verify that there is no concentrator configuration present
    	configurations = listFactoryConfigurations(CONCENTRATOR_AGENT_PID);
    	assert(configurations == null || configurations.length == 0 );

    	// Verify that the concentrator component is unsatisfied
    	Component[] components = getComponents(CONCENTRATOR_AGENT_PID);
    	assert(components != null && components.length == 1 );
    	assert(components[0].getState() == Component.STATE_UNSATISFIED);
    	
		
    	// Let the agents exchange some bids and price info
    	Thread.sleep(60000);
    	
    	// Check log files. After the update only the auctioneer is logging price info.
    	String priceLogFile = getPriceLogFileName("CCQ4_POST");
    	assert(!CSVLogReader.containsLogLinesForToken(priceLogFile, "concentrator"));
    	assert(CSVLogReader.containsLogLinesForToken(priceLogFile, "auctioneer"));
    	
    	// After the update the agents (testagent1, concentrator) will not
    	// log any published bids.
    	String bidLogFile = getBidLogFileName("CCQ4_POST");
    	assert(!CSVLogReader.containsLogLinesForToken(bidLogFile, "concentrator"));
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "auctioneer"));
    	assert(!CSVLogReader.containsLogLinesForToken(bidLogFile, "testagent1"));
    	

    	// Load configuration that adds an Concentrator configuration
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CCQ4_re_add_concentrator.xml");
    	
    	// Verify that there is exactly no concentrator configuration present
    	configurations = listFactoryConfigurations(CONCENTRATOR_AGENT_PID);
    	assert(configurations != null && configurations.length == 1 );

    	// Verify that the concentrator is active
    	components = getComponents(CONCENTRATOR_AGENT_PID);
    	assert(components != null && components.length == 1 );
    	assert(checkComponentState(components, CONCENTRATOR_AGENT_PID, "concentrator", Component.STATE_ACTIVE));    	
		
    	// Let the agents exchange some bids and price info
    	Thread.sleep(60000);
    	
    	// Check log files. After the update only the auctioneer is logging price info.
    	priceLogFile = getPriceLogFileName("CCQ4_POST2");
    	assert(CSVLogReader.containsLogLinesForToken(priceLogFile, "concentrator"));
    	assert(CSVLogReader.containsLogLinesForToken(priceLogFile, "auctioneer"));
    	
    	// After the update the agents (testagent1, concentrator) will not
    	// log any published bids.
    	bidLogFile = getBidLogFileName("CCQ4_POST2");
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "concentrator"));
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "auctioneer"));
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "testagent1"));
    	
    }
	
	// TODO: CCQ5
}
