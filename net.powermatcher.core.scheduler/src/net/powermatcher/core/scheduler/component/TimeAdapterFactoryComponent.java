package net.powermatcher.core.scheduler.component;


import java.util.Map;

import net.powermatcher.core.adapter.component.TargetAdapterFactoryComponent;
import net.powermatcher.core.adapter.service.ConnectorService;
import net.powermatcher.core.adapter.service.DirectAdapterFactoryService;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.object.config.IdentifiableObjectConfiguration;
import net.powermatcher.core.scheduler.TimeAdapterFactory;
import net.powermatcher.core.scheduler.service.TimeConnectorService;

import org.osgi.framework.BundleContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

/**
 * @author IBM
 * @version 0.9.0
 */
@Component(name = TimeAdapterFactoryComponent.COMPONENT_NAME, designateFactory = TimeAdapterFactoryComponentConfiguration.class)
public class TimeAdapterFactoryComponent extends TargetAdapterFactoryComponent<TimeConnectorService> {
	/**
	 * Define the component name (String) constant.
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.core.scheduler.TimeAdapterFactory";

	private TimeAdapterFactory timeAdapterFactory; 

	/**
	 * Constructs an instance of this class.
	 */
	public TimeAdapterFactoryComponent() {
		super(new TimeAdapterFactory());
	}

	/**
	 * Activate with the specified properties parameter.
	 * 
	 * @param properties
	 *            The properties (<code>Map<String,Object></code>) parameter.
	 */
	@Activate
	protected void activate(final BundleContext context, final Map<String, Object> properties) {
		ConfigurationService configuration = new BaseConfiguration(properties);
		String clusterId = configuration.getProperty(IdentifiableObjectConfiguration.CLUSTER_ID_PROPERTY, IdentifiableObjectConfiguration.CLUSTER_ID_DEFAULT);
		this.timeAdapterFactory = new TimeAdapterFactory(clusterId);
		setFactory(this.timeAdapterFactory);
		super.activate(context, properties);
	}

	/**
	 * Deactivate.
	 */
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	/**
	 * Add scheduler connector with the specified target connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>TimeConnectorService</code>)
	 *            parameter.
	 * @see #removeTimeConnector(TimeConnectorService)
	 */
	@Reference(type = '*')
	protected void addTimeConnector(final TimeConnectorService targetConnector) {
		super.addTargetConnector(targetConnector);
	}

	/**
	 * Remove scheduler connector with the specified target connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>TimeConnectorService</code>)
	 *            parameter.
	 * @see #addTimeConnector(TimeConnectorService)
	 */
	protected void removeTimeConnector(final TimeConnectorService targetConnector) {
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
	protected Class<TimeConnectorService> getConnectorType() {
		return TimeConnectorService.class;
	}

}
