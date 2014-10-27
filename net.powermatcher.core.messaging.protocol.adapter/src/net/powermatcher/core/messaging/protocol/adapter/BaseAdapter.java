package net.powermatcher.core.messaging.protocol.adapter;


import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.core.messaging.framework.MessagingAdapter;
import net.powermatcher.core.messaging.protocol.adapter.config.BaseAdapterConfiguration;


/**
 * <p>
 * Abstract class providing a base implementation for publishing to
 * bid, log and price info topics.
 * </p>
 * <p>
 * The class extends the MessagingAdapter by defining the topic names
 * for the bid info, log and price info topics
 * </p>
 * @author IBM
 * @version 0.9.0
 */
public abstract class BaseAdapter extends MessagingAdapter {
	/**
	 * Define the bid topic suffix (String) field.
	 */
	private String bidTopicSuffix;
	/**
	 * Define the price info topic suffix (String) field.
	 */
	private String priceInfoTopicSuffix;
	/**
	 * Define the log topic suffix (String) field.
	 */
	private String logTopicSuffix;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #BaseAdapter(Configurable)
	 */
	protected BaseAdapter() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified configuration
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #BaseAdapter()
	 */
	protected BaseAdapter(final Configurable configuration) {
		super(configuration);
	}

	/**
	 * Bind.
	 * 
	 * @throws Exception
	 */
	@Override
	public void bind() throws Exception {
		super.bind();
	}

	/**
	 * Binding.
	 * 
	 * @throws Exception
	 *             Exception.
	 */
	@Override
	protected void binding() throws Exception {
		super.binding();
	}

	/**
	 * Gets the bid topic suffix (String) value.
	 * 
	 * @return The bid topic suffix (<code>String</code>) value.
	 */
	protected String getBidTopicSuffix() {
		return this.bidTopicSuffix;
	}

	/**
	 * Gets the log topic suffix (String) value.
	 * 
	 * @return The log topic suffix (<code>String</code>) value.
	 */
	protected String getLogTopicSuffix() {
		return this.logTopicSuffix;
	}

	/**
	 * Gets the price info topic suffix (String) value.
	 * 
	 * @return The price info topic suffix (<code>String</code>) value.
	 */
	protected String getPriceInfoTopicSuffix() {
		return this.priceInfoTopicSuffix;
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		this.bidTopicSuffix = getProperty(BaseAdapterConfiguration.BID_TOPIC_SUFFIX_PROPERTY,
				BaseAdapterConfiguration.BID_TOPIC_SUFFIX_DEFAULT);
		this.priceInfoTopicSuffix = getProperty(BaseAdapterConfiguration.PRICE_INFO_TOPIC_SUFFIX_PROPERTY,
				BaseAdapterConfiguration.PRICE_INFO_TOPIC_SUFFIX_DEFAULT);
		this.logTopicSuffix = getProperty(BaseAdapterConfiguration.LOG_TOPIC_SUFFIX_PROPERTY,
				BaseAdapterConfiguration.LOG_TOPIC_SUFFIX_DEFAULT);
	}

	/**
	 * Sets the configuration value.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	@Override
	public void setConfiguration(final Configurable configuration) {
		super.setConfiguration(configuration);
		initialize();
	}

	/**
	 * Unbinding.
	 */
	@Override
	protected void unbinding() {
		super.unbinding();
	}

}
