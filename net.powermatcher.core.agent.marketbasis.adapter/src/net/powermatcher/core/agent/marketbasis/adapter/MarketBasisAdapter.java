package net.powermatcher.core.agent.marketbasis.adapter;


import net.powermatcher.core.adapter.Adapter;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.service.AgentConnectorService;
import net.powermatcher.core.agent.marketbasis.adapter.config.MarketBasisAdapterConfiguration;
import net.powermatcher.core.configurable.service.ConfigurationService;


/**
 * 
 * A MarketBasisAdapter object facilitates the creation of a market basis for the
 * connected agent.
 * 
 * <p>
 * The creation of the market basis is fully configurable through a ConfigurationService
 * object. The properties names and default values are defined in MarketBasisAdapterConfiguration.
 * The following properties can be set:
 * 
 * <dl>
 * <dt>Commodity</dt>
 * <dd>- the article, goods or product the market basis is defined for bids. For example: Electricity</dd>
 * <dt>Currency</dt>
 * <dd>- the currency for the bid, for example EUR (Euro).</dd>
 * </dl>
 * <dt>Maximum price</dt>
 * <dd>- value of the maximum price for a bid EUR (Euro).</dd>
 * </dl>
 * <dt>Minimum price</dt>
 * <dd>- value of the minimum price for a bid.</dd>
 * </dl>
 * <dt>Price steps</dt>
 * <dd>- defines the number of price steps between the minimum and maximum price. For example: if
 * price is between 0 and 10 and there 5 price steps then there are 6 possible prices (0, 2, 4 ,6, 9, 10)</dd>
 * </dl> 
 * <dt>Significance</dt>
 * <dd>- the scale or the number of decimals the prices should be rounded to.</dd>
 * </dl> 
 * </p>
 * <p>
 * You can bind the market basis adapter to an agent AgentConnectorService interface.
 * Once the object is assigned with the setConnector(AgentConnectorService) method, the bind() method
 * will bind the adapter to the agent and invoke the AgentService interface to update the market basis. 
 * </p>
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see MarketBasis
 * @see Adapter
 */
public class MarketBasisAdapter extends Adapter {

	/**
	 * Define the commodity (String) field.
	 */
	private String commodity;
	/**
	 * Define the currency (String) field.
	 */
	private String currency;
	/**
	 * Define the minimum price (double) field.
	 */
	private double minimumPrice;
	/**
	 * Define the maximum price (double) field.
	 */
	private double maximumPrice;
	/**
	 * Define the price steps (int) field.
	 */
	private int priceSteps;
	/**
	 * Define the significance (int) field.
	 */
	private int significance;
	/**
	 * Define the market ref (int) field.
	 */
	private int marketRef;

	/**
	 * Define the agent connector (AgentConnectorService) field.
	 */
	private AgentConnectorService agentConnector;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #MarketBasisAdapter(ConfigurationService)
	 */
	public MarketBasisAdapter() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified configuration
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #MarketBasisAdapter()
	 */
	public MarketBasisAdapter(final ConfigurationService configuration) {
		super(configuration);
	}

	/**
	 * Bind. Create a new market basis according to the configured properties,
	 * retrieve the agent via the AgentConnectorService and invoke the AgentService
	 * of the agent to update the market basis. Before execute this method set the agent
	 * connector.
	 * 
	 * @throws Exception
	 * 
	 * @see #setAgentConnector(AgentConnectorService)
	 */
	@Override
	public void bind() throws Exception {
		super.bind();
		MarketBasis newMarketBasis = new MarketBasis(getCommodity(), getCurrency(), getPriceSteps(), getMinimumPrice(),
				getMaximumPrice(), getSignificance(), getMarketRef());
		this.agentConnector.getAgent().updateMarketBasis(newMarketBasis);
	}

	/**
	 * Get the agent connector that has been set.
	 * 
	 * @return The agent connector that has been set.
	 * @see #setAgentConnector(AgentConnectorService)
	 */
	public AgentConnectorService getAgentConnector() {
		return this.agentConnector;
	}

	/**
	 * Gets the commodity (String) value.
	 * 
	 * @return The commodity (<code>String</code>) value.
	 */
	protected String getCommodity() {
		return this.commodity;
	}

	/**
	 * Gets the currency (String) value.
	 * 
	 * @return The currency (<code>String</code>) value.
	 */
	protected String getCurrency() {
		return this.currency;
	}

	/**
	 * Gets the market ref (int) value.
	 * 
	 * @return The market ref (<code>int</code>) value.
	 */
	public int getMarketRef() {
		return this.marketRef;
	}

	/**
	 * Gets the maximum price (double) value.
	 * 
	 * @return The maximum price (<code>double</code>) value.
	 */
	protected double getMaximumPrice() {
		return this.maximumPrice;
	}

	/**
	 * Gets the minimum price (double) value.
	 * 
	 * @return The minimum price (<code>double</code>) value.
	 */
	protected double getMinimumPrice() {
		return this.minimumPrice;
	}

	/**
	 * Gets the price steps (int) value.
	 * 
	 * @return The price steps (<code>int</code>) value.
	 */
	protected int getPriceSteps() {
		return this.priceSteps;
	}

	/**
	 * Gets the significance (int) value.
	 * 
	 * @return The significance (<code>int</code>) value.
	 */
	protected int getSignificance() {
		return this.significance;
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		this.commodity = getProperty(MarketBasisAdapterConfiguration.COMMODITY_PROPERTY,
				MarketBasisAdapterConfiguration.COMMODITY_DEFAULT);
		this.currency = getProperty(MarketBasisAdapterConfiguration.CURRENCY_PROPERTY,
				MarketBasisAdapterConfiguration.CURRENCY_DEFAULT);
		this.minimumPrice = getProperty(MarketBasisAdapterConfiguration.MINIMUM_PRICE_PROPERTY,
				MarketBasisAdapterConfiguration.MINIMUM_PRICE_DEFAULT);
		this.maximumPrice = getProperty(MarketBasisAdapterConfiguration.MAXIMUM_PRICE_PROPERTY,
				MarketBasisAdapterConfiguration.MAXIMUM_PRICE_DEFAULT);
		this.priceSteps = getProperty(MarketBasisAdapterConfiguration.PRICE_STEPS_PROPERTY,
				MarketBasisAdapterConfiguration.PRICE_STEPS_DEFAULT);
		this.significance = getProperty(MarketBasisAdapterConfiguration.SIGNIFICANCE_PROPERTY,
				MarketBasisAdapterConfiguration.SIGNIFICANCE_DEFAULT);
		this.marketRef = getProperty(MarketBasisAdapterConfiguration.MARKET_REF_PROPERTY,
				MarketBasisAdapterConfiguration.MARKET_REF_DEFAULT);
	}

	/**
	 * Is enabled.
	 * 
	 * @return The is enabled (<code>boolean</code>) value.
	 */
	@Override
	public boolean isEnabled() {
		return super.isEnabled() && getAgentConnector().isEnabled();
	}

	/**
	 * Sets the agent connector value.
	 * 
	 * @param agentConnector
	 *            The agent connector (<code>AgentConnectorService</code>)
	 *            parameter.
	 * @see #getAgentConnector()
	 */
	public void setAgentConnector(final AgentConnectorService agentConnector) {
		this.agentConnector = agentConnector;
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
	 * Unbind.
	 */
	@Override
	public void unbind() {
		super.unbind();
	}

}
