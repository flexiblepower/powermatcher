package net.powermatcher.core.messaging.protocol.adapter.log;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.Date;

import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.log.PriceLogInfo;


/**
 * @author IBM
 * @version 0.9.0
 */
public class PriceLogMessage extends AbstractLogMessage {
	/**
	 * Define the price log info (PriceLogInfo) field.
	 */
	private PriceLogInfo priceLogInfo;

	/**
	 * Constructs an instance of this class from the specified msg parameter.
	 * 
	 * @param msg
	 *            The msg (<code>byte[]</code>) parameter.
	 * @throws InvalidObjectException
	 *             Invalid Object Exception.
	 * @see #PriceLogMessage(PriceLogInfo)
	 */
	public PriceLogMessage(final byte[] msg) throws InvalidObjectException {
		super(MessageType.PRICE);
		fromBytes(msg);
	}

	/**
	 * Constructs an instance of this class from the specified price log info
	 * parameter.
	 * 
	 * @param priceLogInfo
	 *            The price log info (<code>PriceLogInfo</code>) parameter.
	 * @see #PriceLogMessage(byte[])
	 */
	public PriceLogMessage(final PriceLogInfo priceLogInfo) {
		super(MessageType.PRICE);
		this.priceLogInfo = priceLogInfo;
	}

	/**
	 * Append data with the specified strb parameter.
	 * 
	 * @param strb
	 *            The strb (<code>StringBuilder</code>) parameter.
	 */
	@Override
	protected void appendData(final StringBuilder strb) {
		strb.append(getPriceLogInfo());
	}

	/**
	 * From data input with the specified data input, cluster ID, agent ID,
	 * qualifier, time stamp and market basis parameters.
	 * 
	 * @param dataInput
	 *            The data input (<code>DataInput</code>) parameter.
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
	 * @throws IOException
	 *             IOException.
	 */
	@Override
	protected void fromDataInput(final DataInput dataInput, final String clusterId, final String agentId,
			final String qualifier, final Date timestamp, final MarketBasis marketBasis) throws IOException {
		double currentPrice = dataInput.readFloat();
		this.priceLogInfo = new PriceLogInfo(clusterId, agentId, qualifier, timestamp, marketBasis, currentPrice);
	}

	/**
	 * Gets the price log info value.
	 * 
	 * @return The price log info (<code>PriceLogInfo</code>) value.
	 */
	public PriceLogInfo getPriceLogInfo() {
		return this.priceLogInfo;
	}

	/**
	 * To data output with the specified data output parameter.
	 * 
	 * @param dataOutput
	 *            The data output (<code>DataOutput</code>) parameter.
	 * @throws IOException
	 *             IOException.
	 */
	@Override
	protected void toDataOutput(final DataOutput dataOutput) throws IOException {
		super.toDataOutput(dataOutput, this.priceLogInfo);
		dataOutput.writeFloat((float) this.priceLogInfo.getCurrentPrice());
	}

}
