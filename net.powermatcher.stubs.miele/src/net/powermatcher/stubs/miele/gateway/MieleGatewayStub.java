package net.powermatcher.stubs.miele.gateway;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import net.powermatcher.der.agent.miele.at.home.gateway.constants.MieleGatewayConstants;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author IBM
 * @version 1.0.0
 */
public class MieleGatewayStub {
	/**
	 * Define the logger (Logger) field.
	 */
	private final static Logger logger = LoggerFactory.getLogger(MieleGatewayStub.class);

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(final String[] args) throws Exception {

		new MieleGatewayStub().start(args);
	}

	/*
	 * Instance fields
	 */
	private String id;
	private Properties properties;

	private Server server;

	/**
	 * 
	 */
	public MieleGatewayStub() {
	}

	/**
	 * Load properties with the specified arguments parameter and return the
	 * Properties result.
	 * 
	 * @param args
	 *            The arguments (<code>String[]</code>) parameter.
	 * @return Results of the load properties (<code>Properties</code>) value.
	 */
	private Properties loadProperties(final String[] args) {
		if (args.length == 0) {
			logger.error("Usage: MieleGatewayStub <filename.properties>");
		} else {
			String fileName = args[0];
			Properties properties = new Properties();
			try {
				properties.load(new FileInputStream(fileName));
				return properties;
			} catch (final IOException e) {
				logger.error("Could not open properties file: " + fileName, e);
			}
		}
		return null;
	}

	/**
	 * Start with the specified arguments parameter.
	 * 
	 * @param args
	 *            The arguments (<code>String[]</code>) parameter.
	 */
	private void start(final String[] args) {
		this.properties = loadProperties(args);
		if (this.properties != null) {

			try {
				this.id = this.properties
						.getProperty(MieleGatewayConstants.MG_PROPERTY_ID, MieleGatewayConstants.MG_DEFAULT_ID);
				int port = Integer.parseInt(this.properties.getProperty(MieleGatewayConstants.MG_PROPERTY_PORT_NUMBER,
						MieleGatewayConstants.MG_PROPERTY_PORT_NUMBER));
				this.server = new Server(port);
				Handler handler = new MieleGatewayHandler(port, properties);
				this.server.setHandler(handler);

				this.server.start();
				this.server.join();
			} catch (final Exception e) {
				logger.error("Could not start the Miele Gateway" + this.id, e);
			}
		}
	}

}
