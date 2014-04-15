package net.powermatcher.expeditor.launcher.mbroker;


import java.util.Properties;

import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.launcher.main.Main;
import net.powermatcher.expeditor.broker.manager.BrokerManager;


/**
 * Starts PowerMatcher demo application and a message broker.
 * <p>
 * This is a Java main application that starts a PowerMatcher demo
 * application together with a message broker. The broker can be
 * configured using a properties file named broker_config.properties in
 * the local directory.
 * 
 * @author IBM
 * @version 0.9.0
 */
public class MicroBrokerLauncher extends Main {

	/**
	 * Main with the specified arguments parameter.
	 * 
	 * @param args
	 *            The arguments (<code>String[]</code>) parameter.
	 */
	public static void main(final String[] args) {
		new MicroBrokerLauncher().run(args);
	}

	/**
	 * Define the broker manager (BrokerManager) field.
	 */
	private BrokerManager brokerManager;

	/**
	 * Start message broker manager.
	 * @param brokerProperties 
	 * 
	 * @throws Exception
	 *             Exception.
	 */
	protected void startBrokerManager(Properties brokerProperties) throws Exception {
		logger.info("Starting MicroBroker");
		this.brokerManager = new BrokerManager(new BaseConfiguration(brokerProperties));
		this.brokerManager.startBroker();
	}

}
