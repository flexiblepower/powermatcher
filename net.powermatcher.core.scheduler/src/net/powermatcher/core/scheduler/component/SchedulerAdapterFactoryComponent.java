package net.powermatcher.core.scheduler.component;


import java.util.Map;

import net.powermatcher.core.adapter.component.TargetAdapterFactoryComponent;
import net.powermatcher.core.adapter.service.Connectable;
import net.powermatcher.core.adapter.service.DirectAdapterFactoryService;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.core.object.config.IdentifiableObjectConfiguration;
import net.powermatcher.core.scheduler.SchedulerAdapterFactory;
import net.powermatcher.core.scheduler.service.SchedulerConnectorService;

import org.osgi.framework.BundleContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

/**
 * @author IBM
 * @version 0.9.0
 */
@Component(name = SchedulerAdapterFactoryComponent.COMPONENT_NAME, designateFactory = SchedulerAdapterFactoryComponentConfiguration.class)
public class SchedulerAdapterFactoryComponent extends TargetAdapterFactoryComponent<SchedulerConnectorService> {
	/**
	 * Define the component name (String) constant.
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.core.scheduler.SchedulerAdapterFactory";

	private SchedulerAdapterFactory schedulerAdapterFactory; 

	/**
	 * Constructs an instance of this class.
	 */
	public SchedulerAdapterFactoryComponent() {
		super(new SchedulerAdapterFactory());
	}

	/**
	 * Activate with the specified properties parameter.
	 * 
	 * @param properties
	 *            The properties (<code>Map<String,Object></code>) parameter.
	 */
	@Activate
	protected void activate(final BundleContext context, final Map<String, Object> properties) {
		Configurable configuration = new BaseConfiguration(properties);
		String clusterId = configuration.getProperty(IdentifiableObjectConfiguration.CLUSTER_ID_PROPERTY, IdentifiableObjectConfiguration.CLUSTER_ID_DEFAULT);
		this.schedulerAdapterFactory = SchedulerAdapterFactory.getSchedulerAdapterFactory(clusterId);
		setFactory(this.schedulerAdapterFactory);
		super.activate(context, properties);
	}

	/**
	 * Deactivate.
	 */
	@Deactivate
	protected void deactivate() {
		super.deactivate();
		this.schedulerAdapterFactory.destroyScheduler();
	}

	/**
	 * Add scheduler connector with the specified target connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>SchedulerConnectorService</code>)
	 *            parameter.
	 * @see #removeSchedulerConnector(SchedulerConnectorService)
	 */
	@Reference(type = '*')
	protected void addSchedulerConnector(final SchedulerConnectorService targetConnector) {
		super.addTargetConnector(targetConnector);
	}

	/**
	 * Remove scheduler connector with the specified target connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>SchedulerConnectorService</code>)
	 *            parameter.
	 * @see #addSchedulerConnector(SchedulerConnectorService)
	 */
	protected void removeSchedulerConnector(final SchedulerConnectorService targetConnector) {
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
	protected Class<SchedulerConnectorService> getConnectorType() {
		return SchedulerConnectorService.class;
	}

}
