package net.powermatcher.core.adapter;


import net.powermatcher.core.adapter.service.Connectable;

/** 
 * 
 * <p>
 * The ConnectorTrackerListener interface defines the listener for
 * an object that implements the ConnectorTracker interface. 
 * </p>
 * <p> An implementation of the interface will implement the bind and
 * unbind method. The instance will listen for requests from the ConnectorTracker
 * object to bind or unbind a ConnectorService object.
 * </p>
 *
 * @author IBM
 * @version 0.9.0
 *
 * @param <T> Defines the generic ConnectorService type that ConnectorTrackerListener will use.
 * 
 */
public interface TargetConnectorTrackerListener<T extends Connectable> {
	/**
	 * Bind with the specified connector parameter.
	 * 
	 * @param connector
	 *            The connector (<code>T</code>) parameter.
	 */
	public void bind(final T connector);

	/**
	 * Unbind with the specified connector parameter.
	 * 
	 * @param connector
	 *            The connector (<code>T</code>) parameter.
	 */
	public void unbind(final T connector);

}
