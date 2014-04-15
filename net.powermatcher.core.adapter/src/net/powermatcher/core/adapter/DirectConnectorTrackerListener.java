package net.powermatcher.core.adapter;


import net.powermatcher.core.adapter.service.ConnectorService;

/**
 * 
 * <p>
 * The ConnectorTrackerListener interface defines the listener for an object
 * that implements the ConnectorTracker interface.
 * </p>
 * <p>
 * An implementation of the interface will implement the bind and unbind method.
 * The instance will listen for requests from the ConnectorTracker object to
 * bind or unbind a ConnectorService object.
 * </p>
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @param <T>
 *            Defines the generic ConnectorService type that
 *            ConnectorTrackerListener will use.
 * 
 */
public interface DirectConnectorTrackerListener<T extends ConnectorService, F extends ConnectorService> {
	/**
	 * Bind with the specified source and target connector parameters.
	 * 
	 * @param sourceConnector
	 *            The source connector (<code>T</code>) parameter.
	 * @param targetConnector
	 *            The target connector (<code>F</code>) parameter.
	 */
	public void bind(final T sourceConnector, final F targetConnector);

	/**
	 * Unbind with the specified source and target connector parameters.
	 * 
	 * @param sourceConnector
	 *            The source connector (<code>T</code>) parameter.
	 * @param targetConnector
	 *            The target connector (<code>F</code>) parameter.
	 */
	public void unbind(final T sourceConnector, final F targetConnector);

}
