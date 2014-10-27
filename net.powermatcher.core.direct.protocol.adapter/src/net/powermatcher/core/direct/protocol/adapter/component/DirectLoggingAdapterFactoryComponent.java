package net.powermatcher.core.direct.protocol.adapter.component;


import java.util.Map;

import net.powermatcher.core.adapter.DirectConnectorFactoryTracker;
import net.powermatcher.core.adapter.DirectConnectorTrackerListener;
import net.powermatcher.core.adapter.component.DirectAdapterFactoryComponent;
import net.powermatcher.core.adapter.service.Connectable;
import net.powermatcher.core.adapter.service.DirectAdapterFactoryService;
import net.powermatcher.core.agent.framework.log.LogListenable;
import net.powermatcher.core.agent.framework.log.LogPublishable;
import net.powermatcher.core.direct.protocol.adapter.DirectLoggingAdapter;
import net.powermatcher.core.direct.protocol.adapter.DirectLoggingAdapterFactory;

import org.osgi.framework.BundleContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;


/**
 *  OSGi wrapper component for a DirectLoggingAdapter.
 * 
 * <p>
 * The DirectAdapterFactoryComponent is a wrapper class that creates an OSGi component of
 * a DirectLoggingAdapter. A DirectConnectorFactoryTracker will bind the adapter component to a direct logging adapter
 * component that implements the LoggingConnectorService interface if that component 
 * instance has the same connector id and cluster id.
 * </p>
 * Configuration properties and default values are defined in DirectLoggingAdapterFactoryComponentConfiguration.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see DirectConnectorFactoryTracker
 * @see DirectConnectorTrackerListener
 * @see LogPublishable
 * @see DirectLoggingAdapterFactoryComponentConfiguration
 * @see DirectLoggingAdapter
 */
@Component(name = DirectLoggingAdapterFactoryComponent.COMPONENT_NAME, designateFactory = DirectLoggingAdapterFactoryComponentConfiguration.class)
public class DirectLoggingAdapterFactoryComponent extends DirectAdapterFactoryComponent<LogPublishable, LogListenable> {
	/**
	 * Define the component name (String) constant.
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.core.direct.protocol.adapter.component.DirectLoggingAdapterFactory";

	/**
	 * Constructs an instance of this class.
	 */
	public DirectLoggingAdapterFactoryComponent() {
		super(new DirectLoggingAdapterFactory());
	}

	/**
	 * Activate with the specified properties parameter.
	 * 
	 * @param context OSGi bundle context.
	 * @param properties
	 *            The properties (<code>Map<String,Object></code>) parameter.
	 */
	@Activate
	protected void activate(final BundleContext context, final Map<String, Object> properties) {
		super.activate(context, properties);
	}

	/**
	 * Add logging connector with the specified source connector parameter.
	 * 
	 * @param sourceConnector
	 *            The source connector (<code>LoggingConnectorService</code>)
	 *            parameter.
	 * @see #removeLoggingConnector(LogPublishable)
	 */
	@Reference(type = '*')
	void addLoggingConnector(final LogPublishable sourceConnector) {
		super.addSourceConnector(sourceConnector);
	}

	/**
	 * Add log listener connector with the specified target connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>LogListenerConnectorService</code>)
	 *            parameter.
	 * @see #removeLogListenerConnector(LogListenable)
	 */
	@Reference(type = '*')
	void addLogListenerConnector(final LogListenable targetConnector) {
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
	 * Remove logging connector with the specified source connector parameter.
	 * 
	 * @param sourceConnector
	 *            The source connector (<code>LoggingConnectorService</code>)
	 *            parameter.
	 * @see #addLoggingConnector(LogPublishable)
	 */
	protected void removeLoggingConnector(final LogPublishable sourceConnector) {
		super.removeSourceConnector(sourceConnector);
	}

	/**
	 * Remove log listener connector with the specified target connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>AgentConnectorService</code>)
	 *            parameter.
	 * @see #addLogListenerConnector(LogListenable)
	 */
	protected void removeLogListenerConnector(final LogListenable targetConnector) {
		super.removeTargetConnector(targetConnector);
	}

	/**
	 * Get the Java type of the connector T.
	 * Due to type erasure it is necessary to gave a method return the type explicitly for use
	 * in the call to getTargetConnectorIds.
	 * @see DirectAdapterFactoryService#getTargetConnectorIds(Connectable) 
	 * @return The Java type of the connector T.
	 */
	@Override
	protected Class<LogPublishable> getConnectorType() {
		return LogPublishable.class;
	}

}
