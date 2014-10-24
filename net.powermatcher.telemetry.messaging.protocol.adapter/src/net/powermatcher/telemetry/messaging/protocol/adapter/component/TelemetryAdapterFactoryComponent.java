package net.powermatcher.telemetry.messaging.protocol.adapter.component;


import java.util.Map;

import org.osgi.framework.BundleContext;

import net.powermatcher.core.adapter.component.SourceAdapterFactoryComponent;
import net.powermatcher.core.adapter.service.Connectable;
import net.powermatcher.core.adapter.service.DirectAdapterFactoryService;
import net.powermatcher.telemetry.messaging.protocol.adapter.TelemetryAdapter;
import net.powermatcher.telemetry.messaging.protocol.adapter.TelemetryAdapterFactory;
import net.powermatcher.telemetry.service.TelemetryConnectorService;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;


/**
 * OSGi wrapper component for a TelemetryAdapter.
 * 
 * <p>
 * The TelemetryAdapterFactoryComponent is a wrapper class that creates an OSGi component of
 * a TelemetryAdapter. A ConnectorTracker will bind the adapter component to a component 
 * that implements the TelemetryConnectorService interface if that component instance has the
 * same id and cluster id.
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see TelemetryAdapterFactory
 * @see TelemetryAdapter
 * @see TelemetryAdapterFactoryComponentConfiguration
 */
@Component(name = TelemetryAdapterFactoryComponent.COMPONENT_NAME, designateFactory = TelemetryAdapterFactoryComponentConfiguration.class)
public class TelemetryAdapterFactoryComponent extends SourceAdapterFactoryComponent<TelemetryConnectorService> {
	/**
	 * Define the component name (String) constant.
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.telemetry.messaging.protocol.adapter.TelemetryAdapterFactory";

	/**
	 * Constructs an instance of this class.
	 */
	public TelemetryAdapterFactoryComponent() {
		super(new TelemetryAdapterFactory());
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
	 * Add agent connector with the specified source connector parameter.
	 * 
	 * @param sourceConnector
	 *            The source connector (<code>TelemetryConnectorService</code>) parameter.
	 * @see #removeTelemetryConnector(TelemetryConnectorService)
	 */
	@Reference(type = '*')
	protected void addTelemetryConnector(final TelemetryConnectorService sourceConnector) {
		super.addSourceConnector(sourceConnector);
	}

	/**
	 * Deactivate.
	 */
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	/**
	 * Remove agent connector with the specified source connector parameter.
	 * 
	 * @param sourceConnector
	 *            The source connector (<code>TelemetryConnectorService</code>) parameter.
	 * @see #addTelemetryConnector(TelemetryConnectorService)
	 */
	protected void removeTelemetryConnector(final TelemetryConnectorService sourceConnector) {
		super.removeSourceConnector(sourceConnector);
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
