package net.powermatcher.core.scheduler;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.TargetAdapterFactoryService;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.object.IdentifiableObject;
import net.powermatcher.core.scheduler.service.SchedulerConnectorService;
import net.powermatcher.core.scheduler.service.TimeService;


/**
 * Adapter factory for SchedulerAdapter.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see SchedulerConnectorService
 * @see SchedulerAdapter
 */
public class SchedulerAdapterFactory extends IdentifiableObject implements TargetAdapterFactoryService<SchedulerConnectorService> {

	private static Map<String, SchedulerAdapterFactory> schedulerAdapterFactories = new HashMap<String, SchedulerAdapterFactory>();

	@SuppressWarnings("unused")
	private String clusterId;
	private ConditionalScheduledThreadPoolExecutor scheduler;

	public static SchedulerAdapterFactory getSchedulerAdapterFactory() {
		return getSchedulerAdapterFactory(null);
	}

	public static SchedulerAdapterFactory getSchedulerAdapterFactory(String clusterId) {
		synchronized (schedulerAdapterFactories) {
			SchedulerAdapterFactory factory = schedulerAdapterFactories.get(clusterId);
			if (factory == null) {
				factory = new SchedulerAdapterFactory(clusterId);
			}
			return factory;
		}		
	}
	
	public SchedulerAdapterFactory() {
		this(null);
	}

	public SchedulerAdapterFactory(String clusterId) {
		this.clusterId = clusterId;
		synchronized (schedulerAdapterFactories) {
			schedulerAdapterFactories.put(clusterId, this);
		}		
	}

	public ConditionalScheduledThreadPoolExecutor createScheduler(int corePoolSize) {
		scheduler = new ConditionalScheduledThreadPoolExecutor(corePoolSize);
		return scheduler;
	}

	public void destroyScheduler() {
		if (scheduler != null) {
			scheduler.shutdown();
			scheduler = null;
		}
	}

	@Override
	public SchedulerAdapter createAdapter(ConfigurationService configuration, SchedulerConnectorService connector,
			ConnectorLocaterService connectorLocater, int adapterIndex) {
		return createAdapter(configuration, connector);
	}

	/**
	 * Create adapter with the specified configuration and log listener
	 * connector parameters and return the SchedulerAdapter result.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @param schedulerConnector
	 *            The log listener connector (
	 *            <code>SchedulerConnectorService</code>) parameter.
	 * @return Results of the create adapter (<code>LogListenerAdapter</code>)
	 *         value.
	 */
	@Override
	public SchedulerAdapter createAdapter(final ConfigurationService configuration,
			final SchedulerConnectorService schedulerConnector) {
		SchedulerAdapter schedulerAdapter = new SchedulerAdapter(configuration, getScheduler());
		schedulerAdapter.setSchedulerConnector(schedulerConnector);
		return schedulerAdapter;
	}

	@Override
	public String getAdapterName() {
		return SchedulerAdapter.class.getSimpleName();
	}

	public TimeService getTimeSource() {
		return new TimeService() {
			
			@Override
			public int getRate() {
				return scheduler == null ? 1 : scheduler.getRate();
			}
			
			@Override
			public long currentTimeMillis() {
				return scheduler == null ? 0 : scheduler.currentTimeMillis();
			}
		};
	}

	public ScheduledExecutorService getScheduler() {
		if (this.scheduler == null) {
			// TODO Get from factory configuration
			int corePoolSize = Runtime.getRuntime().availableProcessors();
			createScheduler(corePoolSize);
			this.scheduler.start();
		}
		return this.scheduler;
	}
	
}
