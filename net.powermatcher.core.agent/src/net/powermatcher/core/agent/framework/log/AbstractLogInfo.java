package net.powermatcher.core.agent.framework.log;


import java.util.Date;

import net.powermatcher.core.agent.framework.data.MarketBasis;


/**
 * @author IBM
 * @version 0.9.0
 */
public abstract class AbstractLogInfo {
	/**
	 * Define the agent log qualifier (String) constant.
	 */
	public static final String AGENT_LOG_QUALIFIER = "agent";

	/**
	 * Define the matcher log qualifier (String) constant.
	 */
	public static final String MATCHER_LOG_QUALIFIER = "matcher";

	/**
	 * Define the clusterId (String) field.
	 */
	private String clusterId;
	/**
	 * Define the agentId (String) field.
	 */
	private String agentId;
	/**
	 * Define the qualifier (String) field.
	 */
	private String qualifier;
	/**
	 * Define the timestamp (Date) field.
	 */
	private Date timestamp;
	/**
	 * Define the market basis (MarketBasis) field.
	 */
	private MarketBasis marketBasis;

	/**
	 * Constructs an instance of this class from the specified cluster ID, agent
	 * ID, qualifier, time stamp and market basis parameters.
	 * 
	 * @param clusterId
	 *            The cluster ID (<code>String</code>) parameter.
	 * @param agentId
	 *            The agent ID (<code>String</code>) parameter.
	 * @param qualifier
	 *            The qualifier (<code>String</code>) parameter.
	 * @param timestamp
	 *            The time stamp (<code>Date</code>) parameter.
	 * @param marketBasis
	 *            The market basis (<code>MarketBasis</code>) parameter.
	 */
	protected AbstractLogInfo(final String clusterId, final String agentId, final String qualifier, final Date timestamp,
			final MarketBasis marketBasis) {
		this.clusterId = clusterId;
		this.agentId = agentId;
		this.qualifier = qualifier;
		this.timestamp = timestamp;
		this.marketBasis = marketBasis;
	}

	/**
	 * Append data with the specified strb parameter.
	 * 
	 * @param strb
	 *            The strb (<code>StringBuilder</code>) parameter.
	 */
	protected void appendData(final StringBuilder strb) {
		strb.append("clusterId=");
		strb.append(getClusterId());
		strb.append(", agentId=");
		strb.append(getAgentId());
		strb.append(", qualifier=");
		strb.append(getQualifier());
		strb.append(", timestamp=");
		strb.append(getTimestamp());
		strb.append(", marketBasis=");
		strb.append(getMarketBasis());
	}

	/**
	 * Gets the agent ID (String) value.
	 * 
	 * @return The agent ID (<code>String</code>) value.
	 */
	public String getAgentId() {
		return this.agentId;
	}

	/**
	 * Gets the cluster ID (String) value.
	 * 
	 * @return The cluster ID (<code>String</code>) value.
	 */
	public String getClusterId() {
		return this.clusterId;
	}

	/**
	 * Gets the market basis value.
	 * 
	 * @return The market basis (<code>MarketBasis</code>) value.
	 */
	public MarketBasis getMarketBasis() {
		return this.marketBasis;
	}

	/**
	 * Gets the qualifier (String) value.
	 * 
	 * @return The qualifier (<code>String</code>) value.
	 */
	public String getQualifier() {
		return this.qualifier;
	}

	/**
	 * Gets the time stamp (Date) value.
	 * 
	 * @return The time stamp (<code>Date</code>) value.
	 */
	public Date getTimestamp() {
		return this.timestamp;
	}

	/**
	 * Returns the string value.
	 * 
	 * @return The string (<code>String</code>) value.
	 */
	@Override
	public String toString() {
		StringBuilder strb = new StringBuilder();
		strb.append(getClass().getSimpleName());
		strb.append('(');
		appendData(strb);
		strb.append(')');
		return strb.toString();
	}

}
