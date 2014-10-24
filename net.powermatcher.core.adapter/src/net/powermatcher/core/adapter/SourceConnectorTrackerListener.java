package net.powermatcher.core.adapter;


import net.powermatcher.core.adapter.service.Connectable;

/** 
 * 
 * <p>
 * The SourceConnectorTrackerListener interface defines the listener for
 * an object that implements the TargetConnectorFactoryTracker interface. 
 * </p>
 * <p> An implementation of the interface will implement the bind and
 * unbind method. The instance will listen for requests from the TargetConnectorFactoryTracker
 * object to bind or unbind a ConnectorService object.
 * </p>
 *
 * @author IBM
 * @version 0.9.0
 *
 * @param <T> Defines the generic ConnectorService type that ConnectorTrackerListener will use.
 * 
 */
public interface SourceConnectorTrackerListener<T extends Connectable> {

	/**
	 * Bind with the specified connector and target connector id parameters.
	 * 
	 * @param connector
	 *            The source connector (<code>T</code>) parameter.
	 * @param targetConnectorId
	 *            The target connector id (<code>String</code>) parameter.
	 */
	public void bind(final T connector, final String targetConnectorId);

	/**
	 * Unbind with the specified connector and target connector id parameters.
	 * 
	 * @param connector
	 *            The source connector (<code>T</code>) parameter.
	 * @param targetConnectorId
	 *            The target connector id (<code>String</code>) parameter.
	 */
	public void unbind(final T connector, final String targetConnectorId);

}
