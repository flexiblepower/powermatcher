package net.powermatcher.core.direct.protocol.adapter.component;


import java.util.Map;

import net.powermatcher.core.adapter.DirectConnectorFactoryTracker;
import net.powermatcher.core.adapter.DirectConnectorTrackerListener;
import net.powermatcher.core.adapter.component.DirectAdapterFactoryComponent;
import net.powermatcher.core.adapter.service.Connectable;
import net.powermatcher.core.adapter.service.DirectAdapterFactoryService;
import net.powermatcher.telemetry.direct.protocol.adapter.DirectTelemetryAdapter;
import net.powermatcher.telemetry.direct.protocol.adapter.DirectTelemetryAdapterFactory;
import net.powermatcher.telemetry.service.TelemetryConnectorService;
import net.powermatcher.telemetry.service.TelemetryListenerConnectorService;

import org.osgi.framework.BundleContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;


/**
 *  OSGi wrapper component for a DirectTelemetryAdapter.
 * 
 * <p>
 * The DirectTelemetryAdapterFactoryComponent is a wrapper class that creates an OSGi component of
 * a DirectTelemetryAdapter. A DirectConnectorFactoryTracker will bind the adapter component to a direct telemetry adapter
 * component that implements the TelemetryConnectorService interface if that component 
 * instance has the same connector id and cluster id.
 * </p>
 * Configuration properties and default values are defined in DirectTelemetryAdapterFactoryComponentConfiguration.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see DirectConnectorFactoryTracker
 * @see DirectConnectorTrackerListener
 * @see TelemetryConnectorService
 * @see DirectTelemetryAdapterFactoryComponentConfiguration
 * @see DirectTelemetryAdapter
 */
@Component(name = DirectTelemetryAdapterFactoryComponent.COMPONENT_NAME, designateFactory = DirectTelemetryAdapterFactoryComponentConfiguration.class)
public class DirectTelemetryAdapterFactoryComponent extends DirectAdapterFactoryComponent<TelemetryConnectorService, TelemetryListenerConnectorService> {
	/**
	 * Define the component name (String) constant.
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.core.direct.protocol.adapter.component.DirectTelemetryAdapterFactory";

	/**
	 * Constructs an instance of this class.
	 */
	public DirectTelemetryAdapterFactoryComponent() {
		super(new DirectTelemetryAdapterFactory());
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
	 * Add telemetry connector with the specified source connector parameter.
	 * 
	 * @param sourceConnector
	 *            The source connector (<code>TelemetryConnectorService</code>)
	 *            parameter.
	 * @see #removeTelemetryConnector(TelemetryConnectorService)
	 */
	@Reference(type = '*')
	protected void addTelemetryConnector(final TelemetryConnectorService sourceConnector) {
		super.addSourceConnector(sourceConnector);
	}

	/**
	 * Add telemetry listener connector with the specified target connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>TelemetryListenerConnectorService</code>)
	 *            parameter.
	 * @see #removeTelemetryListenerConnector(TelemetryListenerConnectorService)
	 */
	@Reference(type = '*')
	protected void addTelemetryListenerConnector(final TelemetryListenerConnectorService targetConnector) {
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
	 * Remove telemetry connector with the specified connector parameter.
	 * 
	 * @param sourceConnector
	 *            The source connector (<code>TelemetryConnectorService</code>)
	 *            parameter.
	 * @see #addTelemetryConnector(TelemetryConnectorService)
	 */
	protected void removeTelemetryConnector(final TelemetryConnectorService sourceConnector) {
		super.removeSourceConnector(sourceConnector);
	}

	/**
	 * Remove telemetry listener connector with the specified target connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>AgentConnectorService</code>)
	 *            parameter.
	 * @see #addTelemetryListenerConnector(TelemetryListenerConnectorService)
	 */
	protected void removeTelemetryListenerConnector(final TelemetryListenerConnectorService targetConnector) {
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
	protected Class<TelemetryConnectorService> getConnectorType() {
		return TelemetryConnectorService.class;
	}

}
