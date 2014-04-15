package net.powermatcher.core.scheduler;


import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.TargetAdapterFactoryService;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.object.IdentifiableObject;
import net.powermatcher.core.scheduler.service.TimeConnectorService;


/**
 * Adapter factory for SchedulerAdapter.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see TimeConnectorService
 * @see SchedulerAdapter
 */
public class TimeAdapterFactory extends IdentifiableObject implements TargetAdapterFactoryService<TimeConnectorService> {

	private String clusterId;

	public TimeAdapterFactory() {
		this(null);
	}

	public TimeAdapterFactory(String clusterId) {
		this.clusterId = clusterId;
	}


	@Override
	public TimeAdapter createAdapter(ConfigurationService configuration, TimeConnectorService connector,
			ConnectorLocaterService connectorLocater, int adapterIndex) {
		return createAdapter(configuration, connector);
	}

	/**
	 * Create adapter with the specified configuration and log listener
	 * connector parameters and return the TimeAdapter result.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @param timeConnector
	 *            The log listener connector (
	 *            <code>TimeConnectorService</code>) parameter.
	 * @return Results of the create adapter (<code>LogListenerAdapter</code>)
	 *         value.
	 */
	@Override
	public TimeAdapter createAdapter(final ConfigurationService configuration,
			final TimeConnectorService timeConnector) {
		SchedulerAdapterFactory schedulerAdapterFactory = SchedulerAdapterFactory.getSchedulerAdapterFactory(this.clusterId);
		TimeAdapter timeAdapter = new TimeAdapter(configuration, schedulerAdapterFactory.getTimeSource());
		timeAdapter.setTimeConnector(timeConnector);
		return timeAdapter;
	}

	@Override
	public String getAdapterName() {
		return TimeAdapter.class.getSimpleName();
	}
	
}
