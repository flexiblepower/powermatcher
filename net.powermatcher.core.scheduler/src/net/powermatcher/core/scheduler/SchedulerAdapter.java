package net.powermatcher.core.scheduler;



import java.util.concurrent.ScheduledExecutorService;

import net.powermatcher.core.adapter.service.AdapterService;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.object.IdentifiableObject;
import net.powermatcher.core.scheduler.service.SchedulerConnectorService;


/**
 * Provides scheduling services.  
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see SchedulerConnectorService
 */
public class SchedulerAdapter extends IdentifiableObject implements AdapterService {

	/**
	 * Define the scheduler connector (SchedulerConnectorService) field.
	 */
	private SchedulerConnectorService schedulerConnector;
	/**
	 * Define the scheduler (ScheduledExecutorService) field.
	 */
	private ScheduledExecutorService scheduler;

	/**
	 * Constructs an instance of this class.
	 * @param scheduler 
	 * @param configuration 
	 * 
	 */
	public SchedulerAdapter(ConfigurationService configuration, ScheduledExecutorService scheduler) {
		super(configuration);
		this.scheduler = scheduler;
	}

	/**
	 * Bind.
	 * 
	 * @throws Exception
	 */
	@Override
	public void bind() throws Exception {
		this.schedulerConnector.bind(this.scheduler);
	}

	/**
	 * Gets the scheduler connector (SchedulerConnectorService) value.
	 * 
	 * @return The scheduler connector (SchedulerConnectorService) value.
	 * @see #setSchedulerConnector(SchedulerConnectorService)
	 */
	public SchedulerConnectorService getSchedulerConnector() {
		return this.schedulerConnector;
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
	}

	/**
	 * Gets the enabled (boolean) value.
	 * 
	 * @return The enabled (<code>boolean</code>) value.
	 */
	@Override
	public boolean isEnabled() {
		return super.isEnabled() && this.schedulerConnector.isEnabled();
	}

	/**
	 * Sets the configuration value.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	@Override
	public void setConfiguration(final ConfigurationService configuration) {
		super.setConfiguration(configuration);
		initialize();
	}

	/**
	 * Sets the scheduler connector value.
	 * 
	 * @param schedulerConnector
	 *            The scheduler connector (<code>SchedulerConnectorService</code>)
	 *            parameter.
	 * @see #getSchedulerConnector()
	 */
	public void setSchedulerConnector(final SchedulerConnectorService schedulerConnector) {
		this.schedulerConnector = schedulerConnector;
	}

	/**
	 * Unbind.
	 */
	@Override
	public void unbind() {
		this.schedulerConnector.unbind(this.scheduler);
	}

}
