package net.powermatcher.core.scheduler;



import net.powermatcher.core.adapter.service.AdapterService;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.object.IdentifiableObject;
import net.powermatcher.core.scheduler.service.TimeConnectorService;
import net.powermatcher.core.scheduler.service.TimeService;


/**
 * Provides real or simulated time services.  
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see TimeConnectorService
 */
public class TimeAdapter extends IdentifiableObject implements AdapterService {

	/**
	 * Define the time source connector (TimeConnectorService) field.
	 */
	private TimeConnectorService timeConnector;
	/**
	 * Define the time source (TimeService) field.
	 */
	private TimeService timeSource;

	/**
	 * Constructs an instance of this class from the specified configuration
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	public TimeAdapter(final ConfigurationService configuration, TimeService timeSource) {
		super(configuration);
		this.timeSource = timeSource;
	}

	/**
	 * Bind.
	 * 
	 * @throws Exception
	 */
	@Override
	public void bind() throws Exception {
		this.timeConnector.bind(this.timeSource);
	}

	/**
	 * Gets the time source connector (TimeConnectorService) value.
	 * 
	 * @return The time source connector (TimeConnectorService) value.
	 * @see #setTimeConnector(TimeConnectorService)
	 */
	public TimeConnectorService getTimeConnector() {
		return this.timeConnector;
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
		return super.isEnabled() && this.timeConnector.isEnabled();
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
	 * Sets the time source connector value.
	 * 
	 * @param timeConnector
	 *            The time source connector (<code>TimeConnectorService</code>)
	 *            parameter.
	 * @see #getTimeConnector()
	 */
	public void setTimeConnector(final TimeConnectorService timeConnector) {
		this.timeConnector = timeConnector;
	}

	/**
	 * Unbind.
	 */
	@Override
	public void unbind() {
		this.timeConnector.unbind(this.timeSource);
	}

}
