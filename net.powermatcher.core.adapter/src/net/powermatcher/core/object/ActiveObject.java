package net.powermatcher.core.object;


import java.util.concurrent.ScheduledExecutorService;

import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.core.object.config.ActiveObjectConfiguration;
import net.powermatcher.core.scheduler.service.SchedulerConnectorService;

/**
 * 
 * <p>
 * The abstract class ActiveObject extends the ConnectableObject by providing a timer
 * based update functionality. Objects that need to be configurable,
 * identifiable and active, i.e. update regularly its state, should subclass
 * this class.
 * </p>
 * <p>
 * A child class should at least override the doUpdate() class to specify a
 * custom update behavior. The update interval can be defined via the
 * configuration property. The property name for the update interval is defined
 * by the UPDATE_INTERVAL_PROPERTY constant in the ActiveObjectConfiguration
 * interface.
 * </p>
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see ActiveObjectConfiguration
 */
public abstract class ActiveObject extends ConnectableObject implements SchedulerConnectorService {

	/**
	 * Define the scheduler (ScheduledExecutorService) that is used to schedule
	 * tasks.
	 */
	private ScheduledExecutorService scheduler;
	/**
	 * Define the update interval (int) field.
	 */
	private int updateInterval;
	/**
	 * Define the last update time stamp (long) field.
	 */
//	private long lastUpdateTimestamp;

	/**
	 * Constructs an instance of this class.
	 */
	protected ActiveObject() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified configuration
	 * parameter. The update interval should be set via this configuration
	 * object.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	protected ActiveObject(final Configurable configuration) {
		super(configuration);
	}

	/**
	 * Bind the specified scheduler for scheduling tasks in real or simulated
	 * time. Subclasses must override this method to start periodic tasks.
	 * 
	 * @param scheduler
	 *            The scheduler (<code>ScheduledExecutorService</code>) to bind.
	 * @see #unbind(ScheduledExecutorService)
	 */
	@Override
	public void bind(final ScheduledExecutorService scheduler) {
		this.scheduler = scheduler;
		startPeriodicTasks();
	}

	/**
	 * Get the task scheduler bound to this object. Subclasses shall use the
	 * scheduler to schedule one-time or periodic asynchronous tasks.
	 * 
	 * @return The scheduler bound to this object, or null if no scheduler is
	 *         bound.
	 */
	public ScheduledExecutorService getScheduler() {
		return this.scheduler;
	}

	/**
	 * Gets the update interval (int) value.
	 * 
	 * @return The update interval (<code>int</code>) value.
	 */
	protected int getUpdateInterval() {
		return this.updateInterval;
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		this.updateInterval = getProperty(ActiveObjectConfiguration.UPDATE_INTERVAL_PROPERTY,
				ActiveObjectConfiguration.UPDATE_INTERVAL_DEFAULT);
	}

	/**
	 * Sets the configuration value. The update interval should be defined
	 * through the configuration object. The property name for the update
	 * interval is defined by the UPDATE_INTERVAL_PROPERTY constant in
	 * ActiveObjectConfiguration.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * 
	 * @see ActiveObjectConfiguration#UPDATE_INTERVAL_PROPERTY
	 */
	@Override
	public void setConfiguration(final Configurable configuration) {
		super.setConfiguration(configuration);
		initialize();
	}

//	/**
//	 * Set last update time stamp.
//	 */
//	protected void setLastUpdateTimestamp() {
//		this.lastUpdateTimestamp = getCurrentTimeMillis();
//	}

	/**
	 * Start periodic tasks of the active object.
	 * This method will be called when the scheduler is bound to the active object.
	 */
	protected abstract void startPeriodicTasks();

	/**
	 * Stop periodic tasks of the active object.
	 * This method will be called when the scheduler is unbound from the active object.
	 */
	protected abstract void stopPeriodicTasks();

	/**
	 * Unbind the specified scheduler for scheduling tasks in real or simulated
	 * time. Subclasses must override this method to stop periodic tasks.
	 * 
	 * @param scheduler
	 *            The scheduler (<code>ScheduledExecutorService</code>) to
	 *            unbind.
	 * 
	 * @see #bind(ScheduledExecutorService)
	 */
	@Override
	public void unbind(final ScheduledExecutorService scheduler) {
		stopPeriodicTasks();
		this.scheduler = null;
	}

}
