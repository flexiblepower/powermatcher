package net.powermatcher.core.test.osgi;

import java.util.Dictionary;

import net.powermatcher.core.test.util.CSVLogReader;

import org.apache.felix.scr.Component;
import org.osgi.service.cm.Configuration;

/**
 * Resilience test CCQ containing scenarios  for Adding, Updating or Removing 
 * an Auctioneer. Tests are part of the Quality tests for the within the 
 * Communication Framework Testing catgegory. 
 *
 */
public class ResilienceTestCAQ extends ResilienceTestOSGi {
   

    	
	/**
	 * Add an auctioneer to a cluster with an auctioneer (with or without the same 
	 * identifier) already present.
	 * <p>
	 * Starts with an initial configuration that defines an Auctioneer, Concentrator,
	 * CSVLoggingAgent and a TestAgent. Then an identical configuration xml that 
	 * includes an additional second auctioneer is loaded.
	 * <p>
	 * Expected behaviour: There should only be one auctioneer active in a cluster.  
	 *
	 * @throws Exception
	 */
	public void test_CAQ1_Add_second_Auctioneer_only() throws Exception {
		// First initial configuration: Auctioneer, Concentrator, CSVLoggingAgent and TestAgent. 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CAQ1_init.xml");

		// Check if the ConfigManager is present
		assert(isConfigManagerComponentPresent());
    			
    	// Verify that there is an Auctioneer configuration present
    	assert(containsFactoryConfiguration(AUCTIONEER_AGENT_PID));
    	    	
    	// Load configuration that adds an additional Auctioneer configuration
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CAQ1_add_2nd_auctioneer.xml");
    	
    	// Verify that there are two Auctioneer configuration present
    	Configuration[] configurations = listFactoryConfigurations(AUCTIONEER_AGENT_PID);
    	assert(configurations != null && configurations.length == 2 );
    	if (configurations.length == 2) {
    		// Verify the properties
    		Configuration agentConfig = findConfiguration(configurations, AUCTIONEER_AGENT_PID, "auctioneer");
    		assert(agentConfig != null);
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("update.interval") != null && agentProps.get("update.interval").equals(30));

    		// Verify the properties
    		agentConfig = findConfiguration(configurations, AUCTIONEER_AGENT_PID, "auctioneer2");
    		assert(agentConfig != null);
    		agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("update.interval") != null && agentProps.get("update.interval").equals(30));
    	}

    	// Verify that both "auctioneer" and "auctioneer2" are active.
    	Component[] components = getComponents(AUCTIONEER_AGENT_PID);
    	assert(checkComponentState(components, AUCTIONEER_AGENT_PID, "auctioneer", Component.STATE_ACTIVE) );    	
    	assert(!checkComponentState(components, AUCTIONEER_AGENT_PID, "auctioneer2", Component.STATE_ACTIVE) );
    	
    	
    	// Let the agents exchange some bids and price info
    	Thread.sleep(30000);
    }
	
	/**
	 * Update an auctioneer with bad configuration data (i.e. configuration data containing 
	 * parameters which are not inside allowed limits).
	 * <p>
	 * Starts with an initial configuration that defines an Auctioneer, Concentrator,
	 * CSVLoggingAgent and a TestAgent. Then an identical configuration xml where 
	 * some properties of the auctioneer are changed with invalid data.
	 * <p>
	 * Expected behaviour: (1) The updated values can bee seen in the configuration of the auctioneer.
	 * the auctioneer does not use the invalid data and falls back to its default values. 
	 *
	 * @throws Exception
	 */
	public void test_CAQ2_Invalid_Update_Auctioneer() throws Exception {
		// First initial configuration: Auctioneer, Concentrator, CSVLoggingAgent and TestAgent. 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CAQ2_init.xml");

		// Check if the ConfigManager is present
		assert(isConfigManagerComponentPresent());
    			
    	// Verify that there is an Auctioneer configuration present
    	Configuration[] configurations = listFactoryConfigurations(AUCTIONEER_AGENT_PID);
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
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CAQ2_invalid_update_auctioneer.xml");
    	
    	// Verify that there is exactly one Auctioneer configuration present
    	configurations = listFactoryConfigurations(AUCTIONEER_AGENT_PID);
    	assert(configurations != null && configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("auctioneer"));
    		assert(agentProps != null && agentProps.get("update.interval") != null && agentProps.get("update.interval").equals(-35));
    		assert(agentProps != null && agentProps.get("bid.expiration.time") != null && agentProps.get("bid.expiration.time").equals(-500));
    	}

    	// Verify that the Auctioneer is active
    	Component[] components = getComponents(AUCTIONEER_AGENT_PID);
    	assert(components != null && components.length == 1 );
    	assert(checkComponentState(components, AUCTIONEER_AGENT_PID, "auctioneer", Component.STATE_ACTIVE));
    	
    	// Verify the component properties
    	Component auctioneer = getComponent(AUCTIONEER_AGENT_PID, "auctioneer");
    	assert(auctioneer != null);
    	Dictionary<?, ?> auctprops = auctioneer.getProperties();
    	assert(auctprops != null && auctprops.get("update.interval") != null && !auctprops.get("update.interval").equals(-35));
		assert(auctprops != null && auctprops.get("bid.expiration.time") != null && !auctprops.get("bid.expiration.time").equals(-500));    	
    }
	
	
	/**
	 * Ungracefully remove an auctioneer from a cluster.
	 * <p>
	 * Starts with an initial configuration that defines an Auctioneer, Concentrator,
	 * CSVLoggingAgent and a TestAgent. Then the auctioneer is 'ungracefully' removed
	 * by updating the auctioneer id with another id. Which 
	 * <p>
	 * Expected behaviour: (1) The creation of the second auctioneer fails. The
	 * second auctioneer is not active.  
	 *
	 * @throws Exception
	 */
	public void test_CAQ3_Ungracefully_Remove_Auctioneer() throws Exception {
		
		// First initial configuration: Auctioneer, Concentrator, CSVLoggingAgent and TestAgent. 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CAQ3_init.xml");

		// Check if the ConfigManager is present
		assert(isConfigManagerComponentPresent());
    			
    	// Verify that there is an Auctioneer configuration present
    	Configuration[] configurations = listFactoryConfigurations(AUCTIONEER_AGENT_PID);
    	assert(configurations == null || configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("auctioneer"));
    		assert(agentProps != null && agentProps.get("update.interval") != null && agentProps.get("update.interval").equals(30));
    		assert(agentProps != null && agentProps.get("bid.expiration.time") != null && agentProps.get("bid.expiration.time").equals(300));
    	}
    	    	
    	// Let the agents exchange some bids and price info
    	Thread.sleep(60000);   	
    	
    	// Load configuration that adds an Auctioneer configuration
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CAQ3_ungracefully_remove_auctioneer.xml");
    	
    	// Verify that there is exactly no Auctioneer configuration present
    	configurations = listFactoryConfigurations(AUCTIONEER_AGENT_PID);
    	assert(configurations == null || configurations.length == 0 );

    	// Verify that the Auctioneer component is unsatisfied
    	Component[] components = getComponents(AUCTIONEER_AGENT_PID);
    	assert(components != null && components.length == 1 );
    	assert(components[0].getState() == Component.STATE_UNSATISFIED);
    	
		
    	// Let the agents exchange some bids and price info
    	Thread.sleep(60000);
    	    	
       	// Check log files. After the update only the auctioneer is logging price info.
    	String priceLogFile = getPriceLogFileName("CAQ3_POST");
    	assert(!CSVLogReader.containsLogLines(priceLogFile));
    	
    	// After the update the agents (testagent1, concentrator) will not
    	// log any published bids.
    	String bidLogFile = getBidLogFileName("CAQ3_POST");
    	assert(!CSVLogReader.containsLogLinesForToken(bidLogFile, "auctioneer"));
    	assert(!CSVLogReader.containsLogLinesForToken(bidLogFile, "concentrator"));
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "testagent1"));
    	
    }
	
	/**
	 * Ungracefully remove an auctioneer from a cluster, and after a set time, re-add 
	 * an auctioneer to the cluster.
	 * <p>
	 * Starts with an initial configuration that defines an Auctioneer, Concentrator,
	 * CSVLoggingAgent and a TestAgent. Then the auctioneer is 'ungracefully' removed
	 * by removing the configuration. A third version of the configuration file will
	 * restore the auctioneer configuration. 
	 * <p>
	 * Expected behaviour: (1) The cluster is operational and functions without any
	 * problems. (2) After the first update the auctioneer is removed. The concentrator
	 * stops send in bids to the auctioneer and no price information is received. The
	 * TestAgent will continue to send bids to the concentrator. (3) The second update
	 * restores the situation and the cluster works fine again.  
	 *
	 * @throws Exception
	 */
	public void test_CAQ4_ReAddAuctioneerAfterUngracefulRemoval() throws Exception {
		
		// First initial configuration: Auctioneer, Concentrator, CSVLoggingAgent and TestAgent. 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CAQ4_init.xml");

		// Check if the ConfigManager is present
		assert(isConfigManagerComponentPresent());
    			
    	// Verify that there is an Auctioneer configuration present
    	Configuration[] configurations = listFactoryConfigurations(AUCTIONEER_AGENT_PID);
    	assert(configurations == null || configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("auctioneer"));
    		assert(agentProps != null && agentProps.get("update.interval") != null && agentProps.get("update.interval").equals(30));
    		assert(agentProps != null && agentProps.get("bid.expiration.time") != null && agentProps.get("bid.expiration.time").equals(300));
    	}
    	    	
    	// Let the agents exchange some bids and price info
    	Thread.sleep(60000);   	
    	
    	// Load configuration that adds an Auctioneer configuration
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CAQ4_ungracefully_remove_auctioneer.xml");
    	
    	// Verify that there is exactly no Auctioneer configuration present
    	configurations = listFactoryConfigurations(AUCTIONEER_AGENT_PID);
    	assert(configurations == null || configurations.length == 0 );

    	// Verify that the Auctioneer component is unsatisfied
    	Component[] components = getComponents(AUCTIONEER_AGENT_PID);
    	assert(components != null && components.length == 1 );
    	assert(components[0].getState() == Component.STATE_UNSATISFIED);
    	
		
    	// Let the agents exchange some bids and price info
    	Thread.sleep(60000);
    	
    	// Check log files. After the update only the auctioneer is logging price info.
    	String priceLogFile = getPriceLogFileName("CAQ4_POST");
    	assert(!CSVLogReader.containsLogLines(priceLogFile));
    	
    	// After the update the agents (testagent1, concentrator) will not
    	// log any published bids.
    	String bidLogFile = getBidLogFileName("CAQ4_POST");
    	assert(!CSVLogReader.containsLogLinesForToken(bidLogFile, "auctioneer"));
    	assert(!CSVLogReader.containsLogLinesForToken(bidLogFile, "concentrator"));
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "testagent1"));

    	// Load configuration that adds an Auctioneer configuration
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CAQ4_re_add_auctioneer.xml");
    	
    	// Verify that there is exactly no Auctioneer configuration present
    	configurations = listFactoryConfigurations(AUCTIONEER_AGENT_PID);
    	assert(configurations != null && configurations.length == 1 );

    	// Verify that the Auctioneer is active
    	components = getComponents(AUCTIONEER_AGENT_PID);
    	assert(components != null && components.length == 1 );
    	assert(checkComponentState(components, AUCTIONEER_AGENT_PID, "auctioneer", Component.STATE_ACTIVE));    	
		
    	// Let the agents exchange some bids and price info
    	Thread.sleep(60000);
    	    	
    	// Check log files. After auctioneer and concentrator log again price info.
    	priceLogFile = getPriceLogFileName("CAQ4_POST2");
    	assert(CSVLogReader.containsLogLinesForToken(priceLogFile, "auctioneer"));
    	assert(CSVLogReader.containsLogLinesForToken(priceLogFile, "concentrator"));

    	
    	// All agents log again bids info.
    	bidLogFile = getBidLogFileName("CAQ4_POST2");
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "auctioneer"));
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "concentrator"));
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "testagent1"));

    }
}
