package net.powermatcher.core.messaging.protocol.adapter.internal;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;

import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.MarketBasisCache;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.messaging.protocol.adapter.msg.PriceInfoMessage;


/**
 * @author IBM
 * @version 0.9.0
 */
public class InternalPriceInfoMessage extends AbstractInternalMessage implements PriceInfoMessage {
	/**
	 * Define the price info (PriceInfo) field.
	 */
	private PriceInfo priceInfo;

	/**
	 * Constructs an instance of this class from the specified market basis
	 * cache and msg parameters.
	 * 
	 * @param marketBasisCache
	 *            The market basis cache (<code>MarketBasisCache</code>)
	 *            parameter.
	 * @param msg
	 *            The msg (<code>byte[]</code>) parameter.
	 * @throws InvalidObjectException
	 *             Invalid Object Exception.
	 * @see #InternalPriceInfoMessage(PriceInfo)
	 */
	public InternalPriceInfoMessage(final MarketBasisCache marketBasisCache, final byte[] msg) throws InvalidObjectException {
		super(MessageType.PRICE);
		fromBytes(marketBasisCache, msg);
	}

	/**
	 * Constructs an instance of this class from the specified price info
	 * parameter.
	 * 
	 * @param priceInfo
	 *            The price info (<code>PriceInfo</code>) parameter.
	 * @see #InternalPriceInfoMessage(MarketBasisCache,byte[])
	 */
	public InternalPriceInfoMessage(final PriceInfo priceInfo) {
		super(MessageType.PRICE);
		this.priceInfo = priceInfo;
	}

	/**
	 * Append data with the specified strb parameter.
	 * 
	 * @param strb
	 *            The strb (<code>StringBuilder</code>) parameter.
	 */
	@Override
	protected void appendData(final StringBuilder strb) {
		strb.append("priceInfo=");
		strb.append(this.priceInfo);
	}

	/**
	 * From data input with the specified market basis cache and data input
	 * parameters.
	 * 
	 * @param marketBasisCache
	 *            The market basis cache (<code>MarketBasisCache</code>)
	 *            parameter.
	 * @param dataInput
	 *            The data input (<code>DataInput</code>) parameter.
	 * @throws IOException
	 *             IOException.
	 */
	@Override
	protected void fromDataInput(final MarketBasisCache marketBasisCache, final DataInput dataInput) throws IOException {
		String commodity = dataInput.readUTF();
		String currency = dataInput.readUTF();
		int priceSteps = dataInput.readShort();
		double minPrice = dataInput.readFloat();
		double maxPrice = dataInput.readFloat();
		int marketRef = dataInput.readByte() & 0xFF;
		int significance = dataInput.readByte() & 0xFF;
		double currentPrice = dataInput.readFloat();
		MarketBasis marketBasis = new MarketBasis(commodity, currency, priceSteps, minPrice, maxPrice, significance, marketRef);
		MarketBasis externalMarketBasis = marketBasisCache.registerExternalMarketBasis(marketBasis);
		this.priceInfo = new PriceInfo(externalMarketBasis, currentPrice);
	}

	/**
	 * Gets the price info value.
	 * 
	 * @return The price info (<code>PriceInfo</code>) value.
	 */
	@Override
	public PriceInfo getPriceInfo() {
		return this.priceInfo;
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
		MarketBasis marketBasis = this.priceInfo.getMarketBasis();
		dataOutput.writeUTF(marketBasis.getCommodity());
		dataOutput.writeUTF(marketBasis.getCurrency());
		dataOutput.writeShort(marketBasis.getPriceSteps());
		dataOutput.writeFloat((float) marketBasis.getMinimumPrice());
		dataOutput.writeFloat((float) marketBasis.getMaximumPrice());
		dataOutput.writeByte(marketBasis.getMarketRef());
		dataOutput.writeByte(marketBasis.getSignificance());
		dataOutput.writeFloat((float) this.priceInfo.getCurrentPrice());
	}

}
