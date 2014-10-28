package net.powermatcher.core.messaging.protocol.adapter.component;


import java.util.Map;

import org.osgi.framework.BundleContext;

import net.powermatcher.core.adapter.component.TargetAdapterFactoryComponent;
import net.powermatcher.core.adapter.service.ConnectorService;
import net.powermatcher.core.adapter.service.DirectAdapterFactoryService;
import net.powermatcher.core.agent.framework.log.LogListenerConnectorService;
import net.powermatcher.core.messaging.protocol.adapter.LogListenerAdapterFactory;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;


/**
 *  OSGi wrapper component for a LogListenerAdapterFactory.
 * 
 * <p>
 * The LogListenerAdapterFactoryComponent is a wrapper class that creates an OSGi component of
 * a LogListenerAdapter. A ConnectorTracker will bind the adapter component to a log listener
 * component that implements the LogListenerConnectorService interface if that component 
 * instance has the same id and cluster id.
 * </p>
 * Configuration properties and default values are defined in LogListenerAdapterFactoryComponentConfiguration.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see LogListenerAdapterFactory
 * @see LogListenerAdapterFactoryComponentConfiguration
 */
@Component(name = LogListenerAdapterFactoryComponent.COMPONENT_NAME, designateFactory = LogListenerAdapterFactoryComponentConfiguration.class)
public class LogListenerAdapterFactoryComponent extends TargetAdapterFactoryComponent<LogListenerConnectorService> {
	/**
	 * Define the component name (String) constant.
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.core.messaging.protocol.adapter.LogListenerAdapterFactory";

	/**
	 * Constructs an instance of this class.
	 */
	public LogListenerAdapterFactoryComponent() {
		super(new LogListenerAdapterFactory());
	}

	/**
	 * Activate with the specified properties parameter.
	 * 
	 * @param properties
	 *            The properties (<code>Map<String,Object></code>) parameter.
	 */
	@Activate
	protected void activate(final BundleContext context, final Map<String, Object> properties) {
		super.activate(context, properties);
	}

	/**
	 * Add log listener connector with the specified connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>LogListenerConnectorService</code>)
	 *            parameter.
	 * @see #removeLogListenerConnector(LogListenerConnectorService)
	 */
	@Reference(type = '*')
	protected void addLogListenerConnector(final LogListenerConnectorService targetConnector) {
		super.addTargetConnector(targetConnector);
	}

	/**
	 * Deactivate.
	 */
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	/**
	 * Remove log listener connector with the specified connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>LogListenerConnectorService</code>)
	 *            parameter.
	 * @see #addLogListenerConnector(LogListenerConnectorService)
	 */
	protected void removeLogListenerConnector(final LogListenerConnectorService targetConnector) {
		super.removeTargetConnector(targetConnector);
	}

	/**
	 * Get the Java type of the connector T.
	 * Due to type erasure it is necessary to gave a method return the type explicitly for use
	 * in the call to getTargetConnectorIds.
	 * @see DirectAdapterFactoryService#getTargetConnectorIds(ConnectorService) 
	 * @return The Java type of the connector T.
	 */
	@Override
	protected Class<LogListenerConnectorService> getConnectorType() {
		return LogListenerConnectorService.class;
	}

}
