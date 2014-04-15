package net.powermatcher.core.messaging.service;



/**
 * @author IBM
 * @version 0.9.0
 */
public interface MessagingConnectionDefinition {

	/**
	 * Create a new connection from the connection definition
	 * 
	 * @return Newly created connection, ready for adapter binding.
	 */
	public MessagingConnectionService createConnection();

}
