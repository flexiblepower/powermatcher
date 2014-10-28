package net.powermatcher.telemetry.messaging.protocol.adapter.component;


import java.util.Map;

import org.osgi.framework.BundleContext;

import net.powermatcher.core.adapter.component.TargetAdapterFactoryComponent;
import net.powermatcher.core.adapter.service.ConnectorService;
import net.powermatcher.core.adapter.service.DirectAdapterFactoryService;
import net.powermatcher.telemetry.messaging.protocol.adapter.TelemetryListenerAdapterFactory;
import net.powermatcher.telemetry.service.TelemetryListenerConnectorService;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;


/**
 *  OSGi wrapper component for a TelemetryListenerAdapterFactory.
 * 
 * <p>
 * The TelemetryListenerAdapterFactoryComponent is a wrapper class that creates an OSGi component of
 * a TelemetryListenerAdapter. A ConnectorTracker will bind the adapter component to a log listener
 * component that implements the TelemetryListenerConnectorService interface if that component 
 * instance has the same id and cluster id.
 * </p>
 * Configuration properties and default values are defined in TelemetryListenerAdapterFactoryComponentConfiguration.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see TelemetryListenerAdapterFactory
 * @see TelemetryListenerConnectorService
 * @see TelemetryListenerAdapterFactoryComponentConfiguration
 */
@Component(name = TelemetryListenerAdapterFactoryComponent.COMPONENT_NAME, designateFactory = TelemetryListenerAdapterFactoryComponentConfiguration.class)
public class TelemetryListenerAdapterFactoryComponent extends TargetAdapterFactoryComponent<TelemetryListenerConnectorService> {
	/**
	 * Define the component name (String) constant.
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.telemetry.messaging.protocol.adapter.TelemetryListenerAdapterFactory";

	/**
	 * Constructs an instance of this class.
	 */
	public TelemetryListenerAdapterFactoryComponent() {
		super(new TelemetryListenerAdapterFactory());
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
	 * Add log listener connector with the specified target connector parameter.
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
	 * Remove log listener connector with the specified target connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>TelemetryListenerConnectorService</code>)
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
	 * @see DirectAdapterFactoryService#getTargetConnectorIds(ConnectorService) 
	 * @return The Java type of the connector T.
	 */
	@Override
	protected Class<TelemetryListenerConnectorService> getConnectorType() {
		return TelemetryListenerConnectorService.class;
	}

}
